-- KEYS[1] = stock key (e.g. seckill:stock:{campaignId}:{shard})
-- KEYS[2] = claimed users set key (e.g. seckill:users:{campaignId})
-- ARGV[1] = userId
-- Return codes:
--   1  = success
--   0  = sold out in this shard
--  -1  = already claimed by this user

if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
    return -1
end

local stock = tonumber(redis.call('GET', KEYS[1]) or '0')
if stock <= 0 then
    return 0
end

redis.call('DECR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1])
return 1
