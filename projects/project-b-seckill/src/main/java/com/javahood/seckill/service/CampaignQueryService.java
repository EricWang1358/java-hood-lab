package com.javahood.seckill.service;

import com.javahood.seckill.domain.CouponCampaign;
import com.javahood.seckill.engine.PreDeductEngine;
import com.javahood.seckill.repository.CouponCampaignRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Hotspot Campaign Cache with THREE foundational cache protections (Lab #25 + Module 6):
 * 1) Cache Breakdown (击穿) -> SingleFlight (`inFlight` CompletableFuture coalescing):
 *    When a hot campaign key expires and 100 threads miss simultaneously, ONLY 1 thread queries MySQL;
 *    the other 99 await the same CompletableFuture!
 * 2) Cache Penetration (穿透) -> Short-TTL Null Sentinel (`CachedEntry` with `campaign == null`):
 *    Requests for non-existent IDs (e.g., id = -9999) cache an empty sentinel for 5s so DB isn't hammered.
 * 3) Cache Avalanche (雪崩) -> Randomized TTL Jitter (`baseTtl + random(0..5s)`):
 *    Prevents batch-warmed campaigns from expiring at the exact same second.
 */
@Service
public class CampaignQueryService {

    public record CampaignDetailVO(
            Long id,
            String title,
            int totalStock,
            int realtimeRemainingStock,
            boolean active
    ) {
    }

    private record CachedEntry(CouponCampaign campaign, long expiresAtEpochMillis) {
        boolean isExpired(long nowMillis) {
            return nowMillis >= expiresAtEpochMillis;
        }
    }

    private static final long VALID_BASE_TTL_MS = 10_000L;
    private static final long NULL_SENTINEL_TTL_MS = 3_000L;

    private final CouponCampaignRepository campaignRepository;
    private final PreDeductEngine preDeductEngine;

    private final ConcurrentHashMap<Long, CachedEntry> localCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, CompletableFuture<Optional<CouponCampaign>>> inFlight = new ConcurrentHashMap<>();
    private final AtomicLong dbQueryCounter = new AtomicLong(0);

    public CampaignQueryService(CouponCampaignRepository campaignRepository,
                                PreDeductEngine preDeductEngine) {
        this.campaignRepository = campaignRepository;
        this.preDeductEngine = preDeductEngine;
    }

    public Optional<CampaignDetailVO> getCampaignDetail(Long campaignId) {
        long now = Instant.now().toEpochMilli();
        CachedEntry cached = localCache.get(campaignId);
        if (cached != null && !cached.isExpired(now)) {
            return toDetailVO(cached.campaign());
        }

        CompletableFuture<Optional<CouponCampaign>> ownFuture = new CompletableFuture<>();
        CompletableFuture<Optional<CouponCampaign>> existing = inFlight.putIfAbsent(campaignId, ownFuture);

        Optional<CouponCampaign> loaded;
        if (existing == null) {
            // Winner thread performs the single DB lookup
            try {
                dbQueryCounter.incrementAndGet();
                Optional<CouponCampaign> fromDb = campaignRepository.findById(campaignId);
                long ttl = fromDb.isPresent()
                        ? VALID_BASE_TTL_MS + ThreadLocalRandom.current().nextLong(5_000L)
                        : NULL_SENTINEL_TTL_MS;
                localCache.put(campaignId, new CachedEntry(fromDb.orElse(null), Instant.now().toEpochMilli() + ttl));
                ownFuture.complete(fromDb);
                loaded = fromDb;
            } catch (Throwable ex) {
                ownFuture.completeExceptionally(ex);
                throw ex;
            } finally {
                inFlight.remove(campaignId, ownFuture);
            }
        } else {
            // Coalesced follower threads wait on the winner's future without hitting DB
            loaded = existing.join();
        }

        return toDetailVO(loaded.orElse(null));
    }

    public void evictCache(Long campaignId) {
        localCache.remove(campaignId);
    }

    public void resetStatsAndCache() {
        localCache.clear();
        dbQueryCounter.set(0);
    }

    public long getDbQueryCount() {
        return dbQueryCounter.get();
    }

    private Optional<CampaignDetailVO> toDetailVO(CouponCampaign c) {
        if (c == null) {
            return Optional.empty();
        }
        return Optional.of(new CampaignDetailVO(
                c.getId(),
                c.getTitle(),
                c.getTotalStock(),
                preDeductEngine.remainingStock(c.getId()),
                c.isActive()
        ));
    }
}
