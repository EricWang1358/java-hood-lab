package com.javahood.seckill;

import com.javahood.seckill.domain.CouponCampaign;
import com.javahood.seckill.domain.OutboxStatus;
import com.javahood.seckill.outbox.InMemoryOrderEventPublisher;
import com.javahood.seckill.outbox.OutboxRelayScheduler;
import com.javahood.seckill.repository.CouponCampaignRepository;
import com.javahood.seckill.repository.SeckillOrderRepository;
import com.javahood.seckill.repository.TxOutboxRepository;
import com.javahood.seckill.service.OrderStateService;
import com.javahood.seckill.service.SeckillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SeckillConcurrencyBenchmarkTest {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private OrderStateService orderStateService;

    @Autowired
    private CouponCampaignRepository campaignRepository;

    @Autowired
    private SeckillOrderRepository orderRepository;

    @Autowired
    private TxOutboxRepository outboxRepository;

    @Autowired
    private OutboxRelayScheduler outboxRelayScheduler;

    @Autowired
    private InMemoryOrderEventPublisher eventPublisher;

    @Autowired
    @Qualifier("seckillOrderExecutor")
    private ThreadPoolExecutor orderExecutor;

    @Test
    void optimizedSeckill_zeroOversell_perUserLimit_andTxOutboxDelivered() throws Exception {
        eventPublisher.clear();
        CouponCampaign campaign = seckillService.createCampaign("VIP-Coupon-100", 100);
        Long cid = campaign.getId();

        int threads = 32;
        int totalRequests = 1600; // 800 distinct users, each hitting twice
        ExecutorService clients = Executors.newFixedThreadPool(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(totalRequests);

        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger alreadyClaimed = new AtomicInteger();
        AtomicInteger soldOut = new AtomicInteger();

        for (int i = 0; i < totalRequests; i++) {
            final long userId = (i % 800) + 1L;
            clients.submit(() -> {
                try {
                    startGate.await();
                    var resp = seckillService.claimOptimized(cid, userId);
                    switch (resp.outcome()) {
                        case ACCEPTED -> accepted.incrementAndGet();
                        case ALREADY_CLAIMED -> alreadyClaimed.incrementAndGet();
                        case SOLD_OUT -> soldOut.incrementAndGet();
                        case BACKPRESSURE_REJECTED -> {
                        }
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneGate.countDown();
                }
            });
        }

        long t0 = System.nanoTime();
        startGate.countDown();
        assertThat(doneGate.await(15, TimeUnit.SECONDS)).isTrue();
        long preDeductMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
        clients.shutdown();

        // Wait for async order persistence pool to drain
        long deadline = System.currentTimeMillis() + 10_000;
        while ((orderExecutor.getActiveCount() > 0 || !orderExecutor.getQueue().isEmpty())
                && System.currentTimeMillis() < deadline) {
            Thread.sleep(25);
        }

        // Trigger outbox relay
        outboxRelayScheduler.relayPendingEvents();

        long persistedOrders = orderRepository.countByCampaignId(cid);
        CouponCampaign reloaded = campaignRepository.findById(cid).orElseThrow();

        System.out.printf("[Seckill Benchmark] 1600 requests (32 threads) pre-deducted in %d ms | accepted=%d, dup=%d, soldOut=%d%n",
                preDeductMs, accepted.get(), alreadyClaimed.get(), soldOut.get());

        assertThat(accepted.get()).isEqualTo(100);
        assertThat(persistedOrders).isEqualTo(100);
        assertThat(reloaded.getRemainingStock()).isZero();
        assertThat(outboxRepository.countByStatus(OutboxStatus.PENDING)).isZero();
        assertThat(eventPublisher.deliveredCount()).isGreaterThanOrEqualTo(100);
    }

    @Test
    void orderStateMachine_concurrentPayVsTimeoutClose_mutuallyExclusive() throws Exception {
        CouponCampaign campaign = seckillService.createCampaign("CAS-Order-Test", 10);
        var claim = seckillService.claimNaiveSynchronized(campaign.getId(), 90001L);
        String orderNo = claim.orderNo();

        int racers = 16;
        ExecutorService pool = Executors.newFixedThreadPool(racers);
        CountDownLatch gate = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < racers; i++) {
            final boolean isPay = (i % 2 == 0);
            futures.add(pool.submit(() -> {
                gate.await();
                return isPay ? orderStateService.markPaid(orderNo) : orderStateService.closeTimeout(orderNo);
            }));
        }
        gate.countDown();

        int winners = 0;
        for (Future<Boolean> f : futures) {
            if (f.get(5, TimeUnit.SECONDS)) {
                winners++;
            }
        }
        pool.shutdown();

        assertThat(winners).isEqualTo(1);
    }

    @Test
    void benchmark_naiveVsOptimized_throughputComparison() throws Exception {
        int stock = 100;
        int requests = 400;

        CouponCampaign naiveCamp = seckillService.createCampaign("Naive-Baseline", stock);
        long t1 = System.nanoTime();
        for (int i = 0; i < requests; i++) {
            seckillService.claimNaiveSynchronized(naiveCamp.getId(), 10000L + i);
        }
        long naiveMs = Math.max(1, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t1));

        CouponCampaign optCamp = seckillService.createCampaign("Optimized-Path", stock);
        long t2 = System.nanoTime();
        for (int i = 0; i < requests; i++) {
            seckillService.claimOptimized(optCamp.getId(), 20000L + i);
        }
        long optMs = Math.max(1, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t2));

        long deadline = System.currentTimeMillis() + 10_000;
        while ((orderExecutor.getActiveCount() > 0 || !orderExecutor.getQueue().isEmpty())
                && System.currentTimeMillis() < deadline) {
            Thread.sleep(25);
        }

        double naiveQps = requests * 1000.0 / naiveMs;
        double optQps = requests * 1000.0 / optMs;
        System.out.printf("[QPS Comparison] Naive DB Sync: %d ms (%.0f req/s) vs Optimized Sharded+Async: %d ms (%.0f req/s)%n",
                naiveMs, naiveQps, optMs, optQps);

        assertThat(orderRepository.countByCampaignId(naiveCamp.getId())).isEqualTo(stock);
        assertThat(orderRepository.countByCampaignId(optCamp.getId())).isEqualTo(stock);
    }
}
