package com.javahood.seckill.repository;

import com.javahood.seckill.domain.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    boolean existsByOrderNo(String orderNo);

    long countByCampaignId(Long campaignId);

    long countByOrderNo(String orderNo);

    List<UserCoupon> findByUserIdOrderByIdDesc(Long userId);
}
