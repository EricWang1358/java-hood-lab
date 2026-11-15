package com.javahood.seckill.service;

import com.javahood.seckill.config.SeckillTraceFilter;
import com.javahood.seckill.domain.SeckillOrder;
import com.javahood.seckill.domain.TxOutbox;
import com.javahood.seckill.repository.CouponCampaignRepository;
import com.javahood.seckill.repository.SeckillOrderRepository;
import com.javahood.seckill.repository.TxOutboxRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SeckillOrderPersistenceService {

    private final CouponCampaignRepository campaignRepository;
    private final SeckillOrderRepository orderRepository;
    private final TxOutboxRepository outboxRepository;

    public SeckillOrderPersistenceService(CouponCampaignRepository campaignRepository,
                                          SeckillOrderRepository orderRepository,
                                          TxOutboxRepository outboxRepository) {
        this.campaignRepository = campaignRepository;
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
    }

    /**
     * Persists SeckillOrder and TxOutbox in the SAME local DB transaction (Lab #32: lab.txoutbox).
     * Also records MDC traceId propagated from caller thread via SeckillExecutorConfig (Lab #19: lab.tlinherit).
     */
    @Transactional
    public SeckillOrder persistOrderAndOutbox(String orderNo, Long campaignId, Long userId) {
        int updated = campaignRepository.decrementStockIfPositive(campaignId);
        if (updated == 0) {
            throw new IllegalStateException("DB stock exhausted for campaign " + campaignId);
        }
        String traceId = MDC.get(SeckillTraceFilter.MDC_TRACE_KEY);
        SeckillOrder order = orderRepository.save(new SeckillOrder(orderNo, campaignId, userId, traceId));
        String payload = String.format("{\"orderNo\":\"%s\",\"campaignId\":%d,\"userId\":%d}",
                orderNo, campaignId, userId);
        outboxRepository.save(new TxOutbox(
                UUID.randomUUID().toString(),
                "SECKILL_ORDER",
                orderNo,
                "ORDER_CREATED",
                payload
        ));
        return order;
    }
}
