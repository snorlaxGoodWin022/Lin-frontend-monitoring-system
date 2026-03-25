# Monitor SDK

企业级前端监控SDK,提供性能监控、错误监控、用户行为埋点等功能。

## 功能特性

- 🚀 **性能监控**: Web Vitals、页面加载、资源加载、接口性能、长任务
- 🐛 **错误监控**: JS错误、Promise错误、资源错误、Vue错误、SourceMap定位
- 📊 **埋点追踪**: 自动埋点、手动埋点、曝光埋点、页面访问统计
- 📤 **智能上报**: 优先级队列、数据压缩、离线缓存、重试机制
- 🎯 **轻量级**: 压缩后 < 20KB

## 快速开始

### 安装

```bash
npm install monitor-sdk
```

### 使用

```javascript
import MonitorSDK from 'monitor-sdk'

MonitorSDK.init({
  appId: 'your-app-id',
  apiUrl: 'https://monitor.example.com/api/report',
  userId: 'user-123',
  enablePerformance: true,
  enableError: true,
  enableBehavior: true,
  enableExposure: true
})

// 手动埋点
MonitorSDK.track('custom_event', {
  buttonName: 'submit',
  page: 'home'
})
```

## 配置选项

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| appId | string | 必填 | 应用ID |
| apiUrl | string | 必填 | 上报接口地址 |
| userId | string | - | 用户ID |
| enablePerformance | boolean | true | 是否启用性能监控 |
| enableError | boolean | true | 是否启用错误监控 |
| enableBehavior | boolean | true | 是否启用行为埋点 |
| enableExposure | boolean | true | 是否启用曝光监控 |
| reportInterval | number | 5000 | 上报间隔(ms) |
| maxQueueSize | number | 10 | 队列最大大小 |
| sampleRate | number | 1 | 采样率(0-1) |

## API文档

### MonitorSDK.init(config)

初始化SDK

### MonitorSDK.track(eventName, eventData)

手动上报埋点事件

### MonitorSDK.setUserId(userId)

设置用户ID

### MonitorSDK.destroy()

销毁SDK实例

## 浏览器兼容性

- Chrome >= 60
- Firefox >= 60
- Safari >= 12
- Edge >= 79

## License

MIT
