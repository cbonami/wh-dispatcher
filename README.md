# Webhook Dispatcher

Service that offers an API to registers webhooks at subscriber's end, and then invokes these hooks (POST) as events
occurs.

## API

Run app. API is available at [http://localhost:8080/v3/api-docs/](http://localhost:8080/v3/api-docs/)

## Use

### Register new application

```bash
	curl -X POST \
	  http://localhost:8080/applications \
	  -H 'Content-Type: application/x-www-form-urlencoded' \
	  -d 'url=http%3A%2F%2Fptsv2.com%2Fpost.php%3Fdir%3Dwebok&name=test%20app'
```

The registration service has been developed so that you can register using a registration form.

### List registered applications

```bash
curl -v --request GET http://localhost:8080/applications
```

### Delete application by ID

```bash
curl -v --request DELETE http://localhost:8080/applications/1
```

### Send (POST) a message to an application

```bash
curl -v --header "Content-type: text/pain" --request POST --data "WEBHOOK TEST" http://localhost:8080/applications/1/message
```

## Resources

https://spring.io/blog/2020/01/27/creating-docker-images-with-spring-boot-2-3-0-m1
https://www.baeldung.com/spring-rest-openapi-documentation
https://www.baeldung.com/spring-data-redis-tutorial
https://github.com/eugenp/tutorials/tree/master/persistence-modules/spring-data-redis
https://docs.docker.com/docker-for-windows/wsl/
