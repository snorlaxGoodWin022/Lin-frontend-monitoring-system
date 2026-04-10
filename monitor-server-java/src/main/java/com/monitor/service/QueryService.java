package com.monitor.service;

import com.monitor.model.BehaviorDocument;
import com.monitor.model.ErrorDocument;
import com.monitor.model.PerformanceDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class QueryService {

    private static final Logger log = LoggerFactory.getLogger(QueryService.class);
    private static final int MAX_LIMIT = 1000;

    private final MongoTemplate mongoTemplate;

    public QueryService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<PerformanceDocument> queryPerformance(String appId, String startTime, String endTime, int limit) {
        Query query = buildQuery(appId, null, startTime, endTime, limit);
        List<PerformanceDocument> results = mongoTemplate.find(query, PerformanceDocument.class);
        log.debug("Query performance returned {} results for appId={}", results.size(), appId);
        return results;
    }

    public List<ErrorDocument> queryErrors(String appId, String type, String startTime, String endTime, int limit) {
        Query query = buildQuery(appId, type, startTime, endTime, limit);
        List<ErrorDocument> results = mongoTemplate.find(query, ErrorDocument.class);
        log.debug("Query errors returned {} results for appId={}, type={}", results.size(), appId, type);
        return results;
    }

    public List<BehaviorDocument> queryBehavior(String appId, String type, String startTime, String endTime, int limit) {
        Query query = buildQuery(appId, type, startTime, endTime, limit);
        List<BehaviorDocument> results = mongoTemplate.find(query, BehaviorDocument.class);
        log.debug("Query behavior returned {} results for appId={}, type={}", results.size(), appId, type);
        return results;
    }

    private Query buildQuery(String appId, String type, String startTime, String endTime, int limit) {
        Query query = new Query();

        if (appId != null && !appId.isBlank()) {
            query.addCriteria(Criteria.where("appId").is(appId));
        }
        if (type != null && !type.isBlank()) {
            query.addCriteria(Criteria.where("type").is(type));
        }
        if (startTime != null || endTime != null) {
            Criteria timeCriteria = Criteria.where("timestamp");
            if (startTime != null) {
                timeCriteria.gte(new Date(parseTimestamp(startTime)));
            }
            if (endTime != null) {
                timeCriteria.lte(new Date(parseTimestamp(endTime)));
            }
            query.addCriteria(timeCriteria);
        }

        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));
        int effectiveLimit = Math.max(1, Math.min(limit, MAX_LIMIT));
        query.limit(effectiveLimit);

        log.debug("Built query: appId={}, type={}, startTime={}, endTime={}, limit={}",
                appId, type, startTime, endTime, limit);
        return query;
    }

    private long parseTimestamp(String ts) {
        try {
            long value = Long.parseLong(ts);
            if (value < 0) {
                throw new IllegalArgumentException("timestamp must be non-negative");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid timestamp format: " + ts);
        }
    }
}
