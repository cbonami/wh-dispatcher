#!/bin/sh
export ADMIN_SERVER_HOST=admin-server
export ADMIN_SERVER_PORT=9090
export REDIS_SERVER_HOST=redis
mvn spring-boot:run -f ./wh-dispatcher/pom.xml
