# 开源仓库执行系统 (Open WES)

[English](README.md) | [中文](#README_CN.md) | [日本語](README_JP.md)

Open WES 是一个可定制的开源仓库执行系统，旨在简化仓库操作。它可与各种自动化技术无缝集成，提供高效的工作流管理、任务调度和实时数据跟踪功能。

---

### 功能特点
- **任务管理**：高效管理和优先处理仓库操作中的任务。
- **实时监控**：实时查看库存、设备和工作流状态。
- **模块化设计**：轻松与现有仓库系统集成。
- **可定制规则**：配置任务分配、分拣和路径规划的规则。
- **开放 API**：通过 RESTful 或 WebSocket API 与系统交互。

---

### 安装指南

#### 前置条件
1. **Java (17+)**：用于运行后端服务程序。
2. **MySQL (8.0+)**：作为存储仓库数据的关系型数据库。
3. **Nacos (2.0+)**：服务注册和配置管理工具。
4. **Redis (7.0+)**：用于缓存和会话管理。
5. **Node.js (18+)**：用于运行客户端应用程序。

**注意**：MySQL、Nacos 和 Redis 应安装在同一台服务器上。

#### 安装步骤
1. 克隆项目仓库：
   ```bash
   git clone https://github.com/jinsewu/openwes.git
2.  通过执行 `/script` 文件夹中的脚本添加 Nacos 配置。

3.  编辑主机文件以配置 Nacos 主机名 `nacos.openwes.com`：

  *   在 Linux 系统中编辑 `/etc/hosts`
  *   在 Windows 系统中编辑 `C:\Windows\System32\drivers\etc\hosts`
      添加以下内容：

        ``172.0.0.1 nacos.openwes.com``

4.  创建数据库 `openwes`。

5.  启动服务端程序：运行 `/server` 目录下的 `WesApplication`、`GatewayApplication` 和 `StationApplication`。

6.  启动客户端：
   * npm install
   * npm start

7.  登录系统：输入用户名/密码 `admin/12345`。

* * *

### 使用指南

Open WES 提供用户友好的界面，可用于管理您的仓库，包括：

*   **新增商品**：轻松添加新商品到库存，简化新商品的引入流程。
*   **库存管理**：监控和管理库存水平，防止过多或过少的库存问题。
*   **支持完整仓库流程**：涵盖入库（收货）、出库（发货）、复核、包装、盘点和库存搬迁，确保操作高效有序。
*   **生成性能报告**：获取详细的仓库绩效报告，深入了解关键指标，如库存周转率、订单完成率和运营效率。
*   **支持人工与机器人操作**：为人类和机器人操作提供无缝支持，优化生产效率。
*   **轻松集成 WCS 和 RCS**：轻松与仓库控制系统 (WCS) 和机器人控制系统 (RCS) 集成，实现同步高效的管理。

* * *

### 贡献指南

我们欢迎社区贡献来帮助改进此项目。贡献方式如下：

1.  Fork 项目仓库。
2.  为新功能或问题修复创建分支。
3.  提交代码更改并提交 Commit。
4.  提交 Pull Request，详细说明更改内容及解决的问题。

详细说明请参考 [贡献指南](CONTRIBUTING.md)。

* * *

### 许可协议

本项目基于 [MIT 许可证](LICENSE)。

* * *

### 联系我们

如有任何问题或需要帮助，请通过以下方式联系我们：

*   **GitHub Issues**：提交问题或请求功能。

感谢您使用和贡献 Open WES！

* * * 
### 架构设计

Open WES 的架构采用模块化和可扩展设计，可应对复杂的仓库操作。以下是其组件的高层概览：

**架构图**

![Architecture](server/doc/image/architecture.png)

* * *

### 获取帮助

如遇到问题或有任何疑问，请查看以下资源：

*   **[GitHub Issues](https://github.com/jingsewu/open-wes/issues)**：提交问题或请求新功能。
*   **[文档](./doc)**：查找详细指南和 API 文档。
