## 🛠️ 使用手册

1. **Postman测试**

    1. Postman导入 `SpringBoot Store API.postman_collection.json`文件
    2. 设置环境，添加变量 `baseUrl` 为 `http://47.111.2.191:8080`
    3. 先登陆/注册，创建购物车，添加商品，结帐。也可使用管理员权限对商品进行增删改

2. **管理员账号**
   账号：[ppp_melody@163.com](mailto:ppp_melody@163.com)
   密码：666666

4. **支付测试卡号**
   4242 4242 4242 4242（任意有效期和 CVC 均可）

5. **API 文档**
   👉 [点击查看](http://47.111.2.191:8080/swagger-ui/index.html#/)

   > ⚠️ 推荐使用 Chrome 浏览器访问，Safari 可能存在兼容性问题。

---

## 🛒 Spring Store API

### 🚀 技术栈

- **Spring Boot** (3.4.5)
- **Spring Security** + JWT（短令牌认证与长令牌刷新）
- **Spring Data JPA** + MySQL
- **MapStruct**（高效实体与 DTO 映射）
- **Swagger / OpenAPI**（自动生成 [API 文档](http://47.111.2.191:8080/swagger-ui/index.html#/)）
- **Flyway**（数据库版本迁移管理）
- **Spring Boot Actuator**（应用健康监控与度量）
- **MySQL 主从复制**（高可用读写分离）
- **Redis Cluster**（分布式缓存，访问加速与热点保护）
- **RabbitMQ**（消息队列，异步处理与解耦）
- **Docker**（一键容器化部署，环境一致性）

---

### 🎯 主要功能

- **用户注册和登录**
  基于 JWT 的短令牌认证，支持长令牌刷新机制，密码加密存储（BCrypt）
- **角色权限管理**
  支持 USER / ADMIN 角色，权限粒度控制
- **商品管理**
  商品的增删改查，新增/修改/删除仅限 ADMIN 权限
- **用户支付与 Webhook 回调**
  集成支付系统，结合 MySQL 乐观锁和 MQ 延迟队列避免超卖，支持自动回滚
- **统一异常与响应处理**
  全局异常捕获与统一响应格式，严格遵循 RESTful 设计规范
- **高效缓存层**
  基于 Redis Cluster 实现缓存加速与热点保护
- **容器化部署**
  支持 Docker 一键部署，提升系统可移植性与环境一致性

---

### 🌟 设计亮点

- **简洁的实体-DTO 映射**
  使用 MapStruct 自动实现实体与 DTO 的高效映射，减少重复代码

- **统一异常与响应规范**
  全局异常捕获与统一响应格式，严格遵循 RESTful 设计规范

- **高可用数据库架构**
  基于 MySQL 主从复制，实现读写分离与数据冗余

- **高效分布式缓存**
  采用 Redis Cluster 加速数据访问，保护热点数据，提升系统性能

- **消息异步解耦**
  基于 RabbitMQ 集群，实现消息异步处理与系统解耦

- **完善的系统监控**
  通过 Spring Boot Actuator 实时监控系统运行指标

    - 👉 [查看 Health](http://47.111.2.191:8080/actuator/health)
    - 👉 [查看 Metrics](http://47.111.2.191:8080/actuator/metrics)
    - 👉 [查看 性能监控](http://47.111.2.191:8080/actuator/metrics/http.server.requests)

- **容器化部署**
  支持基于 Docker 的一键部署，保证环境一致性和快速交付

---

## 🛠️ User Guide

1. **Postman Testing**

    1. Import the `SpringBoot Store API.postman_collection.json` file into Postman
    2. Set up the environment: add a variable `baseUrl` with the value `http://47.111.2.191:8080`
    3. Log in or register first, then create a cart, add products, and proceed to checkout. You can also use the admin
       account to add, delete, or update products.

2. **Admin Account**
   Email: [ppp\_melody@163.com](mailto:ppp_melody@163.com)
   Password: 666666

3. **Test Card Number**
   4242 4242 4242 4242 (Any valid expiration date and CVC will work)

4. **API Documentation**
   👉 [View here](http://47.111.2.191:8080/swagger-ui/index.html#/)

   > ⚠️ Recommended to use Chrome browser. Safari may have compatibility issues.

---

## 🛒 Spring Store API

### 🚀 Tech Stack

- **Spring Boot** (3.4.5)
- **Spring Security** + **JWT** (Short-lived token authentication + refresh token support)
- **Spring Data JPA** + **MySQL**
- **MapStruct** (Efficient entity-DTO mapping)
- **Swagger / OpenAPI** (Auto-generated [API documentation](http://47.111.2.191:8080/swagger-ui/index.html#/)）
- **Flyway** (Database migration management)
- **Spring Boot Actuator** (Application health checks & metrics)
- **MySQL Master-Slave Replication** (High availability & read/write separation)
- **Redis Cluster** (Distributed cache, performance acceleration & hotspot protection)
- **RabbitMQ** (Message queue, async processing & decoupling)
- **Docker** (One-click containerized deployment, environment consistency)

---

### 🎯 Core Features

- **User Registration & Login**
  JWT-based short-lived token authentication with refresh token support and secure password hashing (BCrypt)
- **Role-Based Access Control**
  Supports USER / ADMIN roles with fine-grained permission control
- **Product Management**
  CRUD operations for products; create/update/delete are restricted to ADMIN
- **User Payment & Webhook Integration**
  Payment system integration with MySQL optimistic locking and MQ delayed queues to prevent overselling and ensure
  transaction rollback
- **Unified Exception & Response Handling**
  Global exception handling and unified response format, strictly following RESTful standards
- **Efficient Caching Layer**
  Redis Cluster-based caching for performance boost and hotspot protection
- **Containerized Deployment**
  Supports one-click deployment via Docker, ensuring portability and environment consistency

---

### 🌟 Architectural Highlights

- **Clean Entity-DTO Mapping**
  Uses MapStruct to automate efficient mapping between entities and DTOs, eliminating boilerplate code

- **Unified Exception & Response Standard**
  Global exception handling and consistent response format, fully RESTful

- **Highly Available Database Architecture**
  MySQL master-slave replication for read/write separation and data redundancy

- **Distributed High-Performance Cache**
  Redis Cluster speeds up data access and protects hotspots, improving system performance

- **Async Messaging & Decoupling**
  RabbitMQ cluster enables async processing and system decoupling

- **Comprehensive System Monitoring**
  Real-time monitoring with Spring Boot Actuator

    - 👉 [Actuator Health](http://47.111.2.191:8080/actuator/health)
    - 👉 [Actuator Metrics](http://47.111.2.191:8080/actuator/metrics)
    - 👉 [Actuator Performance Monitoring](http://47.111.2.191:8080/actuator/metrics/http.server.requests)

- **Containerized Deployment**
  One-click Docker deployment for guaranteed environment consistency and rapid delivery
