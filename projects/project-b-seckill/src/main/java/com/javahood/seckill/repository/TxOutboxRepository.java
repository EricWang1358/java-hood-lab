package com.javahood.seckill.repository;

import com.javahood.seckill.domain.OutboxStatus;
import com.javahood.seckill.domain.TxOutbox;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TxOutboxRepository extends JpaRepository<TxOutbox, Long> {
    List<TxOutbox> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);
    long countByStatus(OutboxStatus status);
}
