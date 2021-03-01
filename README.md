# Webhook Dispatcher

Spring Boot application that 
- offers an HTTP-API to event-subscribers which they can use to register a webhook (i.e. an endpoint URL in the subscriber's own api)
- POSTs event-payloads to these hooks as events occur at publisher's end

Events are published on redis, where they are picked up by the dispatcher, which pushes them to the hooks.

## Build app and push image

Username and password for the docker hub registry need to be passed. If you want to change the registry/repository, you can alter 

```bash
mvn package -Djib.to.auth.username=cbonami -Djib.to.auth.password=<password docker registry> -f ./wh-dispatcher/pom.xml
```

## Run app

```bash
mvn spring-boot:run -f ./wh-dispatcher/pom.xml
```

By default all dependencies (redis, admin-server, etc) are looked for on 'localhost'. You can use a couple of env variables to change this.
For example, this script will start the application when it's running inside docker-compose (as is the case with the workbench setup that uses vscode remote development):

```bash
# file: start-dispatcher-vscode.sh
export ADMIN_SERVER_HOST=admin-server
export REDIS_SERVER_HOST=redis
```

> Note: LiveReload server is also started for fast development (spring-dev-tools).

Following endpoints are exposed by the application:

```bash
# HAL Explorer; use this to TRAVERSE the api
gp preview $(gp url 8080)/browser/browser.html 

# Swagger Docs UI
gp preview $(gp url 8080)/swagger-ui.html

# Open API Spec yml file
gp preview $(gp url 8080)/v3/api-docs/

# Actuator API to inspect and manage the webservice
gp preview $(gp url 8080)/actuator
```

* [./clear-redis.sh](./clear-redis.sh) can be used to empty the redis database during testing.
* [./create-webhook.sh](./create-webhook.sh) creates a webhook that points to endpoint expose by wh-subscriber-dummy service

## Use app

Perform some HTTP-request via curl, postman, etc. Lazy people simply use the [HAL Explorer](http://localhost:8080/browser/browser.html) or the [Swagger UI](). 

```bash
# create webhook
curl -X POST $(gp url 8080)/api/webhooks -H  "accept: application/hal+json" -H  "Content-Type: application/json" -d "{\"url\":\"$(gp url 8081)/postit\",\"name\":\"someWebhook\",\"pubSub\":false}"

# emulate arriving message
curl -X POST $(gp url 8080)/api/webhooks/someWebhook/messages?bucketId=none -H  "accept: application/hal+json" -H  "Content-Type: application/json" -d "{\"type\":\"SomethingHappenedEvent\",\"data\":\"what the hell happened ?\"}"
```

## Load test

Make sure the wh-dispatcher app runs. Then:

```bash
mvn gatling:test -Dsimulation=LoadTest -Dduration=3600 -f ./wh-dispatcher/pom.xml
```

# Dummy subscribing webhook application

A 'dummy' application exposing an endpoint that we can POST to, is automatically made available on port 8081 in the development workbench.

```bash
gp preview $(gp url 8081) 
```
# Administer

A Spring Boot Admin console is made available on port 8090 in the development workbench.

```bash
gp preview $(gp url 8090) 
```

# Monitor

Point your browser to [http://localhost:3000](http://localhost:3000).
- User: grafana
- Password: grafana

Configure the prometheus datasource on [http://localhost:3000/datasources](http://localhost:3000/datasources). 

URL: http://prometheus:9090

> Note: if you want to access prometheus dashboard straight from your browser, go to [http://localhost:9090](http://localhost:9090).

Grafana offers a rich set of predefined dashboards. We're going to use the JVM dashboard. Visit http://localhost:3000/dashboard/import and either upload the configuration saved as a JSON file or paste the dashboard ulr (https://grafana.com/grafana/dashboards/4701).

# Setup vscode Dev Container

> Deprecated -- we are using Gitpod now

> Note: only works with Visual Studio Code   

Development environment is 100% pre-cooked and standard for all developers. Docker-compose is used to spin up dependencies like redis etc. The development env/container is part of the docker-compose definition, and shares the same container network with the other containers (redis etc). Ports that are opened (by the webapp) are automatically forwarded to the host machine (probably W10).

See [WSL2_DEV_ENV.md](./WSL2_DEV_ENV.md) for instructions.

# Useful commands

```
# empty redis db
redis-cli -h localhost --raw keys "*:*:*" | xargs redis-cli -h localhost del
redis-cli -h localhost keys "*" | xargs -L1 -I '$' echo '"$"' | xargs redis-cli -h localhost del

# list all keys
redis-cli -h localhost keys "*"
```


