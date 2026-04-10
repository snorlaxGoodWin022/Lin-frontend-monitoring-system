package com.monitor.controller;

import com.monitor.model.BehaviorDocument;
import com.monitor.model.ErrorDocument;
import com.monitor.model.PerformanceDocument;
import com.monitor.service.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/query")
public class QueryController {

    private static final Logger log = LoggerFactory.getLogger(QueryController.class);
    private static final int MAX_LIMIT = 10000;

    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/performance")
    public ResponseEntity<?> queryPerformance(
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "100") int limit) {

        validateLimit(limit);
        validateTimestamps(startTime, endTime);

        List<PerformanceDocument> data = queryService.queryPerformance(appId, startTime, endTime, limit);
        log.debug("Queried performance: appId={}, limit={}, returned={}", appId, limit, data.size());
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @GetMapping("/errors")
    public ResponseEntity<?> queryErrors(
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "100") int limit) {

        validateLimit(limit);
        validateTimestamps(startTime, endTime);

        List<ErrorDocument> data = queryService.queryErrors(appId, type, startTime, endTime, limit);
        log.debug("Queried errors: appId={}, type={}, returned={}", appId, type, data.size());
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @GetMapping("/behavior")
    public ResponseEntity<?> queryBehavior(
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "100") int limit) {

        validateLimit(limit);
        validateTimestamps(startTime, endTime);

        List<BehaviorDocument> data = queryService.queryBehavior(appId, type, startTime, endTime, limit);
        log.debug("Queried behavior: appId={}, type={}, returned={}", appId, type, data.size());
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    private void validateLimit(int limit) {
        if (limit <= 0 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
        }
    }

    private void validateTimestamps(String startTime, String endTime) {
        if (startTime != null) {
            try {
                Long.parseLong(startTime);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("startTime must be a valid millisecond timestamp");
            }
        }
        if (endTime != null) {
            try {
                Long.parseLong(endTime);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("endTime must be a valid millisecond timestamp");
            }
        }
    }
}
