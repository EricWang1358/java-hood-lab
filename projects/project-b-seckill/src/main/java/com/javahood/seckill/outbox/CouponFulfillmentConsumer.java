package com.javahood.seckill.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javahood.seckill.domain.TxOutbox;
import com.javahood.seckill.domain.UserCoupon;
import com.javahood.seckill.repository.UserCouponRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Downstream Idempotent MQ Consumer (Module 6: MQ At-Least-Once + Idempotent Consumption).
 *
 * Why do we STILL need consumer idempotency when TxOutbox already exists?
 * Because MQ brokers (RocketMQ / Kafka) guarantee AT-LEAST-ONCE delivery: if the broker receives the message
 * from OutboxRelayScheduler, delivers it, and network times out before ACK returns, OutboxRelayScheduler
 * will resend the exact same event!
 * Two-layer defense:
 *   1) Fast path: `existsByOrderNo(orderNo)` skips already-processed orders cleanly.
 *   2) Hard floor: DB unique index `uk_coupon_order_no(order_no)` catches concurrent duplicate consumers!
 */
@Service
public class CouponFulfillmentConsumer {

    private final UserCouponRepository userCouponRepository;
    private final ObjectMapper objectMapper;

    public CouponFulfillmentConsumer(UserCouponRepository userCouponRepository, ObjectMapper objectMapper) {
        this.userCouponRepository = userCouponRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean onOrderCreatedEvent(TxOutbox event) {
        if (!"ORDER_CREATED".equals(event.getEventType())) {
            return false;
        }
        try {
            JsonNode root = objectMapper.readTree(event.getPayload());
            String orderNo = root.get("orderNo").asText();
            long campaignId = root.get("campaignId").asLong();
            long userId = root.get("userId").asLong();

            if (userCouponRepository.existsByOrderNo(orderNo)) {
                return false; // Duplicate delivery safely ignored
            }
            userCouponRepository.saveAndFlush(new UserCoupon(orderNo, campaignId, userId));
            return true;
        } catch (DataIntegrityViolationException dupEx) {
            // Concurrent duplicate consumer hit uk_coupon_order_no unique index -> idempotent success
            return false;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to consume outbox event " + event.getEventId(), ex);
        }
    }
}
