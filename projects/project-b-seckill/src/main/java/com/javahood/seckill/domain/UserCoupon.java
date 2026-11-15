package com.javahood.seckill.domain;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Downstream coupon asset issued by the idempotent MQ consumer.
 * Protected by unique constraint `uk_coupon_order_no` so duplicate MQ deliveries never issue 2 coupons.
 */
@Entity
@Table(name = "user_coupon", uniqueConstraints = {
        @UniqueConstraint(name = "uk_coupon_order_no", columnNames = {"order_no"})
})
public class UserCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 64)
    private String orderNo;

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt = Instant.now();

    protected UserCoupon() {
    }

    public UserCoupon(String orderNo, Long campaignId, Long userId) {
        this.orderNo = orderNo;
        this.campaignId = campaignId;
        this.userId = userId;
        this.issuedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public Long getUserId() {
        return userId;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }
}
