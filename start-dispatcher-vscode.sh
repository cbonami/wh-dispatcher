#!/bin/sh
export ADMIN_SERVER_HOST=admin-server
export REDIS_SERVER_HOST=redis
mvn spring-boot:run -f ./wh-dispatcher/pom.xml
