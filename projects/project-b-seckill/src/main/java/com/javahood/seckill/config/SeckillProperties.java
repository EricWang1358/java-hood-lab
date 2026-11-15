package com.javahood.seckill.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.seckill")
public record SeckillProperties(
        int shards,
        int poolCoreSize,
        int poolMaxSize,
        int poolQueueCapacity,
        long outboxRelayFixedDelayMs
) {
}
