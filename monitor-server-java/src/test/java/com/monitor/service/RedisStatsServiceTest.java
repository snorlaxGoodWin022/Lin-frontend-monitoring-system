package com.monitor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisStatsServiceTest {

    @Mock StringRedisTemplate redisTemplate;
    @Mock HashOperations<String, Object, Object> hashOps;

    RedisStatsService redisStatsService;

    @BeforeEach
    void setUp() {
        redisStatsService = new RedisStatsService(redisTemplate, "stats:", 3600);
    }

    @Test
    @DisplayName("incrementStats 调用 hash increment 并设置过期时间")
    void incrementStats() {
        when(redisTemplate.opsForHash()).thenReturn(hashOps);

        redisStatsService.incrementStats("app1", "performance");

        verify(hashOps).increment("stats:app1:performance", "count", 1);
        verify(redisTemplate).expire("stats:app1:performance", 3600, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("getStats 从 Redis 获取所有类型的计数")
    void getStats() {
        when(redisTemplate.keys("stats:app1:*")).thenReturn(Set.of(
                "stats:app1:performance", "stats:app1:error"
        ));
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
        when(hashOps.get("stats:app1:performance", "count")).thenReturn("10");
        when(hashOps.get("stats:app1:error", "count")).thenReturn("3");

        Map<String, Long> result = redisStatsService.getStats("app1");

        assertThat(result)
                .containsEntry("performance", 10L)
                .containsEntry("error", 3L);
    }

    @Test
    @DisplayName("getStats 无数据时返回空 Map")
    void getStatsEmpty() {
        when(redisTemplate.keys("stats:app1:*")).thenReturn(null);

        Map<String, Long> result = redisStatsService.getStats("app1");

        assertThat(result).isEmpty();
    }
}
