#!/bin/sh

echo "Waiting for MySQL to be ready..."
# 等待 MySQL 启动并可用
# 注意：这里使用了配置文件中的正确密码和 host.docker.internal
until mysqladmin ping -h"${TEST_MYSQL_HOST_MASTER1}" -u"${TEST_MYSQL_USERNAME}" -p"${TEST_MYSQL_PASSWORD}" --silent; do
  echo "MySQL is unavailable - sleeping"
  sleep 2
done
echo "MySQL is up - continuing"

echo "Waiting for Redis to be ready..."
# 等待 Redis 启动并可用
# 需要在 Dockerfile 中安装 netcat (nc) 工具
while : ; do
  for NODE in "$TEST_REDIS_NODE1" "$TEST_REDIS_NODE2" "$TEST_REDIS_NODE3" "$TEST_REDIS_NODE4" "$TEST_REDIS_NODE5" "$TEST_REDIS_NODE6"
  do
    HOST=$(echo $NODE | cut -d: -f1)
    PORT=$(echo $NODE | cut -d: -f2)
    if nc -z "$HOST" "$PORT"; then
      echo "Redis node $HOST:$PORT is up!"; break 2
    fi
  done
  echo "No Redis node available yet, sleeping..."
  sleep 2
done
echo "At least one Redis node is up - continuing"

echo "Waiting for RabbitMQ to be ready..."
# 等待 RabbitMQ 启动并可用
# 需要在 Dockerfile 中安装 netcat (nc) 工具
while : ; do
  for NODE in "$TEST_RABBITMQ_ADDRESS1" "$TEST_RABBITMQ_ADDRESS2" "$TEST_RABBITMQ_ADDRESS3"
  do
    HOST=$(echo $NODE | cut -d: -f1)
    PORT=$(echo $NODE | cut -d: -f2)
    if nc -z "$HOST" "$PORT"; then
      echo "RabbitMQ node $HOST:$PORT is up!"; break 2
    fi
  done
  echo "No RabbitMQ node available yet, sleeping..."
  sleep 2
done
echo "At least one RabbitMQ node is up - continuing"

echo "Starting Spring Boot application with test profile..."
exec java -jar /app/app.jar --spring.profiles.active=test