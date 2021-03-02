# Webhook Dispatcher

Spring Boot application that 
- offers an HTTP-API to event-subscribers which they can use to register a webhook (i.e. an endpoint URL in the subscriber's own api)
- POSTs event-payloads to these hooks as events occur at publisher's end

Events are published on redis, where they are picked up by the dispatcher, which pushes them to the hooks.
We provided guaranteed, at least-once delivery semantics.

![](./img/webhookArchitecture.svg)

Redis basically contains a logical Map-like structure that is observable. Under the hood this logical map is realized by a [Redisson MultiMap](https://redisson.org/glossary/java-multimap.html).
Each value in the map is a bucket. And a bucket is basically an ordered set of messages that is processed in a strict FIFO manner.
The key in the multimap is the concatenation '{webhookId}|{logicalBucketId}', where
- webhookId is the unique name of the webhook endpoint
- logicalBucketId can be any string; e.g. it could be the id of a customer, which would mean that all messages for customer X go to logical bucket X, and are processed in a strict order. Another option is, for example, using a modulo to determine the bucketId.
This means that the multimap is a way to assign one logical bucket-map to each registered webhook.
It is in the first place up to the sender of the message to determine the logicalBucketId that the message belongs to. However, if it is *not* specified, the dispatcher will randomly map it on one of 30 buckets. 
The number of buckets (30 by default) is configurable, but the idea behind using multiple buckets is to allow us to process messages via parallel work stealing queues (n queues/buckets instead of just 1). Problems while processing a particular bucket should not hinder or even stop processing of other messages belonging to other buckets.

The FIFO processing of messages has the following characteristics:
- a message is never processed before the previous message (in the same bucket, that is) was processed successfully
- when a message cannot be processed successfully (because the receiving service timed out or returned a non-202 statuscode), it remains in the bucket, and is retried somewhat later
- if a message is processed successfully (i.e. the webhook replied 202), the processor immediately processes the next message in the bucket (if any), and so on 
- there is some kind of exponential backoff policy that drives the retries; but note that in principle the system will keep on retrying indefinitely 
- retries are not scheduled with quartz or Spring Retry or anything else running inside the JVM; on the contrary, we rely on Redis itself to take care of the scheduling
- multiple instances of the dispatcher service and/or redis can be deployed, but Redis/Redisson makes sure that only one 'wins' when trying to process a bucket; this means that messages are only processed once (unless they are retried of course)

We also provide a REST/HATEOAS API that can be used to 
- register webhooks in the dispatcher; plz note that this information is also stored in redis (via Spring Data Redis)
- inspect the buckets and messages that are in flight
- execute simple administrative tasks like flushing buckets etc
- put messages on redis during testing

## Build app and push image

Username and password for the docker hub registry need to be passed. If you want to change the registry/repository, you can alter 

```bash
mvn package -Djib.to.auth.username=cbonami -Djib.to.auth.password=<password docker registry> -f ./wh-dispatcher/pom.xml
```

## Run app

```bash
mvn spring-boot:run -f ./wh-dispatcher/pom.xml
```

By default all dependencies (redis, admin-server, etc) are looked for on 'localhost'. You can use a couple of env variables to change this:
```bash
export ADMIN_SERVER_HOST=admin-server
export ADMIN_SERVER_PORT=8090
export REDIS_SERVER_HOST=redis
export REDIS_SERVER_PORT=6379
```
This will of course be needed when running the dispatcher service in Kubernetes.

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

Perform some HTTP-request via curl, postman, etc. Lazy people simply use the included [HAL Explorer](https://github.com/toedter/hal-explorer) or the Swagger Docs UI. 

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

# redis

Redis is pre-installed ànd started as a daemon when the workbench starts. Internally, it runs on port 6379. It's also exposed to the outside world.

`redis-cli` is also pre-installed and ready to use.

# Dummy subscribing webhook application

A 'dummy' application exposing an endpoint that we can POST to, is automatically made available on port 8081 in the development workbench.

```bash
gp preview $(gp url 8081) 
```
# Administer 

A [Spring Boot Admin UI](https://github.com/codecentric/spring-boot-admin) is made available on port 8090 in the development workbench.

```bash
gp preview $(gp url 8090) 
```

You can also use the admin server to inspects logs (or change log levels) of both the dispatcher and the dummy subscriber service.

# Monitor

Grafana is pre-installed in the workbench. You can start it as follows:

```bash
sudo ./grafana/start-grafana.sh
gp preview $(gp url 3000) 
```

Configure the prometheus here:

```bash
sudo ./grafana/start-grafana.sh
gp preview $(gp url 3000)/datasources 
```

Prometheus is also pre-installed ànd started when the workbench starts. Normally there is no need to go to its dashboard, as almost everything can be done via Grafana. Neverthless:

```bash
gp preview $(gp url 9090)
```

Grafana offers a rich set of predefined dashboards. We're going to import the JVM dashboard :

```bash
gp preview $(gp url 3000)/dashboard/import
```

Then either upload the configuration saved as a JSON file or paste the dashboard url (https://grafana.com/grafana/dashboards/4701).

> Note: /var/log/grafana/grafana.log can be a useful source of information

# plantuml

```bash
cd img
java -jar ../plantuml.jar -Djava.awt.headless=true -tsvg webhookArchitecture.puml
```

This generates an svg file which needs to be committed and pushed to GitHub. More info [here](https://gist.github.com/noamtamim/f11982b28602bd7e604c233fbe9d910f).

# Setup vscode Dev Container

> Deprecated -- we are using Gitpod now

> Relies on Visual Studio Code (vscode)  

Development environment is 100% pre-cooked and standard for all developers. Docker-compose is used to spin up dependencies like redis etc. The development env/container is part of the docker-compose definition, and shares the same container network with the other containers (redis etc). Ports that are opened (by the webapp) are automatically forwarded to the host machine (probably W10).

See [WSL2_DEV_ENV.md](./WSL2_DEV_ENV.md) for instructions.

# Useful commands

```bash
# empty redis db
redis-cli -h localhost --raw keys "*:*:*" | xargs redis-cli -h localhost del
redis-cli -h localhost keys "*" | xargs -L1 -I '$' echo '"$"' | xargs redis-cli -h localhost del

# list all keys
redis-cli -h localhost keys "*"
```


