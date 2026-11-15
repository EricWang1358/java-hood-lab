package com.javahood.seckill.drills;

import com.javahood.seckill.config.SeckillExecutorConfig;
import com.javahood.seckill.config.SeckillTraceFilter;
import com.javahood.seckill.domain.CouponCampaign;
import com.javahood.seckill.domain.SeckillOrder;
import com.javahood.seckill.domain.TxOutbox;
import com.javahood.seckill.engine.PreDeductEngine;
import com.javahood.seckill.outbox.CouponFulfillmentConsumer;
import com.javahood.seckill.repository.CouponCampaignRepository;
import com.javahood.seckill.repository.SeckillOrderRepository;
import com.javahood.seckill.repository.UserCouponRepository;
import com.javahood.seckill.service.CampaignQueryService;
import com.javahood.seckill.service.OrderStateService;
import com.javahood.seckill.service.SeckillOrderPersistenceService;
import com.javahood.seckill.service.SeckillService;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 项目 B 高并发与分布式一致性基础演练（5 个可打断点的「踩坑 vs 修复」实证测试）
 *
 * 把前序并发课（#19, #20, #24, #25, #26, #32）与 Redis/MQ 工程课的核心难点全部写成可运行断言。
 */
@SpringBootTest
class SeckillFoundationDrillsTest {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private SeckillOrderPersistenceService persistenceService;

    @Autowired
    private OrderStateService orderStateService;

    @Autowired
    private CampaignQueryService campaignQueryService;

    @Autowired
    private CouponFulfillmentConsumer fulfillmentConsumer;

    @Autowired
    private PreDeductEngine preDeductEngine;

    @Autowired
    private CouponCampaignRepository campaignRepository;

    @Autowired
    private SeckillOrderRepository orderRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    @Qualifier("seckillOrderExecutor")
    private ThreadPoolExecutor orderExecutor;

    /**
     * 演练 01：异步下单跨线程池 traceId 丢失 vs 快照装饰器透传（对应第 19 课 lab.tlinherit）
     * - 【基础盲区】：主线程把 traceId 放进 MDC，丢进 CompletableFuture.runAsync 后日志里 traceId 全变成 null。
     * - 【机盖底下】：MDC 底层是普通 ThreadLocal，异步线程池里的线程是提前建好复用的；
     *   必须在 execute() 提交瞬间拍下 callerContext 快照，在子线程运行前恢复、finally 里清空。
     */
    @Test
    void drill01_asyncPoolMdcSnapshot_propagatesTraceIdToPersistedOrderAndCleansWorker() throws Exception {
        // 1) 错误示范：裸线程池直接丢丢失主线程 MDC
        ExecutorService rawPool = Executors.newSingleThreadExecutor();
        MDC.put(SeckillTraceFilter.MDC_TRACE_KEY, "trace-caller-001");
        String seenInRawPool;
        try {
            seenInRawPool = rawPool.submit(() -> MDC.get(SeckillTraceFilter.MDC_TRACE_KEY)).get();
        } finally {
            rawPool.shutdown();
        }
        assertThat(seenInRawPool).isNull();

        // 2) 正确示范：使用本工程包装了 MDC 快照的 seckillOrderExecutor 发起抢券落库
        CouponCampaign campaign = seckillService.createCampaign("MDC-Trace-Drill", 5);
        SeckillService.ClaimResponse claim;
        try {
            claim = seckillService.claimOptimized(campaign.getId(), 7001L);
        } finally {
            MDC.remove(SeckillTraceFilter.MDC_TRACE_KEY);
        }

        waitExecutorIdle(orderExecutor);
        SeckillOrder saved = orderRepository.findByOrderNo(claim.orderNo()).orElseThrow();
        assertThat(saved.getTraceId()).isEqualTo("trace-caller-001");

        // 并且工作线程执行完毕后，自身的 MDC 已被 finally 清空，绝不污染下一单
        String residualOnWorker = orderExecutor.submit(() -> MDC.get(SeckillTraceFilter.MDC_TRACE_KEY)).get();
        assertThat(residualOnWorker).isNull();
    }

