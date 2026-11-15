package com.javahood.seckill.repository;

import com.javahood.seckill.domain.CouponCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponCampaignRepository extends JpaRepository<CouponCampaign, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CouponCampaign c SET c.remainingStock = c.remainingStock - 1 WHERE c.id = :id AND c.remainingStock > 0")
    int decrementStockIfPositive(@Param("id") Long id);

    /**
     * Bounded stock restoration when a PENDING order closes on timeout.
     * Guard `c.remainingStock < c.totalStock` guarantees stock can NEVER exceed totalStock.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE CouponCampaign c SET c.remainingStock = c.remainingStock + 1 WHERE c.id = :id AND c.remainingStock < c.totalStock")
    int incrementStockIfBelowTotal(@Param("id") Long id);
}
