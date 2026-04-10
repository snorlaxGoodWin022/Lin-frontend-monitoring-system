package com.monitor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitor.model.BehaviorDocument;
import com.monitor.model.ErrorDocument;
import com.monitor.model.PerformanceDocument;
import com.monitor.repository.BehaviorRepository;
import com.monitor.repository.ErrorRepository;
import com.monitor.repository.PerformanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock DataProcessor dataProcessor;
    @Mock RedisStatsService redisStatsService;
    @Mock PerformanceRepository performanceRepo;
    @Mock ErrorRepository errorRepo;
    @Mock BehaviorRepository behaviorRepo;

    ObjectMapper objectMapper = new ObjectMapper();

    ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(dataProcessor, redisStatsService,
                performanceRepo, errorRepo, behaviorRepo, objectMapper);
    }

    private Map<String, Object> event(String type) {
        Map<String, Object> e = new HashMap<>();
        e.put("appId", "test-app");
        e.put("timestamp", 1712600000000L);
        e.put("type", type);
        return e;
    }

    // ========== 验证阶段 ==========

    @Test
    @DisplayName("验证失败时抛 IllegalArgumentException")
    void validationFails() {
        when(dataProcessor.validate(any())).thenReturn("appId is required");

        assertThatThrownBy(() -> reportService.processReport(List.of(new HashMap<>())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("appId is required");
    }

    // ========== 分类阶段 ==========

    @Test
    @DisplayName("performance 类型存入 PerformanceRepository")
    void performanceType() {
        when(dataProcessor.validate(any())).thenReturn(null);
        List<Map<String, Object>> events = List.of(event("performance"));

        int count = reportService.processReport(events);

        assertThat(count).isEqualTo(1);
        verify(performanceRepo).saveAll(anyList());
        verify(errorRepo, never()).saveAll(anyList());
        verify(behaviorRepo, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("api 类型存入 PerformanceRepository")
    void apiType() {
        when(dataProcessor.validate(any())).thenReturn(null);
        reportService.processReport(List.of(event("api")));

        verify(performanceRepo).saveAll(anyList());
    }

    @Test
    @DisplayName("error 类型存入 ErrorRepository")
    void errorType() {
        when(dataProcessor.validate(any())).thenReturn(null);
        reportService.processReport(List.of(event("error")));

        verify(errorRepo).saveAll(anyList());
        verify(performanceRepo, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("click/track/scroll/page_view/page_leave/exposure 存入 BehaviorRepository")
    void behaviorTypes() {
        when(dataProcessor.validate(any())).thenReturn(null);

        for (String type : List.of("click", "track", "scroll", "page_view", "page_leave", "exposure")) {
            reset(behaviorRepo);
            reportService.processReport(List.of(event(type)));
            verify(behaviorRepo).saveAll(anyList());
        }
    }

    @Test
    @DisplayName("type 为 null 时跳过，不保存")
    void nullTypeSkipped() {
        when(dataProcessor.validate(any())).thenReturn(null);
        Map<String, Object> e = new HashMap<>();
        e.put("appId", "test");
        e.put("timestamp", 1L);
        // no type

        int count = reportService.processReport(List.of(e));

        assertThat(count).isEqualTo(1);
        verify(performanceRepo, never()).saveAll(anyList());
        verify(errorRepo, never()).saveAll(anyList());
        verify(behaviorRepo, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("unknown 类型跳过")
    void unknownTypeSkipped() {
        when(dataProcessor.validate(any())).thenReturn(null);

        reportService.processReport(List.of(event("unknown_type")));

        verify(performanceRepo, never()).saveAll(anyList());
    }

    // ========== 混合类型 ==========

    @Test
    @DisplayName("混合类型批量上报分别存入对应 Repository")
    void mixedTypes() {
        when(dataProcessor.validate(any())).thenReturn(null);

        List<Map<String, Object>> events = List.of(
                event("performance"), event("error"), event("click"), event("api")
        );

        int count = reportService.processReport(events);

        assertThat(count).isEqualTo(4);
        verify(performanceRepo, times(1)).saveAll(argThat(iterable -> {
            int size = 0; for (Object ignored : iterable) size++; return size == 2;
        }));
        verify(errorRepo, times(1)).saveAll(argThat(iterable -> {
            int size = 0; for (Object ignored : iterable) size++; return size == 1;
        }));
        verify(behaviorRepo, times(1)).saveAll(argThat(iterable -> {
            int size = 0; for (Object ignored : iterable) size++; return size == 1;
        }));
    }

    // ========== Redis 更新 ==========

    @Test
    @DisplayName("每条事件都更新 Redis 统计")
    void updatesRedisStats() {
        when(dataProcessor.validate(any())).thenReturn(null);

        reportService.processReport(List.of(event("performance")));

        verify(redisStatsService).incrementStats("test-app", "performance");
    }

    @Test
    @DisplayName("Redis 更新失败不影响主流程")
    void redisFailureDoesNotBreak() {
        when(dataProcessor.validate(any())).thenReturn(null);
        doThrow(new RuntimeException("Redis down")).when(redisStatsService).incrementStats(any(), any());

        int count = reportService.processReport(List.of(event("performance")));

        assertThat(count).isEqualTo(1); // 仍正常返回
    }

    // ========== 保存失败 ==========

    @Test
    @DisplayName("MongoDB 保存失败抛 RuntimeException")
    void saveFailure() {
        when(dataProcessor.validate(any())).thenReturn(null);
        doThrow(new RuntimeException("MongoDB down")).when(performanceRepo).saveAll(anyList());

        assertThatThrownBy(() -> reportService.processReport(List.of(event("performance"))))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Internal server error");
    }
}
