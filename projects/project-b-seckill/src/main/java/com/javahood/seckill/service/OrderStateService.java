package com.javahood.seckill.service;

import com.javahood.seckill.domain.OrderStatus;
import com.javahood.seckill.domain.SeckillOrder;
import com.javahood.seckill.engine.PreDeductEngine;
import com.javahood.seckill.repository.CouponCampaignRepository;
import com.javahood.seckill.repository.SeckillOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;

/**
 * CAS Order State Machine + Safe Timeout Stock Rollback (Lab #24: lab.orderstate).
 *
 * Foundation insights:
 * 1) Why check `casWon == 1` before restoring stock?
 *    If 2 timeout-scanner threads (or user cancel + timeout job) hit the same order concurrently,
 *    ONLY the single thread whose SQL `UPDATE ... WHERE status = 'PENDING'` returns 1 is allowed
 *    to increment DB & Redis/shard stock. Otherwise stock gets restored twice -> oversell!
 * 2) Why restore Redis/in-memory shard stock inside `afterCommit()`?
 *    If we incremented Redis stock BEFORE the DB transaction committed, and the DB commit failed
 *    (or a buyer claimed before DB stock updated), Redis and MySQL stock would diverge!
 */
@Service
public class OrderStateService {

    private final SeckillOrderRepository orderRepository;
    private final CouponCampaignRepository campaignRepository;
    private final PreDeductEngine preDeductEngine;

    public OrderStateService(SeckillOrderRepository orderRepository,
                             CouponCampaignRepository campaignRepository,
                             PreDeductEngine preDeductEngine) {
        this.orderRepository = orderRepository;
        this.campaignRepository = campaignRepository;
        this.preDeductEngine = preDeductEngine;
    }

    @Transactional
    public boolean markPaid(String orderNo) {
        return orderRepository.casTransitionStatus(orderNo, OrderStatus.PENDING, OrderStatus.PAID, Instant.now()) == 1;
    }

    @Transactional
    public boolean closeTimeout(String orderNo) {
        SeckillOrder order = orderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) {
            return false;
        }
        Long campaignId = order.getCampaignId();

        int casWon = orderRepository.casTransitionStatus(
                orderNo, OrderStatus.PENDING, OrderStatus.CLOSED, Instant.now());
        if (casWon != 1) {
            return false;
        }

        int dbRestored = campaignRepository.incrementStockIfBelowTotal(campaignId);
        if (dbRestored == 1) {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        preDeductEngine.restoreStockAfterClose(campaignId);
                    }
                });
            } else {
                preDeductEngine.restoreStockAfterClose(campaignId);
            }
        }
        return true;
    }
}
