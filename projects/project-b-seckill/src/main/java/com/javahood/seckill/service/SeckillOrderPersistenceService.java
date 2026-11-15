package com.javahood.seckill.service;

import com.javahood.seckill.domain.SeckillOrder;
import com.javahood.seckill.domain.TxOutbox;
import com.javahood.seckill.repository.CouponCampaignRepository;
import com.javahood.seckill.repository.SeckillOrderRepository;
import com.javahood.seckill.repository.TxOutboxRepository;
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
     */
    @Transactional
    public SeckillOrder persistOrderAndOutbox(String orderNo, Long campaignId, Long userId) {
        int updated = campaignRepository.decrementStockIfPositive(campaignId);
        if (updated == 0) {
            throw new IllegalStateException("DB stock exhausted for campaign " + campaignId);
        }
        SeckillOrder order = orderRepository.save(new SeckillOrder(orderNo, campaignId, userId));
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
