package com.monitor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisStatsService {

    private static final Logger log = LoggerFactory.getLogger(RedisStatsService.class);

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

    public void incrementStats(String appId, String type) {
        String key = keyPrefix + appId + ":" + type;
        try {
            redisTemplate.opsForHash().increment(key, "count", 1);
            redisTemplate.expire(key, statsTtl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Failed to increment stats for key={}", key, e);
        }
    }

    public Map<String, Long> getStats(String appId) {
        Map<String, Long> stats = new HashMap<>();
        String pattern = keyPrefix + appId + ":*";

        try {
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
            log.debug("Fetched stats for appId={}: {}", appId, stats);
        } catch (Exception e) {
            log.error("Failed to fetch stats for appId={}", appId, e);
        }
        return stats;
    }
}
