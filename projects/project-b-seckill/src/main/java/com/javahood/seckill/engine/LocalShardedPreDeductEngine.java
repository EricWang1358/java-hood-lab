package com.javahood.seckill.engine;

import com.javahood.seckill.config.SeckillProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * In-JVM sharded CAS pre-deduction engine (Lab #26 lab.shardstock + Lab #23 lab.userlock).
 * Distributes stock into N shards and uses linear probing across shards when a bucket empties,
 * reducing contention by N-fold while guaranteeing zero oversell and 1-per-user limit.
 */
@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false", matchIfMissing = true)
public class LocalShardedPreDeductEngine implements PreDeductEngine {

    private final int shardCount;
    private final ConcurrentHashMap<Long, AtomicIntegerArray> campaignShards = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Set<Long>> claimedUsers = new ConcurrentHashMap<>();

    public LocalShardedPreDeductEngine(SeckillProperties props) {
        this.shardCount = Math.max(1, props.shards());
    }

    @Override
    public void initCampaignStock(Long campaignId, int totalStock) {
        AtomicIntegerArray shards = new AtomicIntegerArray(shardCount);
        int base = totalStock / shardCount;
        int rem = totalStock % shardCount;
        for (int i = 0; i < shardCount; i++) {
            shards.set(i, base + (i < rem ? 1 : 0));
        }
        campaignShards.put(campaignId, shards);
        claimedUsers.put(campaignId, ConcurrentHashMap.newKeySet());
    }

    @Override
    public PreDeductResult tryPreDeduct(Long campaignId, Long userId) {
        AtomicIntegerArray shards = campaignShards.get(campaignId);
        Set<Long> users = claimedUsers.get(campaignId);
        if (shards == null || users == null) {
            return PreDeductResult.soldOut();
        }

        // Per-user atomic guard (Lab #23)
        if (!users.add(userId)) {
            return PreDeductResult.alreadyClaimed();
        }

        int start = Math.floorMod(userId.hashCode(), shardCount);
        for (int offset = 0; offset < shardCount; offset++) {
            int idx = (start + offset) % shardCount;
            if (tryDecrementBucket(shards, idx)) {
                return PreDeductResult.success(idx);
            }
        }

        // Sold out across all shards: release user reservation so state stays consistent
        users.remove(userId);
        return PreDeductResult.soldOut();
    }

    @Override
    public void rollback(Long campaignId, Long userId, int shardIndex) {
        AtomicIntegerArray shards = campaignShards.get(campaignId);
        if (shards != null && shardIndex >= 0 && shardIndex < shards.length()) {
            shards.incrementAndGet(shardIndex);
        }
        Set<Long> users = claimedUsers.get(campaignId);
        if (users != null) {
            users.remove(userId);
        }
    }

    @Override
    public void restoreStockAfterClose(Long campaignId) {
        AtomicIntegerArray shards = campaignShards.get(campaignId);
        if (shards != null) {
            int bucket = ThreadLocalRandom.current().nextInt(shards.length());
            shards.incrementAndGet(bucket);
        }
    }

    @Override
    public int remainingStock(Long campaignId) {
        AtomicIntegerArray shards = campaignShards.get(campaignId);
        if (shards == null) {
            return 0;
        }
        int sum = 0;
        for (int i = 0; i < shards.length(); i++) {
            sum += shards.get(i);
        }
        return sum;
    }

    private boolean tryDecrementBucket(AtomicIntegerArray shards, int idx) {
        while (true) {
            int cur = shards.get(idx);
            if (cur <= 0) {
                return false;
            }
            if (shards.compareAndSet(idx, cur, cur - 1)) {
                return true;
            }
        }
    }
}
