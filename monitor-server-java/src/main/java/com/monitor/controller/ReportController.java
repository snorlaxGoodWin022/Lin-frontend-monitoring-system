package com.monitor.controller;

import com.monitor.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据上报接口
 * 对应 Node.js: POST /api/report
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<?> report(@RequestBody Object rawBody) {
        // SDK 可能发送单个对象或数组，统一转为 List
        List<Map<String, Object>> events;
        if (rawBody instanceof List) {
            events = (List<Map<String, Object>>) rawBody;
        } else {
            events = List.of((Map<String, Object>) rawBody);
        }

        int count = reportService.processReport(events);
        return ResponseEntity.ok(Map.of("success", true, "count", count));
    }
}
