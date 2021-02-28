#!/bin/bash
curl -X POST $(gp url 8080)/api/webhooks -H  "accept: application/hal+json" -H  "Content-Type: application/json" -d "{\"url\":\"$(gp url 8081)/postit\",\"name\":\"workingWebhookLocal\",\"pubSub\":false}"
