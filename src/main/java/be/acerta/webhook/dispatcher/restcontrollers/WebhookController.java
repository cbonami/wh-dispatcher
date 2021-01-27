package be.acerta.webhook.dispatcher.restcontrollers;

import static be.acerta.webhook.dispatcher.LazyString.lazy;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.ResponseEntity.ok;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.validation.Valid;

import be.acerta.webhook.dispatcher.model.Application;
import be.acerta.webhook.dispatcher.model.Bucket;
import be.acerta.webhook.dispatcher.model.Message;
import be.acerta.webhook.dispatcher.persistence.ApplicationRepository;
import be.acerta.webhook.dispatcher.redis.RedisClient;
import be.acerta.webhook.dispatcher.redis.maintenance.RedisMaintenanceService;
import be.acerta.webhook.dispatcher.redis.maintenance.dto.BooleanDto;
import be.acerta.webhook.dispatcher.redis.maintenance.dto.RedisInfoDto;
import be.acerta.webhook.dispatcher.redis.webhook.WebhookMessageDto;
import be.acerta.webhook.dispatcher.redis.webhook.WebhookRedisClient;
import be.acerta.webhook.dispatcher.redis.webhook.WebhookRedisMessageProducer;
import be.acerta.webhook.dispatcher.restcontrollers.dto.NewApplicationDto;
import be.acerta.webhook.dispatcher.restcontrollers.dto.NewMessageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableHypermediaSupport(type = HypermediaType.HAL)
// @EnableHypermediaSupport(type = HypermediaType.HAL_FORMS)
@Tag(name = "webhook_dispatcher", description = "the Webhook Dispatcher API")
@Slf4j
public class WebhookController {

        public static final String APPLICATIONS_URL = "/applications";
        public static final String REDIS_URL = "/redis";

        private static final String BUCKETS = "buckets";
        private static final String MESSAGES = "messages";

        @Autowired
        private ApplicationRepository applicationRepository;

        @Autowired
        private WebhookRedisMessageProducer webhookRedisMessageProducer;

        @Autowired
        private RedisClient redisClient;

        /**
         * returning its id
         */
        @Operation(//
                        summary = "Register a subscribing application i.e. a new webhook (URL).", //
                        description = "Used by an external party to make its application subscribe to messages published by our dispatcher service", //
                        tags = { "application" })
        @PostMapping(value = "/api" + APPLICATIONS_URL, produces = { HAL_JSON_VALUE })
        public ResponseEntity<Application> newApplication(@RequestBody @Valid NewApplicationDto body) {
                Application applicationRequest = Application.builder() //
                                .name(body.getName()).url(body.getUrl()).online(true)//
                                .build();
                Application application = applicationRepository.save(applicationRequest);
                return ResponseEntity.ok(application);
        }

