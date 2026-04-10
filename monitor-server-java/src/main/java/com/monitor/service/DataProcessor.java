package com.monitor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Service
public class DataProcessor {

    private static final Logger log = LoggerFactory.getLogger(DataProcessor.class);

    private final Set<String> sensitiveParams;

    public DataProcessor(@Value("${monitor.url.sensitive-params:token,password,key,secret}") String params) {
        this.sensitiveParams = new HashSet<>(Arrays.asList(params.split(",")));
    }

    public String validate(List<Map<String, Object>> events) {
        if (events == null || events.isEmpty()) {
            log.warn("Received null or empty events list");
            return "Data must be non-empty array";
        }
        for (int i = 0; i < events.size(); i++) {
            Map<String, Object> event = events.get(i);
            if (event.get("appId") == null) {
                log.warn("Event at index {} missing appId", i);
                return "appId is required";
            }
            if (event.get("timestamp") == null) {
                log.warn("Event at index {} missing timestamp", i);
                return "timestamp is required";
            }
        }
        log.debug("Validated {} events", events.size());
        return null;
    }

    public void cleanData(Map<String, Object> event) {
        if (event.get("url") instanceof String url) {
            event.put("url", desensitizeUrl(url));
        }

        Object ts = event.get("timestamp");
        if (ts instanceof Number) {
            event.put("timestamp", new Date(((Number) ts).longValue()));
        }

        Object ua = event.get("userAgent");
        if (ua instanceof String uaStr) {
            event.put("browser", extractBrowser(uaStr));
            event.put("platform", extractPlatform(uaStr));
            event.put("device", extractDevice(uaStr));
            event.remove("userAgent");
        }
    }

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
            log.warn("Failed to parse URL for desensitization: {}", url);
            return url;
        }
    }

    public String extractBrowser(String ua) {
        if (ua.contains("Edg")) return "Edge";
        if (ua.contains("Chrome")) return "Chrome";
        if (ua.contains("Safari")) return "Safari";
        if (ua.contains("Firefox")) return "Firefox";
        return "Unknown";
    }

    public String extractPlatform(String ua) {
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac")) return "MacOS";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        if (ua.contains("Linux")) return "Linux";
        return "Unknown";
    }

    public String extractDevice(String ua) {
        if (ua.contains("Mobile") || ua.contains("iPhone")) return "Mobile";
        if (ua.contains("Tablet") || ua.contains("iPad")) return "Tablet";
        return "Desktop";
    }
}
