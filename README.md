# Lin 前端监控系统

企业级前端监控系统，提供性能监控、错误监控、用户行为追踪等全方位监控能力。

## 项目结构

```
Frontend-Monitoring-System/
├── monitor-sdk/          # 前端监控SDK
├── monitor-server/       # 后端服务
├── monitor-dashboard/   # 管理后台
└── README.md            # 项目说明文档
```

## 功能特性

### 🚀 性能监控
- Web Vitals 指标（LCP、FID、CLS）
- 页面加载时间分析
- 资源加载性能监控
- 接口请求性能追踪
- 长任务检测

### 🐛 错误监控
- JavaScript 错误捕获
- Promise 错误追踪
- 资源加载错误
- Vue/React 错误边界
- SourceMap 错误定位

### 📊 行为追踪
- 自动埋点（点击、滚动、输入）
- 手动埋点接口
- 元素曝光监控
- 页面访问统计
- 用户路径分析

### 📤 智能上报
- 优先级队列管理
- 数据压缩传输
- 离线缓存支持
- 自动重试机制
- 采样率控制

## 快速开始

### 环境要求

- Node.js >= 16
- MongoDB >= 6.0
- Redis >= 7.0
- Docker（可选）

### 1. 启动后端服务

#### 方式一：Docker（推荐）

```bash
cd monitor-server
docker-compose up -d
```

#### 方式二：手动启动

```bash
# 安装依赖
cd monitor-server
npm install

# 配置环境变量
cp .env.example .env
# 编辑 .env 文件，配置 MongoDB 和 Redis 连接信息

# 启动服务
npm run dev
```

服务启动后访问：http://localhost:3000

### 2. 启动管理后台

```bash
cd monitor-dashboard
npm install
npm run dev
```

管理后台访问：http://localhost:5173

### 3. 在项目中集成 SDK

#### 安装 SDK

```bash
npm install monitor-sdk
```

#### 使用 SDK

```javascript
import MonitorSDK from 'monitor-sdk'

MonitorSDK.init({
  appId: 'your-app-id',
  apiUrl: 'http://localhost:3000/api/report',
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

## 配置说明

### SDK 配置参数

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

### 后端环境变量

```env
PORT=3000                    # 服务端口
MONGODB_URI=mongodb://localhost:27017/monitor  # MongoDB连接地址
REDIS_URI=redis://localhost:6379              # Redis连接地址
NODE_ENV=development         # 运行环境
```

## API 接口

### 数据上报

```
POST /api/report
Content-Type: application/json

{
  "type": "performance|error|behavior",
  "data": {...}
}
```

### 数据查询

```
GET /api/query/performance?appId=xxx&startTime=xxx&endTime=xxx
GET /api/query/error?appId=xxx&startTime=xxx&endTime=xxx
GET /api/query/behavior?appId=xxx&startTime=xxx&endTime=xxx
```

### 仪表盘数据

```
GET /api/dashboard/overview?appId=xxx
GET /api/dashboard/trends?appId=xxx&period=7d
```

## 开发指南

### SDK 开发

```bash
cd monitor-sdk
npm install
npm run dev    # 开发模式
npm run build  # 构建
```

### 后端开发

```bash
cd monitor-server
npm install
npm run dev    # 开发模式（使用 nodemon）
npm start      # 生产模式
```

### 前端开发

```bash
cd monitor-dashboard
npm install
npm run dev    # 开发模式
npm run build  # 构建
```

## 部署说明

### Docker 部署

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 生产环境配置

1. 修改 `.env` 文件中的环境变量
2. 配置 MongoDB 和 Redis 的生产环境连接
3. 使用 `npm run build` 构建前端项目
4. 配置 Nginx 反向代理

## 浏览器兼容性

- Chrome >= 60
- Firefox >= 60
- Safari >= 12
- Edge >= 79

## 技术栈

### SDK
- Rollup - 模块打包
- Babel - 代码转译
- Pako - 数据压缩

### 后端
- Express - Web 框架
- MongoDB - 数据存储
- Redis - 缓存和队列
- Mongoose - ODM

### 前端
- Vue 3 - 前端框架
- Element Plus - UI 组件库
- ECharts - 数据可视化
- Axios - HTTP 客户端

## 常见问题

### 1. 数据上报失败
检查 SDK 配置中的 `apiUrl` 是否正确，确保后端服务正常运行。

### 2. SourceMap 错误定位不准确
确保在生产环境中正确配置 SourceMap，并上传到错误服务器。

### 3. 数据量大导致性能问题
调整 `sampleRate` 采样率，或使用 `reportInterval` 控制上报频率。

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## License

MIT