    /**
     * 演练 02：超时关单回补库存 —— 为什么必须「CAS 赢家唯一回补 + afterCommit 再加缓存库存」？（对应第 24 课）
     * - 【基础盲区】：先 select 看状态是 PENDING，就 update 关单并给库存 +1；
     *   两个定时任务或「用户取消 + 超时关单」并发撞上时，1 张未付订单竟把库存回补了 2 次，直接造成后续超卖！
     * - 【机盖底下】：OrderStateService.closeTimeout 只有当 SQL 条件更新返回影响行数 == 1 时才给 DB 库存 +1，
     *   并通过 TransactionSynchronizationManager 在事务真正提交后才把 1 个库存加回分片池，使下一位买家能成功抢到。
     */
    @Test
    void drill02_timeoutCloseStockRestore_16ConcurrentClosersRestoreStrictlyOnce_andNextBuyerSucceeds() throws Exception {
        CouponCampaign camp = seckillService.createCampaign("Close-Restore-Stock-1", 1);
        Long cid = camp.getId();

        // 买家 8001 抢走了唯一 1 张券（此时预扣与 DB 剩余库存均为 0）
        var firstClaim = seckillService.claimOptimized(cid, 8001L);
        assertThat(firstClaim.outcome()).isEqualTo(SeckillService.ClaimOutcome.ACCEPTED);
        waitExecutorIdle(orderExecutor);
        assertThat(preDeductEngine.remainingStock(cid)).isZero();
        assertThat(campaignRepository.findById(cid).orElseThrow().getRemainingStock()).isZero();

        // 16 个线程同时尝试超时关闭这同一笔订单
        ExecutorService racers = Executors.newFixedThreadPool(16);
        CountDownLatch gate = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            futures.add(racers.submit(() -> {
                gate.await();
                return orderStateService.closeTimeout(firstClaim.orderNo());
            }));
        }
        gate.countDown();

        int closeWinners = 0;
        for (Future<Boolean> f : futures) {
            if (f.get(5, TimeUnit.SECONDS)) {
                closeWinners++;
            }
        }
        racers.shutdown();

        // 严格只有 1 个线程关单成功，且 DB 与分片库存都精准回补到 1（绝不会变成 16！）
        assertThat(closeWinners).isEqualTo(1);
        assertThat(campaignRepository.findById(cid).orElseThrow().getRemainingStock()).isEqualTo(1);
        assertThat(preDeductEngine.remainingStock(cid)).isEqualTo(1);

