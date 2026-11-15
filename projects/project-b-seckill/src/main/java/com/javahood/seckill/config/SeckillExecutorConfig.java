package com.javahood.seckill.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bounded ThreadPoolExecutor combining TWO core concurrency lessons:
 * 1) Lab #20 (lab.poolreject): Bounded ArrayBlockingQueue + explicit RejectedExecutionException
 *    (never unbounded LinkedBlockingQueue OOM, never silent DiscardPolicy order loss).
 * 2) Lab #19 (lab.tlinherit): Snapshot-based MDC context propagation across pooled worker threads
 *    with guaranteed finally{} cleanup so worker reuse never leaks traceId to the next buyer!
 */
@Configuration
public class SeckillExecutorConfig {

    private final AtomicLong rejectedCount = new AtomicLong(0);

    @Bean(name = "seckillOrderExecutor", destroyMethod = "shutdown")
    public ThreadPoolExecutor seckillOrderExecutor(SeckillProperties props) {
        return buildBoundedMdcExecutor(
                "seckill-order-",
                props.poolCoreSize(),
                props.poolMaxSize(),
                props.poolQueueCapacity(),
                rejectedCount
        );
    }

    public static ThreadPoolExecutor buildBoundedMdcExecutor(String threadPrefix,
                                                             int coreSize,
                                                             int maxSize,
                                                             int queueCapacity,
                                                             AtomicLong rejectedCounter) {
        AtomicInteger threadSeq = new AtomicInteger(1);
        return new ThreadPoolExecutor(
                coreSize,
                maxSize,
                30L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                r -> {
                    Thread t = new Thread(r, threadPrefix + threadSeq.getAndIncrement());
                    t.setDaemon(false);
                    return t;
                },
                (r, executor) -> {
                    if (rejectedCounter != null) {
                        rejectedCounter.incrementAndGet();
                    }
                    throw new RejectedExecutionException("Seckill order queue saturated (backpressure)");
                }
        ) {
            @Override
            public void execute(Runnable command) {
                // Capture caller thread's MDC snapshot AT SUBMISSION TIME
                Map<String, String> callerContext = MDC.getCopyOfContextMap();
                super.execute(() -> {
                    Map<String, String> previous = MDC.getCopyOfContextMap();
                    if (callerContext != null) {
                        MDC.setContextMap(callerContext);
                    } else {
                        MDC.clear();
                    }
                    try {
                        command.run();
                    } finally {
                        if (previous != null) {
                            MDC.setContextMap(previous);
                        } else {
                            MDC.clear();
                        }
                    }
                });
            }
        };
    }

    public long getRejectedCount() {
        return rejectedCount.get();
    }
}
