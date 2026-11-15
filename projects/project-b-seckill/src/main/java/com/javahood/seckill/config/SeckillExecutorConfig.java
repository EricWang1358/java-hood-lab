package com.javahood.seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bounded ThreadPoolExecutor with explicit backpressure rejection (Lab #20: lab.poolreject).
 * Never uses unbounded LinkedBlockingQueue (avoids OOM) or silent DiscardPolicy (avoids silent order loss).
 */
@Configuration
public class SeckillExecutorConfig {

    private final AtomicLong rejectedCount = new AtomicLong(0);

    @Bean(name = "seckillOrderExecutor", destroyMethod = "shutdown")
    public ThreadPoolExecutor seckillOrderExecutor(SeckillProperties props) {
        AtomicInteger threadSeq = new AtomicInteger(1);
        return new ThreadPoolExecutor(
                props.poolCoreSize(),
                props.poolMaxSize(),
                30L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(props.poolQueueCapacity()),
                r -> {
                    Thread t = new Thread(r, "seckill-order-" + threadSeq.getAndIncrement());
                    t.setDaemon(false);
                    return t;
                },
                (r, executor) -> {
                    rejectedCount.incrementAndGet();
                    throw new RejectedExecutionException("Seckill order queue saturated (backpressure)");
                }
        );
    }

    public long getRejectedCount() {
        return rejectedCount.get();
    }
}
