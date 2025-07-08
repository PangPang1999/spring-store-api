### 使用手册 / User Guide

1. **API 文档 / API Docs**
   [点击查看｜View here](http://8.130.114.242:8080/swagger-ui/index.html#/)
   > ⚠️ 建议使用 Chrome 浏览器访问。Safari 可能存在兼容性问题。  
   > **Recommended to use Chrome browser. Safari may not work properly.**
2. **管理员账号 / Admin Account**
   账号 / Email: [ppp_melody@163.com](mailto:ppp_melody@163.com)
   密码 / Password: 666666

3. **支付测试卡号 / Test Card Number**
   4242 4242 4242 4242 （任意有效期和 CVC 均可 / Any valid date & CVC）

## 🛒 Spring Store API

### 🚀 技术栈 / Tech Stack

- **Spring Boot** (3.4.5)
- **Spring Security** + JWT
- **Spring Data JPA** + MySQL
- **MapStruct**（简化实体和 DTO 的转换）
- **Swagger / OpenAPI**（自动生成的 API 文档）
- **Flyway**（管理数据库迁移）
- **Spring Boot Actuator**（监控系统运行情况）

### 🎯 主要功能 / Core Features

- **用户注册和登录**（使用 JWT 验证）
- **角色权限管理**（USER 和 ADMIN 两种角色）
- **商品管理**（增删改查）
- **用户支付与 Webhook 回调**
- **统一的错误处理和响应格式**

### 📖 接口文档 / API Documentation

- 通过 Swagger UI 自动生成的交互式文档：
  👉 [点击查看 API 文档](http://8.130.114.242:8080/swagger-ui/index.html#/)
  > ⚠️ 建议使用 Chrome 浏览器访问。Safari 可能存在兼容性问题。  
  > **Recommended to use Chrome browser. Safari may not work properly.**

### 🌟 设计亮点 / Architectural Highlights

- **简洁的实体-DTO 映射**
  使用 MapStruct 避免了大量重复的代码。

- **统一的异常处理与响应规范**
  全局捕获异常，API 响应更统一、更易读。

- **平滑的数据库迁移**
  Flyway 提供安全可靠的数据库版本管理。

- **完善的系统监控**
  通过 Spring Boot Actuator 实时查看系统运行指标。
  👉 [查看 Actuator Metrics](http://8.130.114.242:8080/actuator/metrics/http.server.requests)

---

### 🚀 Tech Stack

- **Spring Boot** (3.4.5)
- **Spring Security** integrated with JWT
- **Spring Data JPA** and MySQL
- **MapStruct** simplifies entity-to-DTO conversions
- **Swagger / OpenAPI** for easy API documentation
- **Flyway** manages database schema updates
- **Spring Boot Actuator** keeps an eye on system health

### 🎯 Core Features

- **User Registration & Login**
  Easy and secure login with JWT.

- **Role-Based Permissions**
  Separate access for USER and ADMIN roles.

- **Product Management (CRUD)**
  Easy management of products directly via APIs.

- **Payments & Webhooks**
  Payments handled smoothly, with webhook support.

- **Unified Errors & Responses**
  Consistent error messages and responses for better API experience.

### 📖 API Documentation

- Interactive and auto-generated via Swagger UI:
  👉 [Check the API Docs](http://8.130.114.242:8080/swagger-ui/index.html#/)
  > ⚠️ **Recommended to use Chrome browser. Safari may not work properly.**

### 🌟 Architectural Highlights

- **Simplified Entity-DTO Mapping**
  Less boilerplate, cleaner code thanks to MapStruct.

- **Clear Error Handling & Responses**
  Easily track down errors with a clear, unified format.

- **Easy Database Updates**
  Database migrations are safe and reliable using Flyway.

- **Comprehensive Monitoring**
  Monitor system health and performance anytime with Actuator metrics:
  👉 [View Metrics](http://8.130.114.242:8080/actuator/metrics/http.server.requests)
