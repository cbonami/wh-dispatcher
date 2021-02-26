#!/bin/sh
redis-cli -h redis --raw keys "*:*:*" | xargs redis-cli -h redis del
redis-cli -h redis keys "*" | xargs -L1 -I '$' echo '"$"' | xargs redis-cli -h redis del

curl -X POST "http://localhost:8080/api/webhooks" -H  "accept: application/hal+json" -H  "Content-Type: application/json" -d "{\"url\":\"http://wh-subscriber-dummy:8081/postit\",\"name\":\"workingWebhookLocal\",\"pubSub\":false}"