package com.monitor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitor.model.BehaviorDocument;
import com.monitor.model.ErrorDocument;
import com.monitor.model.PerformanceDocument;
import com.monitor.repository.BehaviorRepository;
import com.monitor.repository.ErrorRepository;
import com.monitor.repository.PerformanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 上报数据处理服务
 * 移植自 monitor-server/src/api/report.js
 */
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private static final Set<String> BEHAVIOR_TYPES = Set.of(
            "click", "track", "scroll", "page_view", "page_leave", "exposure"
    );

    private final DataProcessor dataProcessor;
    private final RedisStatsService redisStatsService;
    private final PerformanceRepository performanceRepo;
    private final ErrorRepository errorRepo;
    private final BehaviorRepository behaviorRepo;
    private final ObjectMapper objectMapper;

    public ReportService(DataProcessor dataProcessor,
                         RedisStatsService redisStatsService,
                         PerformanceRepository performanceRepo,
                         ErrorRepository errorRepo,
                         BehaviorRepository behaviorRepo,
                         ObjectMapper objectMapper) {
        this.dataProcessor = dataProcessor;
        this.redisStatsService = redisStatsService;
        this.performanceRepo = performanceRepo;
        this.errorRepo = errorRepo;
        this.behaviorRepo = behaviorRepo;
        this.objectMapper = objectMapper;
    }

    /**
     * 处理上报数据：验证 → 清洗 → 分类 → 存储 → 更新 Redis
     */
    public int processReport(List<Map<String, Object>> events) {
        // 1. 验证
        String error = dataProcessor.validate(events);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        // 2. 清洗每条数据
        events.forEach(dataProcessor::cleanData);

        // 3. 按类型分类
        List<PerformanceDocument> perfList = new ArrayList<>();
        List<ErrorDocument> errorList = new ArrayList<>();
        List<BehaviorDocument> behaviorList = new ArrayList<>();

        for (Map<String, Object> event : events) {
            String type = (String) event.get("type");
            if (type == null) continue;

            if ("performance".equals(type) || "api".equals(type)) {
                PerformanceDocument doc = objectMapper.convertValue(event, PerformanceDocument.class);
                perfList.add(doc);
            } else if ("error".equals(type)) {
                ErrorDocument doc = objectMapper.convertValue(event, ErrorDocument.class);
                errorList.add(doc);
            } else if (BEHAVIOR_TYPES.contains(type)) {
                BehaviorDocument doc = objectMapper.convertValue(event, BehaviorDocument.class);
                behaviorList.add(doc);
            }
        }

        // 4. 并行批量保存到 MongoDB
        try {
            if (!perfList.isEmpty()) performanceRepo.saveAll(perfList);
            if (!errorList.isEmpty()) errorRepo.saveAll(errorList);
            if (!behaviorList.isEmpty()) behaviorRepo.saveAll(behaviorList);
        } catch (Exception e) {
            log.error("Failed to save report data", e);
            throw new RuntimeException("Internal server error");
        }

        // 5. 更新 Redis 实时统计
        for (Map<String, Object> event : events) {
            String appId = (String) event.get("appId");
            String type = (String) event.get("type");
            if (appId != null && type != null) {
                try {
                    redisStatsService.incrementStats(appId, type);
                } catch (Exception e) {
                    log.warn("Failed to update Redis stats", e);
                }
            }
        }

        return events.size();
    }
}
