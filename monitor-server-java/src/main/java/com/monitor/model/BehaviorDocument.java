package com.monitor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

@Document(collection = "behaviors")
@CompoundIndex(name = "appId_type_timestamp", def = "{'appId': 1, 'type': 1, 'timestamp': -1}")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BehaviorDocument {

    @Id
    private String id;

    @Indexed
    private String appId;
    private String userId;
    private String sessionId;
    private String deviceId;
    private String type;
    private String eventName;
    private Map<String, Object> eventData;
    private Map<String, Object> elementInfo;
    private Integer depth;
    private Integer maxDepth;
    private Double scrollY;
    private Long stayTime;
    private String elementId;
    private Map<String, Object> position;

    @Indexed
    private Date timestamp;

    private String url;
    private String path;
    private String title;
    private String referrer;
    private String platform;
    private String browser;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public Map<String, Object> getEventData() { return eventData; }
    public void setEventData(Map<String, Object> eventData) { this.eventData = eventData; }

    public Map<String, Object> getElementInfo() { return elementInfo; }
    public void setElementInfo(Map<String, Object> elementInfo) { this.elementInfo = elementInfo; }

    public Integer getDepth() { return depth; }
    public void setDepth(Integer depth) { this.depth = depth; }

    public Integer getMaxDepth() { return maxDepth; }
    public void setMaxDepth(Integer maxDepth) { this.maxDepth = maxDepth; }

    public Double getScrollY() { return scrollY; }
    public void setScrollY(Double scrollY) { this.scrollY = scrollY; }

    public Long getStayTime() { return stayTime; }
    public void setStayTime(Long stayTime) { this.stayTime = stayTime; }

    public String getElementId() { return elementId; }
    public void setElementId(String elementId) { this.elementId = elementId; }

    public Map<String, Object> getPosition() { return position; }
    public void setPosition(Map<String, Object> position) { this.position = position; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getReferrer() { return referrer; }
    public void setReferrer(String referrer) { this.referrer = referrer; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