        // 新买家 8002 现在可以顺利抢走这张回补出来的券！
        var secondClaim = seckillService.claimOptimized(cid, 8002L);
        assertThat(secondClaim.outcome()).isEqualTo(SeckillService.ClaimOutcome.ACCEPTED);
        waitExecutorIdle(orderExecutor);
    }

    /**
     * 演练 03：MQ 至少一次投递（At-Least-Once）与消费端幂等防重发券（对应第 32 课 + 模块 6 MQ 幂等）
     * - 【基础盲区】：以为有了 tx_outbox 发件箱就万事大吉，下游消费端直接 insert 发券表。
     * - 【机盖底下】：发件箱投递成功但网络超时未收到 ACK 时，定时任务会再次投递同一条事件！
     *   CouponFulfillmentConsumer 靠 order_no 唯一键兜底，同一事件重复投递 5 次也只发 1 张券。
     */
    @Test
    void drill03_atLeastOnceDuplicateDelivery_idempotentConsumerIssuesStrictlyOneCoupon() {
        String dupOrderNo = "SK-IDEMPOTENT-" + UUID.randomUUID().toString().substring(0, 8);
        String payload = String.format("{\"orderNo\":\"%s\",\"campaignId\":999,\"userId\":5555}", dupOrderNo);
        TxOutbox event = new TxOutbox(UUID.randomUUID().toString(), "SECKILL_ORDER", dupOrderNo, "ORDER_CREATED", payload);

        int newlyIssuedCount = 0;
        for (int i = 0; i < 5; i++) {
            if (fulfillmentConsumer.onOrderCreatedEvent(event)) {
                newlyIssuedCount++;
            }
        }

        assertThat(newlyIssuedCount).isEqualTo(1);
        assertThat(userCouponRepository.countByOrderNo(dupOrderNo)).isEqualTo(1);
    }

    /**
     * 演练 04：秒杀预热详情页的缓存击穿（SingleFlight）与缓存穿透（空值哨兵）防护（对应第 25 课 + 模块 6）
     * - 【基础盲区】：冷键失效瞬间 50 个并发全打进 MySQL；或者恶意刷不存在的 campaignId=-404 把 DB 打挂。
     * - 【机盖底下】：CampaignQueryService 用 inFlight CompletableFuture 合并并发回源（50 并发只查 1 次 DB），
     *   并对不存在的 ID 缓存短 TTL 空哨兵（50 次恶意穿透查询也只查 1 次 DB）。
     */
    @Test
    void drill04_hotspotCampaignQuery_singleFlightAndNullSentinelCoalesceToSingleDbHit() throws Exception {
        CouponCampaign camp = seckillService.createCampaign("Hotspot-Read-Test", 50);
        campaignQueryService.resetStatsAndCache();

        int concurrency = 40;
        ExecutorService readers = Executors.newFixedThreadPool(concurrency);
        CountDownLatch gate1 = new CountDownLatch(1);
        List<Future<?>> f1 = new ArrayList<>();
        for (int i = 0; i < concurrency; i++) {
            f1.add(readers.submit(() -> {
                gate1.await();
                return campaignQueryService.getCampaignDetail(camp.getId()).orElseThrow();
            }));
        }
        gate1.countDown();
        for (Future<?> f : f1) {
            f.get(5, TimeUnit.SECONDS);
        }
        // 40 个并发冷读同一个有效活动 -> SingleFlight 合并为仅仅 1 次数据库查询！
        assertThat(campaignQueryService.getDbQueryCount()).isEqualTo(1L);

        // 再用 40 个并发恶意刷不存在的 ID = -999999L -> 空值哨兵挡住穿透，数据库也只被查 1 次！
        campaignQueryService.resetStatsAndCache();
        CountDownLatch gate2 = new CountDownLatch(1);
        List<Future<?>> f2 = new ArrayList<>();
        for (int i = 0; i < concurrency; i++) {
            f2.add(readers.submit(() -> {
                gate2.await();
                return campaignQueryService.getCampaignDetail(-999999L);
            }));
        }
        gate2.countDown();
        for (Future<?> f : f2) {
            assertThat((java.util.Optional<?>) f.get(5, TimeUnit.SECONDS)).isEmpty();
        }
        readers.shutdown();
        assertThat(campaignQueryService.getDbQueryCount()).isEqualTo(1L);
    }

    /**
     * 演练 05：有界队列饱和反压回滚 —— 绝不静默丢库存（对应第 20 课 lab.poolreject）
     * - 【基础盲区】：用 DiscardPolicy 吞掉拒绝异常，导致分片预扣库存已经 -1，异步落库任务却被扔进垃圾桶，库存永久少卖！
     * - 【机盖底下】：有界线程池满时抛 RejectedExecutionException，claimOptimized 捕获后立即调 preDeductEngine.rollback()，
     *   保证「剩余预扣库存 + 真实接受订单数 == 初始总库存」，账实一分不差。
     */
    @Test
    void drill05_boundedQueueBackpressure_rollsBackPreDeductStockZeroLeak() throws Exception {
        AtomicLong rejected = new AtomicLong();
        ThreadPoolExecutor tinyPool = SeckillExecutorConfig.buildBoundedMdcExecutor("tiny-", 1, 1, 1, rejected);
        CountDownLatch blocker = new CountDownLatch(1);
        try {
            // 先塞满 tinyPool 的 1 个核心线程 + 1 个队列槽位
            tinyPool.execute(() -> {
                try {
                    blocker.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            });
            tinyPool.execute(() -> {
            });

            SeckillService isolatedService = new SeckillService(
                    campaignRepository, orderRepository, preDeductEngine, persistenceService, tinyPool
            );
            CouponCampaign camp = isolatedService.createCampaign("Backpressure-Drill", 10);

            // 此时队列已满，买家 9527 抢券必然触发线程池拒绝反压
            SeckillService.ClaimResponse resp = isolatedService.claimOptimized(camp.getId(), 9527L);
            assertThat(resp.outcome()).isEqualTo(SeckillService.ClaimOutcome.BACKPRESSURE_REJECTED);
            assertThat(rejected.get()).isEqualTo(1L);

            // 核心验证：预扣的库存与用户限购名额必须已被完整回滚！剩余库存仍然是 10，且队列排空后 9527 可以重试成功！
            assertThat(preDeductEngine.remainingStock(camp.getId())).isEqualTo(10);

            blocker.countDown();
            waitExecutorIdle(tinyPool);

            SeckillService.ClaimResponse retryResp = isolatedService.claimOptimized(camp.getId(), 9527L);
            assertThat(retryResp.outcome()).isEqualTo(SeckillService.ClaimOutcome.ACCEPTED);
            waitExecutorIdle(tinyPool);
        } finally {
            blocker.countDown();
            tinyPool.shutdownNow();
        }
    }

    private static void waitExecutorIdle(ThreadPoolExecutor executor) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 10_000;
        while ((executor.getActiveCount() > 0 || !executor.getQueue().isEmpty())
                && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
    }
}
