package com.javahood.seckill.service;

import com.javahood.seckill.domain.OrderStatus;
import com.javahood.seckill.repository.SeckillOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * CAS Order State Machine (Lab #24: lab.orderstate).
 * Guarantees that concurrent Pay Callback and Timeout Close cannot both succeed.
 */
@Service
public class OrderStateService {

    private final SeckillOrderRepository orderRepository;

    public OrderStateService(SeckillOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public boolean markPaid(String orderNo) {
        return orderRepository.casTransitionStatus(orderNo, OrderStatus.PENDING, OrderStatus.PAID, Instant.now()) == 1;
    }

    @Transactional
    public boolean closeTimeout(String orderNo) {
        return orderRepository.casTransitionStatus(orderNo, OrderStatus.PENDING, OrderStatus.CLOSED, Instant.now()) == 1;
    }
}
