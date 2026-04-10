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

@Document(collection = "errors")
@CompoundIndex(name = "appId_type_timestamp", def = "{'appId': 1, 'type': 1, 'timestamp': -1}")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorDocument {

    @Id
    private String id;

    @Indexed
    private String appId;
    private String userId;
    private String sessionId;
    private String deviceId;
    private String type;
    private String message;
    private String source;
    private Integer lineno;
    private Integer colno;
    private String stack;
    private String componentName;
    private String info;
    private Map<String, Object> extra;

    @Indexed
    private Date timestamp;

    private String url;
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

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Integer getLineno() { return lineno; }
    public void setLineno(Integer lineno) { this.lineno = lineno; }

    public Integer getColno() { return colno; }
    public void setColno(Integer colno) { this.colno = colno; }

    public String getStack() { return stack; }
    public void setStack(String stack) { this.stack = stack; }

    public String getComponentName() { return componentName; }
    public void setComponentName(String componentName) { this.componentName = componentName; }

    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }

    public Map<String, Object> getExtra() { return extra; }
    public void setExtra(Map<String, Object> extra) { this.extra = extra; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
