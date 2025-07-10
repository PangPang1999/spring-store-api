# 第一阶段：构建 jar
# 使用 Maven 和 Temurin JDK 17 的组合镜像 作为构建环境
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# 复制 pom.xml 和源代码
COPY pom.xml .
COPY src ./src
# 清理并打包 Spring Boot 应用，跳过测试
RUN mvn clean package -DskipTests

# 第二阶段：运行 jar
# 使用 Temurin JRE 17 作为运行环境，只包含运行时所需组件
FROM eclipse-temurin:17-jre
WORKDIR /app

# 安装 mysql-client (包含 mysqladmin) 和 netcat (包含 nc)，启动脚本中使用
RUN apt-get update \
    && apt-get install -y mysql-client netcat-traditional \
    && rm -rf /var/lib/apt/lists/*

# 复制构建好的 JAR 文件到运行阶段的镜像中
COPY --from=builder /app/target/*.jar app.jar

# 复制启动脚本
COPY docker-entrypoint.sh /app/docker-entrypoint.sh

# 赋予启动脚本可执行权限
RUN chmod +x /app/docker-entrypoint.sh

# 暴露应用端口
EXPOSE 8080

# 设置启动脚本作为容器入口
ENTRYPOINT ["/app/docker-entrypoint.sh"]
