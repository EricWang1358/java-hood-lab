package com.javahood.seckill.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "seckill_order", uniqueConstraints = {
        @UniqueConstraint(name = "uk_campaign_user", columnNames = {"campaign_id", "user_id"})
})
public class SeckillOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 64)
    private String orderNo;

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected SeckillOrder() {
    }

    public SeckillOrder(String orderNo, Long campaignId, Long userId, String traceId) {
        this.orderNo = orderNo;
        this.campaignId = campaignId;
        this.userId = userId;
        this.traceId = traceId;
        this.status = OrderStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public SeckillOrder(String orderNo, Long campaignId, Long userId) {
        this(orderNo, campaignId, userId, null);
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

    public OrderStatus getStatus() {
        return status;
    }

    public String getTraceId() {
        return traceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
