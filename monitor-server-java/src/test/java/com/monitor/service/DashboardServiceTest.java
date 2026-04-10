package com.monitor.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock MongoTemplate mongoTemplate;
    @Mock RedisStatsService redisStatsService;

    @InjectMocks DashboardService dashboardService;

    @Test
    @DisplayName("getStats 委托给 RedisStatsService")
    void getStats() {
        Map<String, Long> expected = Map.of("performance", 10L, "error", 2L);
        when(redisStatsService.getStats("app1")).thenReturn(expected);

        Map<String, Long> result = dashboardService.getStats("app1");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("getPerformanceTrend 调用 MongoTemplate.aggregate")
    void getPerformanceTrend() {
        AggregationResults<Map> mockResults = new AggregationResults<>(List.of(), new Document());
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("performances"), eq(Map.class)))
                .thenReturn(mockResults);

        List<Map> result = dashboardService.getPerformanceTrend("app1", 24);

        assertThat(result).isEmpty();
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("performances"), eq(Map.class));
    }

    @Test
    @DisplayName("getErrorDistribution 调用 MongoTemplate.aggregate")
    void getErrorDistribution() {
        Map<String, Object> errorStat = new HashMap<>();
        errorStat.put("_id", "jsError");
        errorStat.put("count", 5);
        AggregationResults<Map> mockResults = new AggregationResults<>(List.of(errorStat), new Document());
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("errors"), eq(Map.class)))
                .thenReturn(mockResults);

        List<Map> result = dashboardService.getErrorDistribution("app1", 24);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsEntry("_id", "jsError");
    }

    @Test
    @DisplayName("getPvUv 返回 pvData 和 uvData")
    void getPvUv() {
        AggregationResults<Map> emptyResults = new AggregationResults<>(List.of(), new Document());
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("behaviors"), eq(Map.class)))
                .thenReturn(emptyResults);

        Map<String, List<Map>> result = dashboardService.getPvUv("app1", 24);

        assertThat(result).containsKeys("pvData", "uvData");
    }
}
