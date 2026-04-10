package com.monitor.controller;

import com.monitor.exception.GlobalExceptionHandler;
import com.monitor.service.DashboardService;
import com.monitor.service.QueryService;
import com.monitor.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ControllerTest {

    @Mock ReportService reportService;
    @Mock QueryService queryService;
    @Mock DashboardService dashboardService;

    // ========== HealthController ==========

    @Test
    @DisplayName("GET /health 返回 200 和 status:ok")
    void health() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new HealthController()).build();

        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.timestamp").isNumber());
    }

    // ========== ReportController ==========

    @Test
    @DisplayName("POST /api/report 单对象返回成功")
    void reportSingleObject() throws Exception {
        when(reportService.processReport(anyList())).thenReturn(1);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ReportController(reportService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mvc.perform(post("/api/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"test\",\"timestamp\":1712600000000,\"type\":\"performance\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @DisplayName("POST /api/report 数组返回成功")
    void reportArray() throws Exception {
        when(reportService.processReport(anyList())).thenReturn(2);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ReportController(reportService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mvc.perform(post("/api/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"appId\":\"test\",\"timestamp\":1,\"type\":\"performance\"}," +
                                "{\"appId\":\"test\",\"timestamp\":2,\"type\":\"error\"}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    @DisplayName("POST /api/report 验证失败返回 400")
    void reportValidationError() throws Exception {
        when(reportService.processReport(anyList()))
                .thenThrow(new IllegalArgumentException("appId is required"));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ReportController(reportService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mvc.perform(post("/api/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"timestamp\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("appId is required"));
    }

    @Test
    @DisplayName("POST /api/report 服务异常返回 500")
    void reportInternalError() throws Exception {
        when(reportService.processReport(anyList()))
                .thenThrow(new RuntimeException("DB down"));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ReportController(reportService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mvc.perform(post("/api/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"appId\":\"test\",\"timestamp\":1,\"type\":\"performance\"}]"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal server error"));
    }

    // ========== QueryController ==========

    @Test
    @DisplayName("GET /api/query/performance 返回成功")
    void queryPerformance() throws Exception {
        when(queryService.queryPerformance(eq("app1"), isNull(), isNull(), eq(100)))
                .thenReturn(List.of());

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new QueryController(queryService)).build();

        mvc.perform(get("/api/query/performance").param("appId", "app1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/query/errors 支持 type 过滤")
    void queryErrors() throws Exception {
        when(queryService.queryErrors("app1", "jsError", null, null, 100))
                .thenReturn(List.of());

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new QueryController(queryService)).build();

        mvc.perform(get("/api/query/errors")
                        .param("appId", "app1")
                        .param("type", "jsError"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/query/behavior 返回成功")
    void queryBehavior() throws Exception {
        when(queryService.queryBehavior("app1", "click", null, null, 50))
                .thenReturn(List.of());

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new QueryController(queryService)).build();

        mvc.perform(get("/api/query/behavior")
                        .param("appId", "app1")
                        .param("type", "click")
                        .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ========== DashboardController ==========

    @Test
    @DisplayName("GET /api/dashboard/stats 返回统计")
    void dashboardStats() throws Exception {
        when(dashboardService.getStats("app1")).thenReturn(Map.of("performance", 10L));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new DashboardController(dashboardService)).build();

        mvc.perform(get("/api/dashboard/stats").param("appId", "app1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.stats.performance").value(10));
    }

    @Test
    @DisplayName("GET /api/dashboard/performance-trend 返回趋势数据")
    void dashboardPerformanceTrend() throws Exception {
        when(dashboardService.getPerformanceTrend("app1", 24)).thenReturn(List.of());

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new DashboardController(dashboardService)).build();

        mvc.perform(get("/api/dashboard/performance-trend").param("appId", "app1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/dashboard/error-distribution 返回分布数据")
    void dashboardErrorDistribution() throws Exception {
        when(dashboardService.getErrorDistribution("app1", 24)).thenReturn(List.of());

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new DashboardController(dashboardService)).build();

        mvc.perform(get("/api/dashboard/error-distribution").param("appId", "app1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/dashboard/pv-uv 返回 pvData 和 uvData")
    void dashboardPvUv() throws Exception {
        Map<String, List<Map>> pvUv = Map.of("pvData", List.of(), "uvData", List.of());
        when(dashboardService.getPvUv("app1", 24)).thenReturn(pvUv);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new DashboardController(dashboardService)).build();

        mvc.perform(get("/api/dashboard/pv-uv").param("appId", "app1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.pvData").isArray())
                .andExpect(jsonPath("$.uvData").isArray());
    }
}
