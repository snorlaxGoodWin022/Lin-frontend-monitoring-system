package com.monitor.controller;

import com.monitor.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Dashboard 聚合统计接口
 * 对应 Node.js: GET /api/dashboard/stats|performance-trend|error-distribution|pv-uv
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@RequestParam String appId) {
        Map<String, Long> stats = dashboardService.getStats(appId);
        return ResponseEntity.ok(Map.of("success", true, "stats", stats));
    }

    @GetMapping("/performance-trend")
    public ResponseEntity<?> getPerformanceTrend(
            @RequestParam String appId,
            @RequestParam(defaultValue = "24") int hours) {
        List<Map> data = dashboardService.getPerformanceTrend(appId, hours);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @GetMapping("/error-distribution")
    public ResponseEntity<?> getErrorDistribution(
            @RequestParam String appId,
            @RequestParam(defaultValue = "24") int hours) {
        List<Map> data = dashboardService.getErrorDistribution(appId, hours);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @GetMapping("/pv-uv")
    public ResponseEntity<?> getPvUv(
            @RequestParam String appId,
            @RequestParam(defaultValue = "24") int hours) {
        Map<String, List<Map>> pvUv = dashboardService.getPvUv(appId, hours);
        return ResponseEntity.ok(Map.of("success", true, "pvData", pvUv.get("pvData"), "uvData", pvUv.get("uvData")));
    }
}
