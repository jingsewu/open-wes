# Project Context: Open Warehouse Execution System (WES)

## 🎯 项目目标

Open WES 是一个开源的、可定制的仓库执行系统，专注于实时协调仓库自动化设备（机器人、AGV、IoT）的工作流程 [citation:1]。

## 🏗️ 技术栈

- **前端**：React (? 从 `client` 目录推断)，Webpack
- **后端**：Java17 Springboot springCloud (? 从 `server` 目录推断)
- **数据库**：MySQL (有 `initdb.d` 目录)
- **缓存**：Redis
- **部署**：Docker + Docker Compose
- **API 风格**：RESTful + WebSocket (实时监控)

## 📐 架构原则

- **微服务架构**：各模块可独立部署
- **领域驱动设计**：按业务能力划分模块（WES核心业务出入盘理货、库存、库内业务）
- **清晰分层**：DDD4层架构
```
┌─────────────────────────────────────────────┐
│  interfaces (接口层)                        │
│  - gRPC/REST 适配器                        │
│  - DTO ↔ Domain 转换 (ProtoMapper)        │
└──────────────┬──────────────────────────────┘
               │ 可依赖
               ↓
┌─────────────────────────────────────────────┐
│  application (应用层)                       │
│  - 用例编排                                │
│  - 事务边界控制                            │
│  - 跨聚合协调                              │
└──────────────┬──────────────────────────────┘
               │ 可依赖
               ↓
┌─────────────────────────────────────────────┐
│  domain (领域层) ⭐                         │
│  - 聚合根、实体、值对象                    │
│  - 业务规则和不变性                        │
│  - 仓储接口                                │
│  - 完全独立，不依赖任何层                  │
└────────────┬───────────────────────────────┘
             │ 实现接口
             ↑
┌─────────────────────────────────────────────┐
│  infrastructure (基础设施层)                │
│  - Repository 实现                         │
│  - PO ↔ Domain 转换 (POMapper)            │
│  - Hibernate ORM                          │                         │
└─────────────────────────────────────────────┘
```

## 🧪 测试策略

- 单元测试：Jest (前端) / Junit (后端)
- 集成测试：使用测试数据库

## 🔧 代码规范

- 前端：ESLint + Prettier
- 后端：根据目前的代码风格形成规范（允许做一些优化修改）
- Commit：Conventional Commits

## 🚀 快速启动

```bash
git clone https://github.com/jingsewu/open-wes
cd open-wes
HOST_IP=$(hostname -I | awk '{print $1}') docker-compose up -d
```

## 📁 目录结构说明

- `client/` - React 前端
- `server/` - Java 后端
- `initdb.d/` - 数据库初始化脚本
- `docker-compose.yml` - 本地开发环境
