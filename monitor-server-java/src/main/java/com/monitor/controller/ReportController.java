package com.monitor.controller;

import com.monitor.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<?> report(@RequestBody Object rawBody) {
        if (rawBody == null) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }

        List<Map<String, Object>> events;
        if (rawBody instanceof List) {
            events = (List<Map<String, Object>>) rawBody;
        } else if (rawBody instanceof Map) {
            events = List.of((Map<String, Object>) rawBody);
        } else {
            throw new IllegalArgumentException("Invalid request body format");
        }

        int count = reportService.processReport(events);
        log.info("Processed {} monitor events", count);
        return ResponseEntity.ok(Map.of("success", true, "count", count));
    }
}
