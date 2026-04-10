package com.monitor.service;

import com.monitor.model.ErrorDocument;
import com.monitor.model.PerformanceDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueryServiceTest {

    @Mock MongoTemplate mongoTemplate;

    @InjectMocks QueryService queryService;

    @Test
    @DisplayName("queryPerformance 传递正确的 class 和 query")
    void queryPerformance() {
        when(mongoTemplate.find(any(Query.class), eq(PerformanceDocument.class)))
                .thenReturn(List.of(new PerformanceDocument()));

        List<PerformanceDocument> result = queryService.queryPerformance("app1", null, null, 50);

        assertThat(result).hasSize(1);
        verify(mongoTemplate).find(any(Query.class), eq(PerformanceDocument.class));
    }

    @Test
    @DisplayName("queryErrors 支持 type 过滤")
    void queryErrorsWithType() {
        when(mongoTemplate.find(any(Query.class), eq(ErrorDocument.class)))
                .thenReturn(List.of());

        queryService.queryErrors("app1", "jsError", "1712600000000", "1712700000000", 100);

        verify(mongoTemplate).find(any(Query.class), eq(ErrorDocument.class));
    }

    @Test
    @DisplayName("参数全部为 null 时仍能构建有效 query")
    void allParamsNull() {
        when(mongoTemplate.find(any(Query.class), eq(PerformanceDocument.class)))
                .thenReturn(List.of());

        queryService.queryPerformance(null, null, null, 100);

        verify(mongoTemplate).find(any(Query.class), eq(PerformanceDocument.class));
    }
}
