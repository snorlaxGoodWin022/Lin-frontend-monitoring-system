package com.monitor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
    private static final String PERFORMANCE_COLLECTION = "performances";
    private static final String ERROR_COLLECTION = "errors";
    private static final String BEHAVIOR_COLLECTION = "behaviors";

    private final MongoTemplate mongoTemplate;
    private final RedisStatsService redisStatsService;

    public DashboardService(MongoTemplate mongoTemplate, RedisStatsService redisStatsService) {
        this.mongoTemplate = mongoTemplate;
        this.redisStatsService = redisStatsService;
    }

    public Map<String, Long> getStats(String appId) {
        log.debug("Fetching stats for appId={}", appId);
        return redisStatsService.getStats(appId);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getPerformanceTrend(String appId, int hours) {
        Date startTime = new Date(System.currentTimeMillis() - hours * 3600 * 1000L);
        log.debug("Fetching performance trend: appId={}, hours={}", appId, hours);

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

        List<Map<String, Object>> results = (List) mongoTemplate.aggregate(aggregation, PERFORMANCE_COLLECTION, Map.class).getMappedResults();
        log.debug("Performance trend returned {} data points", results.size());
        return results;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getErrorDistribution(String appId, int hours) {
        Date startTime = new Date(System.currentTimeMillis() - hours * 3600 * 1000L);
        log.debug("Fetching error distribution: appId={}, hours={}", appId, hours);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("appId").is(appId)
                        .and("timestamp").gte(startTime)),
                Aggregation.group("type").count().as("count"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "count"))
        );

        List<Map<String, Object>> results = (List) mongoTemplate.aggregate(aggregation, ERROR_COLLECTION, Map.class).getMappedResults();
        log.debug("Error distribution returned {} types", results.size());
        return results;
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<Map<String, Object>>> getPvUv(String appId, int hours) {
        Date startTime = new Date(System.currentTimeMillis() - hours * 3600 * 1000L);
        log.debug("Fetching PV/UV: appId={}, hours={}", appId, hours);

        // PV 聚合
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
        List<Map<String, Object>> pvData = (List) mongoTemplate.aggregate(pvAgg, BEHAVIOR_COLLECTION, Map.class).getMappedResults();

        // UV 聚合
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
        List<Map<String, Object>> uvData = (List) mongoTemplate.aggregate(uvAgg, BEHAVIOR_COLLECTION, Map.class).getMappedResults();

        log.debug("PV/UV returned {} pv points, {} uv points", pvData.size(), uvData.size());

        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        result.put("pvData", pvData);
        result.put("uvData", uvData);
        return result;
    }
}