        @Operation(//
                        summary = "List registered applications and their webhooks.", //
                        tags = { "application" })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Application.class)))) })
        @GetMapping(value = "/api" + APPLICATIONS_URL, produces = { HAL_JSON_VALUE })
        public ResponseEntity<CollectionModel<Application>> listApplications() {

                List<Application> appResources = StreamSupport
                                .stream(applicationRepository.findAll().spliterator(), false).map(app -> {
                                        app.add(linkTo(methodOn(WebhookController.class).getApplication(app.getId()))
                                                        .withSelfRel());
                                        app.add(linkTo(methodOn(WebhookController.class).listBuckets(app.getId()))
                                                        .withRel(BUCKETS));
                                        app.add(linkTo(methodOn(WebhookController.class).listMessages(app.getId()))
                                                        .withRel(MESSAGES));
                                        return app;
                                }).collect(Collectors.toList());
                return ResponseEntity.ok(CollectionModel.of(appResources, //
                                linkTo(methodOn(WebhookController.class).listApplications()).withSelfRel() //
                                                .andAffordance(afford(
                                                                methodOn(WebhookController.class).newApplication(null))) //
                                                .andAffordance(afford(methodOn(WebhookController.class).newMessage(null,
                                                                null, null, null)))));
        }

        @Operation(//
                        summary = "Returns an application by id.", //
                        tags = { "application" })
        @GetMapping(value = "/api" + APPLICATIONS_URL + "/{appId}", produces = { HAL_JSON_VALUE })
        public ResponseEntity<Application> getApplication(@PathVariable("appId") String appId) {
                return applicationRepository.findById(appId).map(app -> {
                        app.add(linkTo(methodOn(WebhookController.class).getApplication(app.getId())).withSelfRel());
                        app.add(linkTo(methodOn(WebhookController.class).listBuckets(app.getId())).withRel(BUCKETS));
                        return ResponseEntity.ok(app);
                }).orElse(ResponseEntity.notFound().build());
        }

        @Operation(//
                        summary = "List all buckets that are currently being processed for a given app.", //
                        description = "Plz note that the results evaporate i.e. will vary from msec top msec as buckets (mini-queues) are emptied.", //
                        tags = { "application" })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))) })
        @GetMapping(value = "/api" + APPLICATIONS_URL + "/{appId}/buckets", produces = { HAL_JSON_VALUE })
        public ResponseEntity<CollectionModel<EntityModel<String>>> listBuckets(@PathVariable("appId") String appId) {
                // todo fix
                return ResponseEntity.ok(CollectionModel.of(Collections.emptyList(), //
                                linkTo(methodOn(WebhookController.class).listBuckets(appId)).withSelfRel()));
        }

        @Operation(//
                        summary = "List all messages that are currently being processed for a given app. ", //
                        description = "Messages are grouped per bucket. Plz note that the results evaporate i.e. will vary from msec top msec as buckets (mini-queues) are emptied.", //
                        tags = { "application" })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Bucket.class)))) })
        @GetMapping(value = "/api" + APPLICATIONS_URL + "/{appId}/messages", produces = { HAL_JSON_VALUE })
        public ResponseEntity<CollectionModel<Bucket>> listMessages(@PathVariable("appId") String appId) {
                List<Bucket> bucketResources = StreamSupport
                                .stream(this.redisClient.getBuckets().keySet().spliterator(), false)//
                                .map(k -> {
                                        // todo
                                        final Bucket bucket = Bucket.builder().id(k).build();
                                        bucket.add(linkTo(methodOn(WebhookController.class).getBucket(appId,
                                                        bucket.getId())).withSelfRel());
                                        return bucket;
                                }).collect(Collectors.toList());
                // todo fix
                return ResponseEntity.ok(CollectionModel.of(bucketResources, //
                                linkTo(methodOn(WebhookController.class).listMessages(appId)).withSelfRel()));
        }

        @GetMapping(value = "/api" + APPLICATIONS_URL + "/{appId}/buckets/{bucketId}/messages", produces = {
                        HAL_JSON_VALUE })
        public ResponseEntity<Bucket> getBucket(@PathVariable("appId") String appId,
                        @PathVariable("bucketId") String bucketId) {
                // todo fix
                // return ResponseEntity.ok(CollectionModel.of(Collections.emptyList(), //
                // linkTo(methodOn(WebhookController.class).getBucket(appId,
                // bucketId)).withSelfRel()));

                return ResponseEntity.ok(Bucket.builder().id(bucketId).build());
        }

        @Operation(//
                        summary = "Delete application by id.", //
                        description = "Deletes an application and deregisters all its webhooks.", //
                        tags = { "application" })
        @DeleteMapping(value = "/api" + APPLICATIONS_URL + "/{appId}")
        public ResponseEntity<String> deleteApplication(@PathVariable("appId") String appId) {
                return applicationRepository.findById(appId).map(app -> {
                        applicationRepository.delete(app);
                        // todo deregister webhooks
                        return ResponseEntity.ok(app.getId());
                }).orElse(ResponseEntity.notFound().build());
        }

        @Operation(//
                        summary = "POST a message to specified application over its registered webhook.", //
                        description = "Only useful during testing of the webhook interface of the destination application. Plz note that the webhook dispatcher is kept as generic as possible, which means that it is up to the poster to define the bucket that a message belongs to.", //
                        tags = { "message" })
        @PostMapping(value = "/api" + APPLICATIONS_URL + "/{appId}/messages", produces = { HAL_JSON_VALUE })
        // todo validate params
        // {"type":"webhook_v1", "body":"bagatelle"}
        public ResponseEntity<Message> newMessage( //
                        @PathVariable("appId") String appId, //
                        @RequestBody NewMessageDto message, //
                        @RequestParam(value = "bucket", defaultValue = "*") String bucketId, //
                        @RequestHeader("Content-Type") String mimeType) {

                return applicationRepository.findById(appId).map(application -> {
                        log.debug("Publishing Message [{}] to existing Application {}", lazy(message::toString),
                                        lazy(application::getName));

                        // put json data in an envelope
                        // todo apply hmac encryption
                        final String id = UUID.randomUUID().toString();
                        final String idempotencyKey = "" + message.hashCode();

                        WebhookMessageDto webhookMessageDto = WebhookMessageDto.builder() //
                                        .id(id) //
                                        .data(idempotencyKey) //
                                        .type(message.getType()) //
                                        .data(message.getBody()) //
                                        .idempotencyKey(idempotencyKey) //
                                        .mimeType(isEmpty(mimeType) ? MediaType.APPLICATION_JSON_VALUE : mimeType) //
                                        .webhookUrl(application.getUrl()) //
                                        .build();
                        Message msg = this.webhookRedisMessageProducer.publish(application.getName(),
                                        bucketId.equals("*") ? UUID.randomUUID().toString() : bucketId,
                                        webhookMessageDto);
                        return new ResponseEntity<>(msg, HttpStatus.CREATED);
                }).orElse(ResponseEntity.notFound().build());

        }

        @Builder
        @Data
        public static class Endpoint extends RepresentationModel<Endpoint> {

                private String name;

        }

        @Inject
        private RedisMaintenanceService redisService;

        @Builder
        @Data
        public static class EmptyResource extends RepresentationModel<EmptyResource> {

        }

        @GetMapping(value = "/api" + REDIS_URL, produces = { HAL_JSON_VALUE })
        public ResponseEntity<EmptyResource> getEndpoints() {

                EmptyResource api = EmptyResource.builder().build();
                api.add(linkTo(methodOn(WebhookController.class).getEndpoints()).withSelfRel());
                api.add(linkTo(methodOn(WebhookController.class).getInfo()).withRel("some general statistics"));
                api.add(linkTo(methodOn(WebhookController.class).isProcessorLocked(null))
                                .withRel("is processor locked?"));
                api.add(linkTo(methodOn(WebhookController.class).clear())
                                .withRel("clear all buckets and control lists"));

                return ResponseEntity.ok(api);
        }

        @GetMapping(produces = HAL_JSON_VALUE, value = "/api" + REDIS_URL + "/info")
        public ResponseEntity<RedisInfoDto> getInfo() {
                return ok(redisService.getRedisInfo());
        }

        @GetMapping(produces = HAL_JSON_VALUE, value = "/api" + REDIS_URL + "/isprocessorlocked")
        public ResponseEntity<BooleanDto> isProcessorLocked(@RequestParam(name = "bucketId") String bucketId) {
                return ok(BooleanDto.booleanDto().withValue(
                                redisService.isProcessorLocked(WebhookRedisClient.WEBHOOK_REDIS_GROUP, bucketId)));
        }

        @GetMapping(produces = HAL_JSON_VALUE, value = "/api" + REDIS_URL + "/clear")
        public ResponseEntity<Void> clear() {
                redisService.clear();
                return ResponseEntity.noContent().<Void>build();
        }

}
