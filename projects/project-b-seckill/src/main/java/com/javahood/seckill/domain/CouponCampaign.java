package com.javahood.seckill.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "coupon_campaign")
public class CouponCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(name = "total_stock", nullable = false)
    private int totalStock;

    @Column(name = "remaining_stock", nullable = false)
    private int remainingStock;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected CouponCampaign() {
    }

    public CouponCampaign(String title, int totalStock) {
        this.title = title;
        this.totalStock = totalStock;
        this.remainingStock = totalStock;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public int getRemainingStock() {
        return remainingStock;
    }

    public void setRemainingStock(int remainingStock) {
        this.remainingStock = remainingStock;
    }

    public boolean isActive() {
        return active;
    }
}
