package com.monitor.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    private final MongoTemplate mongoTemplate;
    private final StringRedisTemplate redisTemplate;

    public HealthController(MongoTemplate mongoTemplate, StringRedisTemplate redisTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        result.put("timestamp", System.currentTimeMillis());

        try {
            mongoTemplate.executeCommand("{ ping: 1 }");
            result.put("mongodb", "up");
        } catch (Exception e) {
            log.error("MongoDB health check failed", e);
            result.put("mongodb", "down");
            result.put("status", "degraded");
        }

        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            result.put("redis", "up");
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            result.put("redis", "down");
            result.put("status", "degraded");
        }

        return result;
    }
}
