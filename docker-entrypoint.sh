#!/bin/sh
echo "Waiting for MySQL to be ready..."
until mysqladmin ping -hmysql -uroot -p12345678 --silent; do
  sleep 2
done
echo "MySQL is up"
exec java -jar app.jar
