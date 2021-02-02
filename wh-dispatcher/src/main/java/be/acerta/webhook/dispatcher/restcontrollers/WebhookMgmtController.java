package be.acerta.webhook.dispatcher.restcontrollers;

import static be.acerta.webhook.dispatcher.LazyString.lazy;
import static be.acerta.webhook.dispatcher.restcontrollers.dto.RedisGroupInfoDto.redisGroupInfoDto;
import static be.acerta.webhook.dispatcher.restcontrollers.dto.RedisInfoDto.redisStatusDto;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.validation.Valid;

import be.acerta.webhook.dispatcher.model.Webhook;
import be.acerta.webhook.dispatcher.model.Bucket;
import be.acerta.webhook.dispatcher.model.Message;
import be.acerta.webhook.dispatcher.persistence.ApplicationRepository;
import be.acerta.webhook.dispatcher.redis.JsonUtil;
import be.acerta.webhook.dispatcher.redis.RedisClient;
import be.acerta.webhook.dispatcher.redis.webhook.WebhookMessageDto;
import be.acerta.webhook.dispatcher.redis.webhook.WebhookRedisMessageProducer;
import be.acerta.webhook.dispatcher.restcontrollers.dto.BooleanDto;
import be.acerta.webhook.dispatcher.restcontrollers.dto.NewApplicationDto;
import be.acerta.webhook.dispatcher.restcontrollers.dto.NewMessageDto;
import be.acerta.webhook.dispatcher.restcontrollers.dto.RedisInfoDto;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
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
@Schema(title = "Manage webhooks")
@Tags({ @Tag(name = "Webhooks") })
@Slf4j
public class WebhookMgmtController {

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

