package com.monitor.controller;

import com.monitor.model.BehaviorDocument;
import com.monitor.model.ErrorDocument;
import com.monitor.model.PerformanceDocument;
import com.monitor.service.QueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据查询接口
 * 对应 Node.js: GET /api/query/performance|errors|behavior
 */
@RestController
@RequestMapping("/api/query")
public class QueryController {

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

        List<PerformanceDocument> data = queryService.queryPerformance(appId, startTime, endTime, limit);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @GetMapping("/errors")
    public ResponseEntity<?> queryErrors(
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "100") int limit) {

        List<ErrorDocument> data = queryService.queryErrors(appId, type, startTime, endTime, limit);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @GetMapping("/behavior")
    public ResponseEntity<?> queryBehavior(
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "100") int limit) {

        List<BehaviorDocument> data = queryService.queryBehavior(appId, type, startTime, endTime, limit);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }
}
