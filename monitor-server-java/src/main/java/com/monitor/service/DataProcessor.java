package com.monitor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 数据处理工具：URL 脱敏、UA 解析、时间戳转换
 * 移植自 monitor-server/src/utils/processor.js
 */
@Service
public class DataProcessor {

    private final Set<String> sensitiveParams;

    public DataProcessor(@Value("${monitor.url.sensitive-params:token,password,key,secret}") String params) {
        this.sensitiveParams = new HashSet<>(Arrays.asList(params.split(",")));
    }

    /**
     * 验证上报数据：每条记录必须包含 appId 和 timestamp
     */
    public String validate(List<Map<String, Object>> events) {
        if (events == null || events.isEmpty()) {
            return "Data must be non-empty array";
        }
        for (Map<String, Object> event : events) {
            if (event.get("appId") == null) {
                return "appId is required";
            }
            if (event.get("timestamp") == null) {
                return "timestamp is required";
            }
        }
        return null;
    }

    /**
     * 清洗数据：URL 脱敏、时间戳转换、UA 解析
     */
    public void cleanData(Map<String, Object> event) {
        // URL 脱敏
        if (event.get("url") instanceof String url) {
            event.put("url", desensitizeUrl(url));
        }

        // 时间戳转换（SDK 发送毫秒时间戳）
        Object ts = event.get("timestamp");
        if (ts instanceof Number) {
            // 保持为毫秒时间戳，存入 MongoDB 时由 Spring Data 自动转换
            event.put("timestamp", new Date(((Number) ts).longValue()));
        }

        // UA 解析
        Object ua = event.get("userAgent");
        if (ua instanceof String uaStr) {
            event.put("browser", extractBrowser(uaStr));
            event.put("platform", extractPlatform(uaStr));
            event.remove("userAgent");
        }
    }

    /**
     * URL 脱敏：移除敏感查询参数（token, password, key, secret）
     */
    public String desensitizeUrl(String url) {
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            if (query == null || query.isEmpty()) {
                return url;
            }

            StringBuilder newQuery = new StringBuilder();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String key = pair.contains("=") ? pair.substring(0, pair.indexOf('=')) : pair;
                if (!sensitiveParams.contains(key)) {
                    if (newQuery.length() > 0) {
                        newQuery.append("&");
                    }
                    newQuery.append(pair);
                }
            }

            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
                    uri.getPath(), newQuery.toString(), uri.getFragment()).toString();
        } catch (URISyntaxException e) {
            return url;
        }
    }

    /**
     * 提取浏览器名称（注意顺序：Chrome UA 也包含 Safari，需先匹配 Chrome）
     */
    public String extractBrowser(String ua) {
        if (ua.contains("Edg")) return "Edge";
        if (ua.contains("Chrome")) return "Chrome";
        if (ua.contains("Safari")) return "Safari";
        if (ua.contains("Firefox")) return "Firefox";
        return "Unknown";
    }

    /**
     * 提取操作系统
     */
    public String extractPlatform(String ua) {
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac")) return "MacOS";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        if (ua.contains("Linux")) return "Linux";
        return "Unknown";
    }

    /**
     * 提取设备类型
     */
    public String extractDevice(String ua) {
        if (ua.contains("Mobile") || ua.contains("iPhone")) return "Mobile";
        if (ua.contains("Tablet") || ua.contains("iPad")) return "Tablet";
        return "Desktop";
    }
}
