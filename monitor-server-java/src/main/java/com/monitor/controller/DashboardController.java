package com.monitor.controller;

import com.monitor.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);
    private static final int MAX_HOURS = 720; // 30 days

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@RequestParam String appId) {
        validateAppId(appId);

        Map<String, Long> stats = dashboardService.getStats(appId);
        log.debug("Fetched stats for appId={}: {}", appId, stats);
        return ResponseEntity.ok(Map.of("success", true, "stats", stats));
    }

    @GetMapping("/performance-trend")
    public ResponseEntity<?> getPerformanceTrend(
            @RequestParam String appId,
            @RequestParam(defaultValue = "24") int hours) {
        validateAppId(appId);
        validateHours(hours);

        List<Map<String, Object>> data = dashboardService.getPerformanceTrend(appId, hours);
        log.debug("Fetched performance trend for appId={}, hours={}", appId, hours);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @GetMapping("/error-distribution")
    public ResponseEntity<?> getErrorDistribution(
            @RequestParam String appId,
            @RequestParam(defaultValue = "24") int hours) {
        validateAppId(appId);
        validateHours(hours);

        List<Map<String, Object>> data = dashboardService.getErrorDistribution(appId, hours);
        log.debug("Fetched error distribution for appId={}, hours={}", appId, hours);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @GetMapping("/pv-uv")
    public ResponseEntity<?> getPvUv(
            @RequestParam String appId,
            @RequestParam(defaultValue = "24") int hours) {
        validateAppId(appId);
        validateHours(hours);

        Map<String, List<Map<String, Object>>> pvUv = dashboardService.getPvUv(appId, hours);
        log.debug("Fetched PV/UV for appId={}, hours={}", appId, hours);
        return ResponseEntity.ok(Map.of("success", true, "pvData", pvUv.get("pvData"), "uvData", pvUv.get("uvData")));
    }

    private void validateAppId(String appId) {
        if (appId == null || appId.trim().isEmpty()) {
            throw new IllegalArgumentException("appId cannot be empty");
        }
    }

    private void validateHours(int hours) {
        if (hours <= 0 || hours > MAX_HOURS) {
            throw new IllegalArgumentException("hours must be between 1 and " + MAX_HOURS);
        }
    }
}
