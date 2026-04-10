package com.monitor.service;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Dashboard 聚合统计服务
 * 移植自 monitor-server/src/api/dashboard.js
 */
@Service
public class DashboardService {

    private final MongoTemplate mongoTemplate;
    private final RedisStatsService redisStatsService;

    public DashboardService(MongoTemplate mongoTemplate, RedisStatsService redisStatsService) {
        this.mongoTemplate = mongoTemplate;
        this.redisStatsService = redisStatsService;
    }

    /**
     * 从 Redis 获取实时统计数据
     */
    public Map<String, Long> getStats(String appId) {
        return redisStatsService.getStats(appId);
    }

    /**
     * 性能趋势：按小时聚合 Web Vitals 平均值
     */
    public List<Map> getPerformanceTrend(String appId, int hours) {
        Date startTime = new Date(System.currentTimeMillis() - hours * 3600 * 1000L);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("appId").is(appId)
                        .and("timestamp").gte(startTime)),
                Aggregation.project()
                        .andExpression("year(timestamp)").as("year")
                        .andExpression("month(timestamp)").as("month")
                        .andExpression("dayOfMonth(timestamp)").as("day")
                        .andExpression("hour(timestamp)").as("hour")
                        .and("metrics").as("metrics"),
                Aggregation.group(Fields.fields().and("year").and("month").and("day").and("hour"))
                        .avg("metrics.fcp").as("avgFCP")
                        .avg("metrics.lcp").as("avgLCP")
                        .avg("metrics.fid").as("avgFID")
                        .avg("metrics.cls").as("avgCLS")
                        .count().as("count"),
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "_id"))
        );

        return mongoTemplate.aggregate(aggregation, "performances", Map.class).getMappedResults();
    }

    /**
     * 错误分布：按错误类型分组统计
     */
    public List<Map> getErrorDistribution(String appId, int hours) {
        Date startTime = new Date(System.currentTimeMillis() - hours * 3600 * 1000L);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("appId").is(appId)
                        .and("timestamp").gte(startTime)),
                Aggregation.group("type").count().as("count"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "count"))
        );

        return mongoTemplate.aggregate(aggregation, "errors", Map.class).getMappedResults();
    }

    /**
     * PV/UV 统计：按小时聚合页面浏览量和独立访客数
     */
    public Map<String, List<Map>> getPvUv(String appId, int hours) {
        Date startTime = new Date(System.currentTimeMillis() - hours * 3600 * 1000L);

        // PV 聚合：按小时统计 page_view 总数
        Aggregation pvAgg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("appId").is(appId)
                        .and("type").is("page_view")
                        .and("timestamp").gte(startTime)),
                Aggregation.project()
                        .andExpression("year(timestamp)").as("year")
                        .andExpression("month(timestamp)").as("month")
                        .andExpression("dayOfMonth(timestamp)").as("day")
                        .andExpression("hour(timestamp)").as("hour"),
                Aggregation.group(Fields.fields().and("year").and("month").and("day").and("hour"))
                        .count().as("pv"),
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "_id"))
        );
        List<Map> pvData = mongoTemplate.aggregate(pvAgg, "behaviors", Map.class).getMappedResults();

        // UV 聚合：先按小时+userId 去重，再按小时计数
        Aggregation uvAgg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("appId").is(appId)
                        .and("type").is("page_view")
                        .and("timestamp").gte(startTime)),
                Aggregation.project()
                        .andExpression("year(timestamp)").as("year")
                        .andExpression("month(timestamp)").as("month")
                        .andExpression("dayOfMonth(timestamp)").as("day")
                        .andExpression("hour(timestamp)").as("hour")
                        .and("userId").as("userId"),
                Aggregation.group(Fields.fields().and("year").and("month").and("day").and("hour").and("userId")),
                Aggregation.group("_id.year", "_id.month", "_id.day", "_id.hour")
                        .count().as("uv"),
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "_id"))
        );
        List<Map> uvData = mongoTemplate.aggregate(uvAgg, "behaviors", Map.class).getMappedResults();

        Map<String, List<Map>> result = new HashMap<>();
        result.put("pvData", pvData);
        result.put("uvData", uvData);
        return result;
    }
}
