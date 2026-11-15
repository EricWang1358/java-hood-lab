package com.javahood.seckill.outbox;

import com.javahood.seckill.domain.TxOutbox;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-JVM idempotent event bus simulating RocketMQ/Kafka broker delivery for local runs & benchmarks.
 */
@Component
public class InMemoryOrderEventPublisher implements OrderEventPublisher {

    private final Set<String> deliveredEventIds = ConcurrentHashMap.newKeySet();
    private final List<TxOutbox> deliveredEvents = new CopyOnWriteArrayList<>();

    @Override
    public void publish(TxOutbox event) {
        if (deliveredEventIds.add(event.getEventId())) {
            deliveredEvents.add(event);
        }
    }

    public int deliveredCount() {
        return deliveredEvents.size();
    }

    public void clear() {
        deliveredEventIds.clear();
        deliveredEvents.clear();
    }
}
