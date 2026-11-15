package com.javahood.seckill.service;

import com.javahood.seckill.domain.CouponCampaign;
import com.javahood.seckill.domain.SeckillOrder;
import com.javahood.seckill.engine.PreDeductEngine;
import com.javahood.seckill.engine.PreDeductResult;
import com.javahood.seckill.repository.CouponCampaignRepository;
import com.javahood.seckill.repository.SeckillOrderRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class SeckillService {

    public enum ClaimOutcome {
        ACCEPTED,
        SOLD_OUT,
        ALREADY_CLAIMED,
        BACKPRESSURE_REJECTED
    }

    public record ClaimResponse(ClaimOutcome outcome, String orderNo) {
    }

    private final CouponCampaignRepository campaignRepository;
    private final SeckillOrderRepository orderRepository;
    private final PreDeductEngine preDeductEngine;
    private final SeckillOrderPersistenceService persistenceService;
    private final ThreadPoolExecutor orderExecutor;

    public SeckillService(CouponCampaignRepository campaignRepository,
                          SeckillOrderRepository orderRepository,
                          PreDeductEngine preDeductEngine,
                          SeckillOrderPersistenceService persistenceService,
                          @Qualifier("seckillOrderExecutor") ThreadPoolExecutor orderExecutor) {
        this.campaignRepository = campaignRepository;
        this.orderRepository = orderRepository;
        this.preDeductEngine = preDeductEngine;
        this.persistenceService = persistenceService;
        this.orderExecutor = orderExecutor;
    }

    @Transactional
    public CouponCampaign createCampaign(String title, int stock) {
        CouponCampaign saved = campaignRepository.save(new CouponCampaign(title, stock));
        preDeductEngine.initCampaignStock(saved.getId(), stock);
        return saved;
    }

    /**
     * Optimized path: Sharded atomic pre-deduct + per-user guard + bounded pool async tx-outbox persistence.
     */
    public ClaimResponse claimOptimized(Long campaignId, Long userId) {
        PreDeductResult pre = preDeductEngine.tryPreDeduct(campaignId, userId);
        if (pre.status() == PreDeductResult.Status.ALREADY_CLAIMED) {
            return new ClaimResponse(ClaimOutcome.ALREADY_CLAIMED, null);
        }
        if (pre.status() == PreDeductResult.Status.SOLD_OUT) {
            return new ClaimResponse(ClaimOutcome.SOLD_OUT, null);
        }

        String orderNo = "SK-" + campaignId + "-" + userId + "-" + UUID.randomUUID().toString().substring(0, 8);
        try {
            CompletableFuture.runAsync(() -> {
                try {
                    persistenceService.persistOrderAndOutbox(orderNo, campaignId, userId);
                } catch (Exception ex) {
                    preDeductEngine.rollback(campaignId, userId, pre.shardIndex());
                }
            }, orderExecutor);
            return new ClaimResponse(ClaimOutcome.ACCEPTED, orderNo);
        } catch (RejectedExecutionException ex) {
            preDeductEngine.rollback(campaignId, userId, pre.shardIndex());
            return new ClaimResponse(ClaimOutcome.BACKPRESSURE_REJECTED, null);
        }
    }

    /**
     * Naive synchronous DB-backed baseline for before/after QPS benchmarking.
     */
    @Transactional
    public synchronized ClaimResponse claimNaiveSynchronized(Long campaignId, Long userId) {
        if (orderRepository.existsByCampaignIdAndUserId(campaignId, userId)) {
            return new ClaimResponse(ClaimOutcome.ALREADY_CLAIMED, null);
        }
        int updated = campaignRepository.decrementStockIfPositive(campaignId);
        if (updated == 0) {
            return new ClaimResponse(ClaimOutcome.SOLD_OUT, null);
        }
        String orderNo = "NV-" + campaignId + "-" + userId + "-" + UUID.randomUUID().toString().substring(0, 8);
        SeckillOrder order = orderRepository.save(new SeckillOrder(orderNo, campaignId, userId));
        return new ClaimResponse(ClaimOutcome.ACCEPTED, order.getOrderNo());
    }
}
