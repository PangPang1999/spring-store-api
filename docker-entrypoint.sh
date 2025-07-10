#!/bin/sh

echo "script for local dev"

echo "Waiting for MySQL to be ready..."
# 等待 MySQL 启动并可用
# 注意：这里使用了配置文件中的正确密码和 host.docker.internal
until mysqladmin ping -h"$HOST_DOCKER_INTERNAL" -uroot -p"myPassword!" --silent; do
  echo "MySQL is unavailable - sleeping"
  sleep 2
done
echo "MySQL is up - continuing"

echo "Waiting for Redis to be ready..."
# 等待 Redis 启动并可用
# 需要在 Dockerfile 中安装 netcat (nc) 工具
while : ; do
  if nc -z "$HOST_DOCKER_INTERNAL" 6379; then
    echo "Redis node $HOST_DOCKER_INTERNAL:6379 is up!"; break
  fi
  if nc -z "$HOST_DOCKER_INTERNAL" 6380; then
    echo "Redis node $HOST_DOCKER_INTERNAL:6380 is up!"; break
  fi
  if nc -z "$HOST_DOCKER_INTERNAL" 6381; then
    echo "Redis node $HOST_DOCKER_INTERNAL:6381 is up!"; break
  fi
  if nc -z "$HOST_DOCKER_INTERNAL" 6382; then
    echo "Redis node $HOST_DOCKER_INTERNAL:6382 is up!"; break
  fi
  if nc -z "$HOST_DOCKER_INTERNAL" 6383; then
    echo "Redis node $HOST_DOCKER_INTERNAL:6383 is up!"; break
  fi
  if nc -z "$HOST_DOCKER_INTERNAL" 6384; then
    echo "Redis node $HOST_DOCKER_INTERNAL:6384 is up!"; break
  fi
  echo "No Redis node available yet, sleeping..."
  sleep 2
done

echo "At least one Redis node is up - continuing"

echo "Waiting for RabbitMQ to be ready..."
# 等待 RabbitMQ 启动并可用
# 需要在 Dockerfile 中安装 netcat (nc) 工具
while : ; do
  if nc -z "$HOST_DOCKER_INTERNAL" 5672; then
    echo "RabbitMQ node $HOST_DOCKER_INTERNAL:5672 is up!"; break
  fi
  if nc -z "$HOST_DOCKER_INTERNAL" 5673; then
    echo "RabbitMQ node $HOST_DOCKER_INTERNAL:5673 is up!"; break
  fi
  if nc -z "$HOST_DOCKER_INTERNAL" 5674; then
    echo "RabbitMQ node $HOST_DOCKER_INTERNAL:5674 is up!"; break
  fi
  echo "No RabbitMQ node available yet, sleeping..."
  sleep 2
done

echo "At least one RabbitMQ node is up - continuing"

echo "Starting Spring Boot application with dev profile..."
# 启动 Spring Boot 应用，并激活 dev 配置文件
exec java -jar /app/app.jar --spring.profiles.active=dev