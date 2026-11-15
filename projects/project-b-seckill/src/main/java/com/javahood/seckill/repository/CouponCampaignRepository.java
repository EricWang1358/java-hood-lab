package com.javahood.seckill.repository;

import com.javahood.seckill.domain.CouponCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponCampaignRepository extends JpaRepository<CouponCampaign, Long> {

    @Modifying
    @Query("UPDATE CouponCampaign c SET c.remainingStock = c.remainingStock - 1 WHERE c.id = :id AND c.remainingStock > 0")
    int decrementStockIfPositive(@Param("id") Long id);
}
