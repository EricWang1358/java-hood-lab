package com.javahood.seckill.engine;

import com.javahood.seckill.config.SeckillProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
public class RedisLuaPreDeductEngine implements PreDeductEngine {

    private final StringRedisTemplate redisTemplate;
    private final int shardCount;
    private final DefaultRedisScript<Long> preDeductScript;

    public RedisLuaPreDeductEngine(StringRedisTemplate redisTemplate, SeckillProperties props) {
        this.redisTemplate = redisTemplate;
        this.shardCount = Math.max(1, props.shards());
        this.preDeductScript = new DefaultRedisScript<>();
        this.preDeductScript.setLocation(new ClassPathResource("scripts/seckill_pre_deduct.lua"));
        this.preDeductScript.setResultType(Long.class);
    }

    @Override
    public void initCampaignStock(Long campaignId, int totalStock) {
        int base = totalStock / shardCount;
        int rem = totalStock % shardCount;
        for (int i = 0; i < shardCount; i++) {
            int val = base + (i < rem ? 1 : 0);
            redisTemplate.opsForValue().set(stockKey(campaignId, i), String.valueOf(val));
        }
        redisTemplate.delete(usersKey(campaignId));
    }

    @Override
    public PreDeductResult tryPreDeduct(Long campaignId, Long userId) {
        int start = (int) (Math.abs(userId.hashCode()) % shardCount);
        String uKey = usersKey(campaignId);
        for (int offset = 0; offset < shardCount; offset++) {
            int idx = (start + offset) % shardCount;
            Long res = redisTemplate.execute(
                    preDeductScript,
                    List.of(stockKey(campaignId, idx), uKey),
                    String.valueOf(userId)
            );
            if (res == null) {
                continue;
            }
            if (res == -1L) {
                return PreDeductResult.alreadyClaimed();
            }
            if (res == 1L) {
                return PreDeductResult.success(idx);
            }
        }
        return PreDeductResult.soldOut();
    }

    @Override
    public void rollback(Long campaignId, Long userId, int shardIndex) {
        if (shardIndex >= 0 && shardIndex < shardCount) {
            redisTemplate.opsForValue().increment(stockKey(campaignId, shardIndex));
        }
        redisTemplate.opsForSet().remove(usersKey(campaignId), String.valueOf(userId));
    }

    @Override
    public int remainingStock(Long campaignId) {
        int sum = 0;
        for (int i = 0; i < shardCount; i++) {
            String val = redisTemplate.opsForValue().get(stockKey(campaignId, i));
            if (val != null) {
                sum += Integer.parseInt(val);
            }
        }
        return sum;
    }

    private String stockKey(Long campaignId, int shard) {
        return "seckill:stock:" + campaignId + ":" + shard;
    }

    private String usersKey(Long campaignId) {
        return "seckill:users:" + campaignId;
    }
}
