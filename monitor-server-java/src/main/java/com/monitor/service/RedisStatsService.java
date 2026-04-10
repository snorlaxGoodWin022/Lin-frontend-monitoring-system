package com.monitor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 实时统计服务
 * Key 格式：stats:{appId}:{type}，Hash field：count，TTL：3600s
 */
@Service
public class RedisStatsService {

    private final StringRedisTemplate redisTemplate;
    private final String keyPrefix;
    private final long statsTtl;

    public RedisStatsService(StringRedisTemplate redisTemplate,
                             @Value("${monitor.redis.stats-key-prefix:stats:}") String keyPrefix,
                             @Value("${monitor.redis.stats-ttl:3600}") long statsTtl) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = keyPrefix;
        this.statsTtl = statsTtl;
    }

    /**
     * 递增某个 app 某种类型的事件计数
     */
    public void incrementStats(String appId, String type) {
        String key = keyPrefix + appId + ":" + type;
        redisTemplate.opsForHash().increment(key, "count", 1);
        redisTemplate.expire(key, statsTtl, TimeUnit.SECONDS);
    }

    /**
     * 获取某个 app 所有类型的实时统计
     * 返回 Map<type, count>
     */
    public Map<String, Long> getStats(String appId) {
        Map<String, Long> stats = new HashMap<>();
        String pattern = keyPrefix + appId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null) return stats;

        int prefixLength = (keyPrefix + appId + ":").length();
        for (String key : keys) {
            String type = key.substring(prefixLength);
            Object count = redisTemplate.opsForHash().get(key, "count");
            if (count != null) {
                stats.put(type, Long.parseLong(count.toString()));
            }
        }
        return stats;
    }
}
