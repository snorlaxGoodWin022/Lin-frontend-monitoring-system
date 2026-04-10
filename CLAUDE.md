# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Frontend Monitoring System (前端监控系统) — a multi-package project for tracking web application performance, errors, and user behavior. Data flows from SDK → Server (MongoDB/Redis) → Dashboard visualization. Contains both a Node.js and Java implementation of the server.

## Common Commands

Each package runs independently from its own directory. There is no root-level build system.

### monitor-sdk
```bash
cd monitor-sdk
npm install
npm run dev      # rollup -c -w (watch mode)
npm run build    # rollup -c → dist/ (UMD, UMD-min, ESM, CJS)
npm test         # jest
```

### monitor-server
```bash
cd monitor-server
npm install
cp .env.example .env   # configure MONGODB_URI, REDIS_URI, PORT
npm run dev             # nodemon src/app.js
npm start               # node src/app.js
npm test                # jest
docker-compose up -d    # alternative: start with MongoDB + Redis via Docker
```

### monitor-server-java (Spring Boot + Maven, JDK 17)
```bash
cd monitor-server-java
mvn compile              # 编译
mvn spring-boot:run      # 启动开发服务（需要 MongoDB + Redis 运行中）
mvn package -DskipTests  # 打包 jar
docker-compose up -d     # Docker 启动（包含 MongoDB + Redis）
```
环境要求：`JAVA_HOME=D:\softIT\Java-Jdk\jdk-17`，`MAVEN_HOME=D:\softIT\Maven\apache-maven`

### monitor-dashboard
```bash
cd monitor-dashboard
npm install
npm run dev      # vite dev server on :5173 (proxies /api → localhost:3000)
npm run build    # vite build
```

## Architecture

### Data Flow

1. **SDK** (browser) collects metrics → queues in `ReportManager` → batches & sends via `sendBeacon`/`fetch` to `POST /api/report`
2. **Server** validates & cleans data → categorizes by type → bulk-inserts into MongoDB collections (`Performance`, `Error`, `Behavior`) → updates Redis real-time counters
3. **Dashboard** queries `GET /api/query/*` and `GET /api/dashboard/*` endpoints → renders via Vue 3 + ECharts

### SDK Module System (`monitor-sdk/src/`)

- `core/base.js` — `MonitorBase` orchestrates module lifecycle. Modules are loaded dynamically via `require('../modules/${name}')` based on config flags (`enablePerformance`, `enableError`, `enableBehavior`, `enableExposure`).
- `core/config.js` — `Config` class validates `appId`/`apiUrl` required fields, manages sampling rate.
- `core/report.js` — `ReportManager` maintains a priority queue, auto-reports on interval or when queue reaches `maxQueueSize`, caches failed reports to `CacheManager` for recovery.
- `modules/performance.js` — Uses `PerformanceObserver` for Web Vitals (LCP, FID, CLS, FCP), intercepts `fetch`/`XHR` for API timing.
- `modules/error.js` — Captures `window.onerror`, `unhandledrejection`, resource load errors, provides a Vue error handler.
- `modules/behavior.js` — Auto-tracks clicks (with XPath), page views/stay time, scroll depth. Manual events via `track()`.
- `modules/exposure.js` — Element viewport exposure tracking.

SDK builds to 4 formats via Rollup: UMD (`MonitorSDK` global), UMD-minified, ESM, CJS. `pako` is external (for compression).

### Server API (`monitor-server/src/`)

Node.js (Express) 实现。Java 实现见 `monitor-server-java/`，API 契约完全一致。

- `api/report.js` — Single `POST /` endpoint. Validates array data, categorizes by `type` field, bulk-inserts to MongoDB, updates Redis hash counters.
- `api/query.js` — `GET /performance`, `GET /errors`, `GET /behavior` with `appId`, time range, type filters.
- `api/dashboard.js` — Aggregation endpoints: `/stats` (Redis counters), `/performance-trend` (hourly Web Vitals averages), `/error-distribution` (group by error type), `/pv-uv` (page view / unique visitor counts).
- `models/` — Mongoose schemas for `Performance`, `Error`, `Behavior` with compound indexes on `(appId, timestamp)`.
- `utils/processor.js` — Input validation, URL desensitization (strips token/password/key/secret params), UA parsing.

### Dashboard (`monitor-dashboard/src/`)

Vue 3 SPA with 4 views: `Dashboard` (overview), `Performance`, `Error`, `Behavior`. Uses Element Plus for UI, ECharts for charts, Vue Router with hash-free `createWebHistory`. Vite proxies `/api` to the server.

## Key Conventions

- **SDK uses ES module syntax** (`import`/`export`) but the Node.js server uses **CommonJS** (`require`/`module.exports`).
- **Java server** (`monitor-server-java/`) uses Spring Boot 3.2 + JDK 17, standard layered architecture: controller → service → repository. Uses `Map<String, Object>` + Jackson `ObjectMapper.convertValue()` for dynamic JSON → Document conversion.
- Server environment config is in `monitor-server/.env` (dotenv).
- MongoDB and Redis are required dependencies — use `docker-compose up -d` in `monitor-server/` to spin them up.
- SDK entry point is `monitor-sdk/src/index.js`, exporting a singleton `MonitorSDK` instance.
- Report data type categorization: `performance`|`api` → Performance collection, `error` → Error collection, `click`|`track`|`scroll`|`page_view`|`page_leave`|`exposure` → Behavior collection.
