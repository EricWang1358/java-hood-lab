package com.javahood.seckill.repository;

import com.javahood.seckill.domain.OrderStatus;
import com.javahood.seckill.domain.SeckillOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface SeckillOrderRepository extends JpaRepository<SeckillOrder, Long> {

    Optional<SeckillOrder> findByOrderNo(String orderNo);

    boolean existsByCampaignIdAndUserId(Long campaignId, Long userId);

    long countByCampaignId(Long campaignId);

    /**
     * CAS state transition (Lab #24: lab.orderstate) - prevents concurrent pay vs timeout-close races.
     */
    @Modifying
    @Query("UPDATE SeckillOrder o SET o.status = :target, o.updatedAt = :now WHERE o.orderNo = :orderNo AND o.status = :expected")
    int casTransitionStatus(@Param("orderNo") String orderNo,
                            @Param("expected") OrderStatus expected,
                            @Param("target") OrderStatus target,
                            @Param("now") Instant now);
}
