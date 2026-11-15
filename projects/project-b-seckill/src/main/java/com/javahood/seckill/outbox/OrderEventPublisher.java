package com.javahood.seckill.outbox;

import com.javahood.seckill.domain.TxOutbox;

public interface OrderEventPublisher {
    void publish(TxOutbox event);
}
