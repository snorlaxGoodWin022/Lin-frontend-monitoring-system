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

    private final MongoTemplate mongoTemplate;

    public QueryService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<PerformanceDocument> queryPerformance(String appId, String startTime, String endTime, int limit) {
        Query query = buildQuery(appId, null, startTime, endTime, limit);
        return mongoTemplate.find(query, PerformanceDocument.class);
    }

    public List<ErrorDocument> queryErrors(String appId, String type, String startTime, String endTime, int limit) {
        Query query = buildQuery(appId, type, startTime, endTime, limit);
        return mongoTemplate.find(query, ErrorDocument.class);
    }

    public List<BehaviorDocument> queryBehavior(String appId, String type, String startTime, String endTime, int limit) {
        Query query = buildQuery(appId, type, startTime, endTime, limit);
        return mongoTemplate.find(query, BehaviorDocument.class);
    }

    private Query buildQuery(String appId, String type, String startTime, String endTime, int limit) {
        Query query = new Query();

        if (appId != null && !appId.isEmpty()) {
            query.addCriteria(Criteria.where("appId").is(appId));
        }
        if (type != null && !type.isEmpty()) {
            query.addCriteria(Criteria.where("type").is(type));
        }
        if (startTime != null || endTime != null) {
            Criteria timeCriteria = Criteria.where("timestamp");
            if (startTime != null) timeCriteria.gte(new Date(Long.parseLong(startTime)));
            if (endTime != null) timeCriteria.lte(new Date(Long.parseLong(endTime)));
            query.addCriteria(timeCriteria);
        }

        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));
        query.limit(limit);

        log.debug("Built query: appId={}, type={}, startTime={}, endTime={}, limit={}",
                appId, type, startTime, endTime, limit);
        return query;
    }
}
