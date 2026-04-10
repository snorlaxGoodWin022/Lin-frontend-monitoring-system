package com.monitor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class DataProcessorTest {

    private DataProcessor dataProcessor;

    @BeforeEach
    void setUp() {
        dataProcessor = new DataProcessor("token,password,key,secret");
    }

    // ========== validate ==========

    @Nested
    @DisplayName("validate()")
    class Validate {

        @Test
        @DisplayName("null 事件列表返回错误")
        void nullEvents() {
            assertThat(dataProcessor.validate(null)).isEqualTo("Data must be non-empty array");
        }

        @Test
        @DisplayName("空事件列表返回错误")
        void emptyEvents() {
            assertThat(dataProcessor.validate(Collections.emptyList())).isEqualTo("Data must be non-empty array");
        }

        @Test
        @DisplayName("缺少 appId 返回错误")
        void missingAppId() {
            List<Map<String, Object>> events = List.of(
                    Map.of("timestamp", 1712600000000L)
            );
            assertThat(dataProcessor.validate(events)).isEqualTo("appId is required");
        }

        @Test
        @DisplayName("缺少 timestamp 返回错误")
        void missingTimestamp() {
            List<Map<String, Object>> events = List.of(
                    Map.of("appId", "test-app")
            );
            assertThat(dataProcessor.validate(events)).isEqualTo("timestamp is required");
        }

        @Test
        @DisplayName("合法数据返回 null")
        void validEvents() {
            List<Map<String, Object>> events = List.of(
                    Map.of("appId", "test-app", "timestamp", 1712600000000L)
            );
            assertThat(dataProcessor.validate(events)).isNull();
        }

        @Test
        @DisplayName("多条数据中有一条缺少字段即报错")
        void secondEventInvalid() {
            List<Map<String, Object>> events = List.of(
                    Map.of("appId", "ok", "timestamp", 1L),
                    new HashMap<>(Map.of("appId", "bad")) // no timestamp
            );
            assertThat(dataProcessor.validate(events)).isEqualTo("timestamp is required");
        }
    }

    // ========== desensitizeUrl ==========

    @Nested
    @DisplayName("desensitizeUrl()")
    class DesensitizeUrl {

        @Test
        @DisplayName("移除 token 参数")
        void removesToken() {
            String result = dataProcessor.desensitizeUrl("https://example.com/page?token=abc123&name=test");
            assertThat(result).doesNotContain("token=");
            assertThat(result).contains("name=test");
        }

        @Test
        @DisplayName("移除 password 参数")
        void removesPassword() {
            String result = dataProcessor.desensitizeUrl("https://example.com?password=secret&id=1");
            assertThat(result).doesNotContain("password=");
            assertThat(result).contains("id=1");
        }

        @Test
        @DisplayName("移除 key 和 secret 参数")
        void removesKeyAndSecret() {
            String result = dataProcessor.desensitizeUrl("https://example.com?key=k1&secret=s1&foo=bar");
            assertThat(result).doesNotContain("key=").doesNotContain("secret=");
            assertThat(result).contains("foo=bar");
        }

        @Test
        @DisplayName("无查询参数时不变")
        void noQueryUnchanged() {
            String url = "https://example.com/page";
            assertThat(dataProcessor.desensitizeUrl(url)).isEqualTo(url);
        }

        @Test
        @DisplayName("全部是敏感参数时 query 为空")
        void allSensitiveParams() {
            String result = dataProcessor.desensitizeUrl("https://example.com?token=abc&password=xyz");
            assertThat(result).doesNotContain("token").doesNotContain("password");
        }

        @Test
        @DisplayName("异常 URL 原样返回")
        void malformedUrl() {
            String bad = "not a url at all %%%";
            assertThat(dataProcessor.desensitizeUrl(bad)).isEqualTo(bad);
        }

        @Test
        @DisplayName("保留 fragment 锚点")
        void preservesFragment() {
            String result = dataProcessor.desensitizeUrl("https://example.com?token=abc#section");
            assertThat(result).contains("#section");
            assertThat(result).doesNotContain("token=");
        }
    }

    // ========== extractBrowser ==========

    @Nested
    @DisplayName("extractBrowser()")
    class ExtractBrowser {

        @Test
        @DisplayName("Chrome UA 识别为 Chrome（不是 Safari）")
        void chrome() {
            String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
            assertThat(dataProcessor.extractBrowser(ua)).isEqualTo("Chrome");
        }

        @Test
        @DisplayName("Edge UA 识别为 Edge（优先于 Chrome）")
        void edge() {
            String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0";
            assertThat(dataProcessor.extractBrowser(ua)).isEqualTo("Edge");
        }

        @Test
        @DisplayName("Safari UA 识别为 Safari")
        void safari() {
            String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15";
            assertThat(dataProcessor.extractBrowser(ua)).isEqualTo("Safari");
        }

        @Test
        @DisplayName("Firefox UA 识别为 Firefox")
        void firefox() {
            String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0";
            assertThat(dataProcessor.extractBrowser(ua)).isEqualTo("Firefox");
        }

        @Test
        @DisplayName("未知 UA 返回 Unknown")
        void unknown() {
            assertThat(dataProcessor.extractBrowser("SomeCrawler/1.0")).isEqualTo("Unknown");
        }
    }

    // ========== extractPlatform ==========

    @Nested
    @DisplayName("extractPlatform()")
    class ExtractPlatform {

        @Test
        void windows() {
            assertThat(dataProcessor.extractPlatform("Mozilla/5.0 (Windows NT 10.0...)")).isEqualTo("Windows");
        }

        @Test
        void mac() {
            assertThat(dataProcessor.extractPlatform("Mozilla/5.0 (Macintosh; Intel Mac OS X...)")).isEqualTo("MacOS");
        }

        @Test
        void android() {
            assertThat(dataProcessor.extractPlatform("Mozilla/5.0 (Linux; Android 14...)")).isEqualTo("Android");
        }

        @Test
        void ios() {
            assertThat(dataProcessor.extractPlatform("Mozilla/5.0 (iPhone; CPU iPhone OS 17...)")).isEqualTo("iOS");
        }

        @Test
        void linux() {
            assertThat(dataProcessor.extractPlatform("Mozilla/5.0 (X11; Linux x86_64...)")).isEqualTo("Linux");
        }

        @Test
        void unknown() {
            assertThat(dataProcessor.extractPlatform("curl/8.0")).isEqualTo("Unknown");
        }
    }

    // ========== extractDevice ==========

    @Nested
    @DisplayName("extractDevice()")
    class ExtractDevice {

        @Test
        void mobileViaMobileKeyword() {
            assertThat(dataProcessor.extractDevice("Mozilla/5.0 (Linux; Android 14; Mobile)")).isEqualTo("Mobile");
        }

        @Test
        void mobileViaIPhone() {
            assertThat(dataProcessor.extractDevice("Mozilla/5.0 (iPhone; CPU iPhone OS 17...)")).isEqualTo("Mobile");
        }

        @Test
        void tabletViaIPad() {
            assertThat(dataProcessor.extractDevice("Mozilla/5.0 (iPad; CPU OS 17...)")).isEqualTo("Tablet");
        }

        @Test
        void desktop() {
            assertThat(dataProcessor.extractDevice("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")).isEqualTo("Desktop");
        }
    }

    // ========== cleanData ==========

    @Nested
    @DisplayName("cleanData()")
    class CleanData {

        @Test
        @DisplayName("时间戳从 Long 转为 Date")
        void convertsTimestamp() {
            Map<String, Object> event = new HashMap<>();
            event.put("appId", "test");
            event.put("timestamp", 1712600000000L);

            dataProcessor.cleanData(event);

            assertThat(event.get("timestamp")).isInstanceOf(Date.class);
        }

        @Test
        @DisplayName("解析 UA 后移除 userAgent 字段并添加 browser/platform")
        void parsesUserAgent() {
            Map<String, Object> event = new HashMap<>();
            event.put("userAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0");

            dataProcessor.cleanData(event);

            assertThat(event).containsEntry("browser", "Chrome");
            assertThat(event).containsEntry("platform", "Windows");
            assertThat(event).doesNotContainKey("userAgent");
        }

        @Test
        @DisplayName("URL 中的敏感参数被移除")
        void desensitizesUrl() {
            Map<String, Object> event = new HashMap<>();
            event.put("url", "https://example.com?token=abc&name=test");

            dataProcessor.cleanData(event);

            assertThat((String) event.get("url")).doesNotContain("token=");
            assertThat((String) event.get("url")).contains("name=test");
        }

        @Test
        @DisplayName("无 url/timestamp/userAgent 字段时不报错")
        void noFieldsNoError() {
            Map<String, Object> event = new HashMap<>();
            event.put("appId", "test");
            dataProcessor.cleanData(event); // 不抛异常即可
            assertThat(event.get("appId")).isEqualTo("test");
        }
    }
}