        @Operation(//
                        summary = "Register a subscribing application i.e. a new webhook (URL).", //
                        description = "Used by an external party to make its application subscribe to messages published by our dispatcher service", //
                        tags = { "application" })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "successful operation", content = @Content(schema = @Schema(implementation = Webhook.class))) })
        @PostMapping(value = "/api" + APPLICATIONS_URL, produces = HAL_JSON_VALUE)
        public ResponseEntity<Webhook> newApplication(@RequestBody @Valid NewApplicationDto body) {
                Webhook applicationRequest = Webhook.builder() //
                                .name(body.getName()).url(body.getUrl()).online(true)//
                                .build();
                Webhook application = applicationRepository.save(applicationRequest);
                return ResponseEntity.ok(application);
        }

        @Operation(//
                        summary = "List registered applications and their webhooks.", //
                        tags = { "application" })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Webhook.class)))) })
        @GetMapping(value = "/api" + APPLICATIONS_URL, produces = HAL_JSON_VALUE)
        public ResponseEntity<CollectionModel<Webhook>> listApplications() {

                List<Webhook> appResources = StreamSupport.stream(applicationRepository.findAll().spliterator(), false)
                                .map(app -> {
                                        app.add(linkTo(methodOn(WebhookMgmtController.class)
                                                        .getApplication(app.getId())).withSelfRel());
                                        app.add(linkTo(methodOn(WebhookMgmtController.class).listBuckets(app.getId()))
                                                        .withRel(BUCKETS));
                                        app.add(linkTo(methodOn(WebhookMgmtController.class).listMessages(app.getId()))
                                                        .withRel(MESSAGES));
                                        return app;
                                }).collect(Collectors.toList());
                return ResponseEntity.ok(CollectionModel.of(appResources, //
                                linkTo(methodOn(WebhookMgmtController.class).listApplications()).withSelfRel() //
                                                .andAffordance(afford(methodOn(WebhookMgmtController.class)
                                                                .newApplication(null))) //
                                                .andAffordance(afford(methodOn(WebhookMgmtController.class)
                                                                .newMessage(null, null, -1, null)))));
        }

        @Operation(//
                        summary = "Returns an application by id.", //
                        tags = { "application" })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Bucket.class))) })
        @GetMapping(value = "/api" + APPLICATIONS_URL + "/{appId}", produces = HAL_JSON_VALUE)
        public ResponseEntity<Webhook> getApplication(@PathVariable("appId") String appId) {
                return applicationRepository.findById(appId).map(app -> {
                        app.add(linkTo(methodOn(WebhookMgmtController.class).getApplication(app.getId()))
                                        .withSelfRel());
                        app.add(linkTo(methodOn(WebhookMgmtController.class).listBuckets(app.getId()))
                                        .withRel(BUCKETS));
                        return ResponseEntity.ok(app);
                }).orElse(ResponseEntity.notFound().build());
        }

        @Operation(//
                        summary = "List all logical buckets, and the messages that they contain, that are currently being processed for a given app.", //
                        description = "Plz note that the results evaporate i.e. will vary from msec top msec as buckets (mini-queues) are emptied.", //
                        tags = { "application" })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Bucket.class)))) })
        @GetMapping(value = "/api" + APPLICATIONS_URL + "/{appId}/buckets", produces = HAL_JSON_VALUE)
        public ResponseEntity<CollectionModel<Bucket>> listBuckets(@PathVariable("appId") String appId) {

                return applicationRepository.findById(appId).map(app -> {

                        List<Bucket> bucketResources = getBucketsByApp(appId);
                        return ResponseEntity.ok(CollectionModel.of(bucketResources, //
                                        linkTo(methodOn(WebhookMgmtController.class).listBuckets(appId))
                                                        .withSelfRel()));

                }).orElse(ResponseEntity.notFound().build());

        }

        @Operation(//
                        summary = "List all messages that are currently being processed for a given app, grouped by logical bucket. ", //
                        description = "Messages are grouped per bucket. Plz note that the results evaporate i.e. will vary from msec to msec as buckets (mini-queues) are processed and emptied by the dispatcher processes.", //
                        tags = { "application" })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Bucket.class)))) })
        @GetMapping(value = "/api" + APPLICATIONS_URL + "/{appId}/messages", produces = HAL_JSON_VALUE)
        public ResponseEntity<CollectionModel<Bucket>> listMessages(@PathVariable("appId") String appId) {
                return applicationRepository.findById(appId).map(app -> {

                        List<Bucket> bucketResources = getBucketsByApp(appId);

                        return ResponseEntity.ok(CollectionModel.of(bucketResources, //
                                        linkTo(methodOn(WebhookMgmtController.class).listMessages(appId))
                                                        .withSelfRel()));

                }).orElse(ResponseEntity.notFound().build());

        }

        @Operation(//
                        summary = "Return bucket with all its contained messages", //
                        tags = { "bucket" })
        @GetMapping(value = "/api" + APPLICATIONS_URL + "/{appId}/buckets/{bucketId}", produces = HAL_JSON_VALUE)
        public ResponseEntity<Bucket> getBucket(@PathVariable("appId") String appId,
                        @PathVariable("bucketId") String bucketId) {
                                
                return applicationRepository.findById(appId).map(app -> {

                        List<Message> messages = getMessagesInBucket(bucketId);
                        /*
                         * if (bstr == null) return ResponseEntity.notFound().<Bucket>build();
                         */
                        Bucket bucket = Bucket.builder()//
                                        .id(bucketId)//
                                        .messages(messages)//
                                        .build();
                        bucket.add(linkTo(methodOn(WebhookMgmtController.class).getBucket(appId, bucket.getId()))
                                        .withSelfRel());
                        bucket.add(linkTo(methodOn(WebhookMgmtController.class).forceProcessing(appId, bucketId))
                                        .withRel("force"));
                        bucket.add(linkTo(methodOn(WebhookMgmtController.class).triggerProcessing(appId, bucketId))
                                        .withRel("trigger"));

                        return ResponseEntity.ok(bucket);
                }).orElse(ResponseEntity.notFound().build());

        }

        private void removeMessagesByApp(String appId) {
                redisClient.getBuckets().keySet().stream().forEach(key -> {
                        try {
                                if (key.startsWith(appId))
                                        redisClient.removeBucket(key);
                        } catch (Exception e) {
                                log.warn("Error while removing all messages by app", e);
                        }
                });
        }

        private List<Message> getMessagesInBucket(String bucketId) {
                return redisClient.getBuckets().getAll(bucketId).stream()//
                                .map(message -> JsonUtil.jsonToObject(message, Message.class))//
                                .collect(Collectors.toList());
        }

        private List<Bucket> getBucketsByApp(String appId) {
                return StreamSupport.stream(this.redisClient.getBuckets().keySet().spliterator(), false)//
                                .filter(bucketId -> bucketId.startsWith(appId))//
                                .map(bucketId -> {
                                        List<Message> messages = getMessagesInBucket(bucketId);
                                        Bucket bucket = Bucket.builder().id(bucketId).messages(messages).build();
                                        bucket.add(linkTo(methodOn(WebhookMgmtController.class).getBucket(appId,
                                                        bucket.getId())).withSelfRel());
                                        return bucket;
                                })//
                                .collect(Collectors.toList());
        }

        @Operation(//
                        summary = "Remove bucket with all its messages", //
                        tags = { "bucket" })
        @DeleteMapping(value = "/api" + APPLICATIONS_URL + "/{appId}/buckets/{bucketId}", produces = HAL_JSON_VALUE)
        public ResponseEntity<Void> removeBucket(@PathVariable("appId") String appId,
                        @PathVariable("bucketId") String bucketId) {
                return applicationRepository.findById(appId).map(app -> {
                        // todo fix
                        redisClient.removeBucket(bucketId);
                        return ResponseEntity.noContent().<Void>build();
                }).orElse(ResponseEntity.notFound().build());

        }

        @Operation(//
                        summary = "Trigger processing of bucket's messages", //
                        tags = { "bucket" })
        @GetMapping(value = "/api" + APPLICATIONS_URL
                        + "/{appId}/buckets/{bucketId}/trigger", produces = HAL_JSON_VALUE)
        public ResponseEntity<Void> triggerProcessing(@PathVariable("appId") String appId,
                        @PathVariable("bucketId") String bucketId) {

                return applicationRepository.findById(appId).map(app -> {
                        redisClient.triggerProcessing(bucketId);
                        return ResponseEntity.noContent().<Void>build();
                }).orElse(ResponseEntity.notFound().build());

        }

        @Operation(//
                        summary = "Is processor of given bucket locked ?", //
                        tags = { "bucket" })
        @GetMapping(value = "/api" + APPLICATIONS_URL + "/{appId}/buckets/{bucketId}/locked", produces = HAL_JSON_VALUE)
        public ResponseEntity<BooleanDto> isProcessorLocked(@PathVariable("appId") String appId,
                        @PathVariable("bucketId") String bucketId) {
                return ok(BooleanDto.booleanDto().withValue(redisClient.isProcessorLocked(bucketId)));
        }

        @Operation(//
                        summary = "Force processing of bucket's messages", //
                        tags = { "bucket" })
        @GetMapping(value = "/api" + APPLICATIONS_URL + "/{appId}/buckets/{bucketId}/force", produces = HAL_JSON_VALUE)
        public ResponseEntity<Void> forceProcessing(@PathVariable("appId") String appId,
                        @PathVariable("bucketId") String bucketId) {
                redisClient.forceProcessing(bucketId);
                return ResponseEntity.noContent().<Void>build();
        }

        @Operation(//
                        summary = "Delete application by id.", //
                        description = "Deletes an application and deregisters all its webhooks.", //
                        tags = { "application" })
        @DeleteMapping(value = "/api" + APPLICATIONS_URL + "/{appId}")
        public ResponseEntity<String> deleteApplication(@PathVariable("appId") String appId) {
                return applicationRepository.findById(appId).map(app -> {
                        applicationRepository.delete(app);
                        removeMessagesByApp(appId);
                        return ResponseEntity.ok(app.getId());
                }).orElse(ResponseEntity.notFound().build());
        }

        @Operation(//
                        summary = "POST a message to specified application over its registered webhook.", //
                        description = "Only useful during testing of the webhook interface of the destination application. Plz note that the webhook dispatcher is kept as generic as possible, which means that it is up to the poster to define the bucket that a message belongs to.", //
                        tags = { "message" })
        @PostMapping(value = "/api" + APPLICATIONS_URL + "/{appId}/messages", produces = { HAL_JSON_VALUE })
        public ResponseEntity<Message> newMessage( //
                        @PathVariable("appId") String appId, //
                        @RequestBody NewMessageDto message, //
                        @RequestParam(value = "bucket", defaultValue = "-1") int bucketNb, //
                        @RequestHeader("Content-Type") String mimeType) {

                return applicationRepository.findById(appId).map(application -> {
                        log.debug("Publishing Message [{}] to existing Application {}", lazy(message::toString),
                                        lazy(application::getName));

                        // put json data in an envelope
                        // @fixme apply hmac encryption
                        final String id = UUID.randomUUID().toString();
                        final String idempotencyKey = "" + message.hashCode();

                        WebhookMessageDto webhookMessageDto = WebhookMessageDto.builder() //
                                        .id(id) //
                                        .data(idempotencyKey) //
                                        .type(message.getType()) //
                                        .data(message.getData()) //
                                        .idempotencyKey(idempotencyKey) //
                                        .mimeType(isEmpty(mimeType) ? MediaType.APPLICATION_JSON_VALUE : mimeType) //
                                        .webhookUrl(application.getUrl()) //
                                        .build();
                        Message msg = this.webhookRedisMessageProducer.publish(appId,
                                        bucketNb == -1 ? randomBucketNumber(30) : bucketNb, webhookMessageDto);
                        return new ResponseEntity<>(msg, HttpStatus.CREATED);

                }).orElse(ResponseEntity.notFound().build());

        }

        private int randomBucketNumber(int nbBuckets) {
                return (ThreadLocalRandom.current().nextInt(1, nbBuckets) + 1);
        }

        @DeleteMapping(value = "/api" + APPLICATIONS_URL + "/{appId}/messages/{messageId}", produces = HAL_JSON_VALUE)
        public ResponseEntity<Void> deleteMessage(@PathVariable("appId") String appId, String bucketId,
                        String messageId) {

                return applicationRepository.findById(appId).map(app -> {
                        applicationRepository.delete(app);
                        redisClient.removeMessageById(bucketId, messageId);
                        return ResponseEntity.noContent().<Void>build();
                }).orElse(ResponseEntity.notFound().build());

        }

        @Builder
        @Data
        public static class Endpoint extends RepresentationModel<Endpoint> {

                private String name;

        }

        @Builder
        @Data
        public static class EmptyResource extends RepresentationModel<EmptyResource> {

        }

        @GetMapping(value = "/api" + REDIS_URL, produces = { HAL_JSON_VALUE })
        public ResponseEntity<EmptyResource> getEndpoints() {

                EmptyResource api = EmptyResource.builder().build();
                api.add(linkTo(methodOn(WebhookMgmtController.class).getEndpoints()).withSelfRel());
                api.add(linkTo(methodOn(WebhookMgmtController.class).getInfo()).withRel("some general statistics"));
                api.add(linkTo(methodOn(WebhookMgmtController.class).clear())
                                .withRel("clear all buckets and control lists"));

                return ResponseEntity.ok(api);
        }

        @GetMapping(produces = HAL_JSON_VALUE, value = "/api" + REDIS_URL + "/info")
        public ResponseEntity<RedisInfoDto> getInfo() {
                return ok(redisStatusDto().withRedisStatus(Lists.newArrayList(redisClient).stream()
                                .map(cl -> redisGroupInfoDto()
                                                .withAantalBuckets(redisClient.getBuckets().keySet().size())
                                                .withBucketIds(redisClient.getBuckets().keySet())
                                                .withAantalWachtendeBuckets(
                                                                redisClient.getAwaitRetries().keySet().size())
                                                .withWachtendeBuckets(redisClient.getAwaitRetries().keySet()))
                                .collect(toList())));
        }

        @GetMapping(produces = HAL_JSON_VALUE, value = "/api" + REDIS_URL + "/clear")
        public ResponseEntity<Void> clear() {
                redisClient.cleanRedis();
                return ResponseEntity.noContent().<Void>build();
        }

}
