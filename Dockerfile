# 第一阶段：构建 jar
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

RUN sed -i 's@http://archive.ubuntu.com/ubuntu/@https://mirrors.tuna.tsinghua.edu.cn/ubuntu/@g' /etc/apt/sources.list \
    && apt-get update \
    && apt-get install -y mysql-client \
    && rm -rf /var/lib/apt/lists/*

COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 第二阶段：运行 jar
FROM eclipse-temurin:17-jre
WORKDIR /app
# 安装 mysql-client，包括 mysqladmin 工具
#RUN apt-get update && apt-get install -y mysql-client && rm -rf /var/lib/apt/lists/*
RUN sed -i 's@http://archive.ubuntu.com/ubuntu/@https://mirrors.tuna.tsinghua.edu.cn/ubuntu/@g' /etc/apt/sources.list \
    && apt-get update \
    && apt-get install -y mysql-client \
    && rm -rf /var/lib/apt/lists/*

# 复制构建产物
COPY --from=builder /app/target/*.jar app.jar

# 只复制启动脚本
COPY docker-entrypoint.sh /app/docker-entrypoint.sh

# 赋予可执行权限
RUN chmod +x /app/docker-entrypoint.sh

EXPOSE 8080

# 启动脚本作为容器入口
ENTRYPOINT ["/app/docker-entrypoint.sh"]
