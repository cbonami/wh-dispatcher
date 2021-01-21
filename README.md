# Webhook Dispatcher

Spring Boot application that 
- offers to event-subscribers an HTTP-API where they can register a webhook (i.e. an endpoint URL in the subscriber's api)
- POSTs event-payloads to these hooks as events occur at publisher's end

## Setup Dev Env

### Option 1: using WSL2

= preferred option, especially when using WSL-Remote for Containers; development environment is 100% pre-cooked and standard for all developers

See [WSL2_DEV_ENV.md](./WSL2_DEV_ENV.md) for setup.

### Option 2: 100% Windows10 setup

Simply install Redis as a windows service on your local W10 machine. See instructions [here](https://medium.com/@binary10111010/redis-cli-installation-on-windows-684fb6b6ac6b).

## Run Redis

By default, Redis runs on port 6379.

## Run app

```bash
./mvnw spring-boot:run
```

* [http://localhost:8080/l](http://localhost:8080/) -> [HAL Explorer](https://github.com/toedter/hal-explorer); use this to TRAVERSE the api
* [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) -> Swagger Docs UI
* [http://localhost:8080/v3/api-docs/](http://localhost:8080/v3/api-docs/) -> Open API 3.0 JSON description.

## Use app

Perform some HTTP-request via curl, or use the examples in the [.http](src/test/resources/test.http)-files. Both IntelliJ and vscode (via an extension) can interprete these standard .http files.  

### Delete application by ID

```bash
curl -v --request DELETE http://localhost:8080/applications/1
```

### Send (POST) a message to an application

```bash
curl -v --header "Content-type: text/plain" --request POST --data "WEBHOOK TEST" http://localhost:8080/applications/1/message
```

## Useful commands

```
# empty redis db
redis-cli FLUSHDB

# 
redis-cli keys *
```


