package com.javahood.seckill.outbox;

import com.javahood.seckill.domain.OutboxStatus;
import com.javahood.seckill.domain.TxOutbox;
import com.javahood.seckill.repository.TxOutboxRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Polls tx_outbox PENDING events and relays them reliably to MQ (Lab #32: lab.txoutbox).
 */
@Component
public class OutboxRelayScheduler {

    private static final int MAX_RETRIES = 5;

    private final TxOutboxRepository outboxRepository;
    private final OrderEventPublisher publisher;

    public OutboxRelayScheduler(TxOutboxRepository outboxRepository, OrderEventPublisher publisher) {
        this.outboxRepository = outboxRepository;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelayString = "${app.seckill.outbox-relay-fixed-delay-ms:200}")
    @Transactional
    public int relayPendingEvents() {
        List<TxOutbox> batch = outboxRepository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.PENDING, PageRequest.of(0, 100));
        int sent = 0;
        for (TxOutbox event : batch) {
            try {
                publisher.publish(event);
                event.markSent();
                sent++;
            } catch (Exception ex) {
                event.markRetryOrFailed(MAX_RETRIES);
            }
        }
        return sent;
    }
}
