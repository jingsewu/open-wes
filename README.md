# Open Warehouse Execution System (WES)

![GitHub Repo stars](https://img.shields.io/github/stars/jingsewu/open-wes?style=social)
![License](https://img.shields.io/github/license/jingsewu/open-wes)
![Release](https://img.shields.io/github/v/release/jingsewu/open-wes)
![Gitee Stars](https://gitee.com/pigTear/open-wes/badge/star.svg?theme=social)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/jingsewu/open-wes)

[English](README.md) | [中文](README_CN.md) | [日本語](README_JP.md)

**Open WES** is a customizable, open-source Warehouse Execution System designed to streamline warehouse operations. It
integrates seamlessly with various automation technologies, providing efficient workflow management, task scheduling,
and real-time data tracking.

## Features

- **Task Management**: Efficiently manage and prioritize tasks across warehouse operations.
- **Real-time Monitoring**: Get a live view of inventory, equipment, and workflows.
- **Modular Design**: Easily integrate with existing warehouse systems.
- **Customizable Rules**: Configure rules for task allocation, sorting, and routing.
- **Open API**: Interact with the system using RESTful or WebSocket APIs.
- **AI-Powered Optimization**: Leverage artificial intelligence to optimize warehouse operations, predict demand, and automate decision-making.

## Project Setup with Docker Compose

### Prerequisites

Before you begin, ensure you have the following installed:

1. **Docker**: [Install Docker](https://docs.docker.com/get-docker/)
2. **Docker Compose**: [Install Docker Compose](https://docs.docker.com/compose/install/)


### Run the project within 30s

Run the following commands to clone the repository and start the project in under 30 seconds:

```bash
git clone https://github.com/jingsewu/open-wes
cd open-wes
docker-compose up --build
```

## Comparison
| **Category**               | **Open-WES**                                                                 | **Traditional WMS**                                                                 |
|-----------------------------|--------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| **Core Focus**              | Real-time orchestration of warehouse workflows, automation, and execution.     | Inventory tracking, order management, and broad operational oversight.              |
| **Automation Integration** | Built for seamless integration with robots, AGVs, IoT, and real-time systems.  | Limited automation support; often requires middleware or custom development.        |
| **Open Source**             | Free, open-source (MIT license). Community-driven development.                 | Proprietary, closed-source. Licensing fees and vendor lock-in.                     |
| **Customization**           | Fully customizable (code access). Adapt workflows to specific needs.           | Limited customization; depends on vendor-provided configurations.                  |
| **Cost**                    | No licensing fees. Low cost for deployment (hosting/maintenance only).         | High upfront licensing costs + recurring fees for support/upgrades.                |
| **Scalability**             | Modular design scales with automation and business needs.                      | Scalability limited by vendor roadmap and licensing tiers.                         |
| **Real-Time Processing**    | Optimizes tasks dynamically (e.g., order prioritization, resource allocation). | Batch-oriented processing; slower response to real-time changes.                   |
| **Deployment**              | Cloud-native or on-premise. Flexible infrastructure.                           | Often on-premise or vendor-hosted. Limited cloud flexibility.                       |
| **Integration**             | APIs for modern tools (e.g., ROS, IoT platforms, microservices).               | Legacy APIs; integration may require costly middleware.                             |
| **User Interface**          | Developer-friendly (code-first). UI customizable via open frameworks.          | Pre-built UI with limited flexibility.                                              |
| **Support**                 | Community-driven support (GitHub, forums). Optional paid consultancy.          | Vendor-provided support (additional cost). Slower fixes for niche use cases.        |
| **Use Cases**               | High-automation warehouses, e-commerce/3PLs needing real-time adaptability.    | General inventory management, low-to-medium automation facilities.                 |
| **Transparency**            | Full visibility into code and processes. Auditable and secure.                 | Opaque systems; security/processes depend on vendor trust.                         |

---

### **Key Differentiators**:
1. **Automation-Centric**: Open-WES is designed for warehouses using robots, IoT, and real-time workflows, while traditional WMS focuses on inventory and order management.
2. **Cost Efficiency**: Open-WES eliminates licensing fees and leverages community contributions for innovation.
3. **Flexibility**: Customize Open-WES to integrate with niche tools (e.g., custom robots, AI schedulers).
4. **Real-Time vs. Batch**: Open-WES reacts instantly to changes (e.g., order surges, machine downtime), whereas WMS relies on scheduled updates.

### **When to Use Open-WES**:
- You need **real-time coordination** of robots, AGVs, or IoT devices.
- You prioritize **cost savings** and **custom workflows**.
- Your warehouse is evolving toward Industry 4.0 (smart automation).

### **When to Use Traditional WMS**:
- You require a **proven, out-of-the-box** solution for basic inventory/order management.
- Your operations rely on **legacy systems** with minimal automation.
- You prefer **vendor-managed support** over self-hosting.

For detailed instructions and examples, refer to our example [website](https://www.openwes.top/).

## Contributing

We welcome contributions from the community to help improve this project. To contribute:

1. Fork the repository.
2. Create a new branch for your feature or bug fix.
3. Make your changes and commit them.
4. Submit a pull request, detailing your changes and the problem they solve.

Please review our [Contribution Guidelines](CONTRIBUTING.md) for more information.

## License

This project is licensed under the [MIT License](LICENSE).

## Contact

If you have any questions or need assistance, feel free to reach out to us on
our [GitHub Issues](https://github.com/jingsewu/open-wes/issues) page.

Thank you for using and contributing !

## Architecture
The architecture of Open WES is modular and scalable, designed to handle complex warehouse operations. Below is a high-level overview of its components:

![Architecture](server/doc/image/architecture.png)

## Getting Help

If you encounter issues or have questions, check out the following resources:
- [GitHub Issues](https://github.com/jingsewu/open-wes/issues): Report bugs or request features.
- [Documentation](https://docs.openwes.top): Find detailed guides and API documentation.  
