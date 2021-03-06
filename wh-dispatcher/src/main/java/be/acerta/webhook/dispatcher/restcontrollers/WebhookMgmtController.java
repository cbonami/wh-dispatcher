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
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.validation.Valid;

import be.acerta.webhook.dispatcher.model.Bucket;
import be.acerta.webhook.dispatcher.model.Message;
import be.acerta.webhook.dispatcher.model.Webhook;
import be.acerta.webhook.dispatcher.persistence.WebhookRepository;
import be.acerta.webhook.dispatcher.redis.JsonUtil;
import be.acerta.webhook.dispatcher.redis.MessageDeliveryType;
import be.acerta.webhook.dispatcher.redis.RedisClient;
import be.acerta.webhook.dispatcher.redis.webhook.WebhookMessageDto;
import be.acerta.webhook.dispatcher.redis.webhook.WebhookRedisMessageProducer;
import be.acerta.webhook.dispatcher.restcontrollers.dto.BooleanDto;
import be.acerta.webhook.dispatcher.restcontrollers.dto.NewMessageDto;
import be.acerta.webhook.dispatcher.restcontrollers.dto.NewWebhookDto;
import be.acerta.webhook.dispatcher.restcontrollers.dto.RedisInfoDto;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// @EnableHypermediaSupport(type = HypermediaType.HAL_FORMS)
@RestController
@EnableHypermediaSupport(type = HypermediaType.HAL)
@Schema(title = "Manage webhooks")
@Tag(name = "Webhooks")
public class WebhookMgmtController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebhookMgmtController.class);
    public static final String WEBHOOKS_URL = "/webhooks";
    public static final String REDIS_URL = "/redis";
    private static final String BUCKETS = "buckets";
    private static final String MESSAGES = "messages";
    @Autowired
    private WebhookRepository webhookRepository;
    @Autowired
    private WebhookRedisMessageProducer webhookRedisMessageProducer;
    @Autowired
    private RedisClient redisClient;
    // @fixme nb of automatically created buckets should be an injected application
    // property
    private int nbAutoBuckets = 30;

    @Operation(summary = "Register a webhook (URL) with a unique name", description = "Used by an external party (\'application\') to register a url to which the service will dispatch (POST) messages", tags = {
        "webhook"})
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "successful operation", content = @Content(schema = @Schema(implementation = Webhook.class)))})
    @PostMapping(value = "/api" + WEBHOOKS_URL, produces = HAL_JSON_VALUE)
    public ResponseEntity<Webhook> newWebhook(@RequestBody @Valid NewWebhookDto dto) {
        Webhook applicationRequest = Webhook.builder().name(dto.getName()).url(dto.getUrl()).pubSub(dto.isPubSub())
            .build();
        Webhook webhook = webhookRepository.save(applicationRequest);
        webhook.add(linkTo(methodOn(WebhookMgmtController.class).listMessages(dto.getName())).withSelfRel());
        return ResponseEntity.ok(webhook);
    }

    @Operation(summary = "Lists all registered webhooks", tags = {"webhook"})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Webhook.class))))})
    @GetMapping(value = "/api" + WEBHOOKS_URL, produces = HAL_JSON_VALUE)
    public ResponseEntity<CollectionModel<Webhook>> listWebhooks() {
        List<Webhook> appResources = StreamSupport.stream(webhookRepository.findAll().spliterator(), false).map(wh -> {
            wh.add(linkTo(methodOn(WebhookMgmtController.class).getWebhook(wh.getName())).withSelfRel());
            wh.add(linkTo(methodOn(WebhookMgmtController.class).listBuckets(wh.getName())).withRel(BUCKETS));
            wh.add(linkTo(methodOn(WebhookMgmtController.class).listMessages(wh.getName())).withRel(MESSAGES));
            return wh;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(appResources, //
            linkTo(methodOn(WebhookMgmtController.class).listWebhooks()).withSelfRel()
                .andAffordance(afford(methodOn(WebhookMgmtController.class).newWebhook(null))).andAffordance(
                afford(methodOn(WebhookMgmtController.class).newMessage(null, null, "none", null)))));
    }

    @Operation(summary = "Returns an webhook by name", tags = {"webhook"})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Bucket.class)))})
    @GetMapping(value = "/api" + WEBHOOKS_URL + "/{whName}", produces = HAL_JSON_VALUE)
    public ResponseEntity<Webhook> getWebhook(@PathVariable("whName") String whName) {
        return webhookRepository.findByName(whName).map(wh -> {
            wh.add(linkTo(methodOn(WebhookMgmtController.class).getWebhook(wh.getName())).withSelfRel());
            wh.add(linkTo(methodOn(WebhookMgmtController.class).listBuckets(wh.getName())).withRel(BUCKETS));
            return ResponseEntity.ok(wh);
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "List all logical buckets, and the messages that they contain, that are currently being processed for a given webhook.", description = "Plz note that the results evaporate i.e. will vary from msec to msec as buckets (mini-queues) are emptied.", tags = {
        "webhook"})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Bucket.class))))})
    @GetMapping(value = "/api" + WEBHOOKS_URL + "/{whName}/buckets", produces = HAL_JSON_VALUE)
    public ResponseEntity<CollectionModel<Bucket>> listBuckets(@PathVariable("whName") String whName) {
        return webhookRepository.findByName(whName).map(wh -> {
            List<Bucket> bucketResources = getBucketsByWebhookId(wh.getId());
            return ResponseEntity.ok(CollectionModel.of(bucketResources,
                linkTo(methodOn(WebhookMgmtController.class).listBuckets(whName)).withSelfRel()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "List all messages that are currently being processed for a given webhook, grouped by logical bucket. ", description = "Messages are grouped per bucket. Plz note that the results evaporate i.e. will vary from msec to msec as buckets (mini-queues) are processed and emptied by the dispatcher processes.", tags = {
        "webhook"})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Bucket.class))))})
    @GetMapping(value = "/api" + WEBHOOKS_URL + "/{whName}/messages", produces = HAL_JSON_VALUE)
    public ResponseEntity<CollectionModel<Bucket>> listMessages(@PathVariable("whName") String whName) {
        return //
            webhookRepository.findByName(whName).map(wh -> {
                List<Bucket> bucketResources = getBucketsByWebhookId(wh.getId());
                return ResponseEntity.ok(CollectionModel.of(bucketResources,
                    linkTo(methodOn(WebhookMgmtController.class).listMessages(whName)).withSelfRel()));
            }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Return bucket with all its contained messages", tags = {"bucket"})
    @GetMapping(value = "/api" + WEBHOOKS_URL + "/{whName}/buckets/{bucketId}", produces = HAL_JSON_VALUE)
    public ResponseEntity<Bucket> getBucket(//
                                            @PathVariable("whName") String whName, //
                                            @PathVariable("bucketId") String bucketId) {
        return webhookRepository.findByName(whName).map(wh -> {
            String absBucketId = transformToAbsoluteBucketId(wh.getId(), bucketId);
            List<Message> messages = getMessagesInBucketById(absBucketId);
            Bucket bucket = Bucket.builder().id(absBucketId).messages(messages).build();
            bucket.add(linkTo(methodOn(WebhookMgmtController.class).getBucket(whName, bucketId)).withSelfRel());
            bucket.add(
                linkTo(methodOn(WebhookMgmtController.class).forceProcessing(whName, bucketId)).withRel("force"));
            bucket.add(linkTo(methodOn(WebhookMgmtController.class).triggerProcessing(whName, bucketId))
                .withRel("trigger"));
            return ResponseEntity.ok(bucket);
        }).orElse(ResponseEntity.notFound().build());
    }

    private void removeMessagesByWebhookId(String whId) {
        redisClient.getBuckets().keySet().stream().forEach(key -> {
            try {
                if (key.startsWith(whId))
                    redisClient.removeBucket(key);
            } catch (Exception e) {
                log.warn("Error while removing all messages by webhook", e);
            }
        });
    }

    private List<Message> getMessagesInBucketById(String absBucketId) {
        return //
            //
            redisClient.getBuckets().getAll(absBucketId).stream()
                .map(message -> JsonUtil.jsonToObject(message, Message.class)).collect(Collectors.toList());
    }

    private List<Bucket> getBucketsByWebhookId(String whId) {
        return StreamSupport.stream(this.redisClient.getBuckets().keySet().spliterator(), false)
            .filter(bucketId -> bucketId.startsWith(whId)).map(bucketId -> {
                List<Message> messages = getMessagesInBucketById(bucketId);
                Bucket bucket = Bucket.builder().id(bucketId).messages(messages).build();
                bucket.add(linkTo(methodOn(WebhookMgmtController.class).getBucket(whId, bucket.getId()))
                    .withSelfRel());
                return bucket;
            }).collect(Collectors.toList());
    }

    @Operation(summary = "Remove bucket with all its messages", tags = {"bucket"})
    @DeleteMapping(value = "/api" + WEBHOOKS_URL + "/{whName}/buckets/{bucketId}", produces = HAL_JSON_VALUE)
    public ResponseEntity<Void> removeBucket(@PathVariable("whName") String whName,
                                             @PathVariable("bucketId") String bucketId) {
        return webhookRepository.findByName(whName).map(wh -> {
            String absBucketId = transformToAbsoluteBucketId(wh.getId(), bucketId);
            redisClient.removeBucket(absBucketId);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Trigger processing of bucket\'s messages", tags = {"bucket"})
    @GetMapping(value = "/api" + WEBHOOKS_URL + "/{whName}/buckets/{bucketId}/trigger", produces = HAL_JSON_VALUE)
    public ResponseEntity<Void> triggerProcessing(@PathVariable("whName") String whName,
                                                  @PathVariable("bucketId") String bucketId) {
        return webhookRepository.findByName(whName).map(wh -> {
            String absBucketId = transformToAbsoluteBucketId(wh.getId(), bucketId);
            redisClient.triggerProcessing(absBucketId);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Is processor of given bucket locked ?", tags = {"bucket"})
    @GetMapping(value = "/api" + WEBHOOKS_URL + "/{whName}/buckets/{bucketId}/locked", produces = HAL_JSON_VALUE)
    public ResponseEntity<BooleanDto> isProcessorLocked(@PathVariable("whName") String whName,
                                                        @PathVariable("bucketId") String bucketId) {
        return webhookRepository.findByName(whName).map(wh -> {
            String absBucketId = transformToAbsoluteBucketId(wh.getId(), bucketId);
            redisClient.triggerProcessing(absBucketId);
            return ok(BooleanDto.booleanDto().withValue(redisClient.isProcessorLocked(absBucketId)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Force processing of bucket\'s messages", tags = {"bucket"})
    @GetMapping(value = "/api" + WEBHOOKS_URL + "/{whName}/buckets/{bucketId}/force", produces = HAL_JSON_VALUE)
    public ResponseEntity<Void> forceProcessing(@PathVariable("whName") String whName,
                                                @PathVariable("bucketId") String bucketId) {
        return webhookRepository.findByName(whName).map(wh -> {
            String absBucketId = transformToAbsoluteBucketId(wh.getId(), bucketId);
            redisClient.forceProcessing(absBucketId);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete webhook by name.", description = "Deletes an webhook and deregisters all its webhooks.", tags = {
        "webhook"})
    @DeleteMapping("/api" + WEBHOOKS_URL + "/{whName}")
    public ResponseEntity<Void> deleteApplication(@PathVariable("whName") String whName) {
        return webhookRepository.findByName(whName).map(wh -> {
            webhookRepository.delete(wh);
            removeMessagesByWebhookId(wh.getId());
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "POST a message to specified registered webhook (url).", description = "Mainly geared towards testing of the webhook-api of the destination-application."
        + " Plz note that the webhook dispatcher is kept as generic as possible, which means"
        + "that it is up to the poster to define the bucket that a message belongs to.", tags = {"message"})
    @PostMapping(value = "/api" + WEBHOOKS_URL + "/{whName}/messages", produces = {HAL_JSON_VALUE})
    public ResponseEntity<Message> newMessage( //
                                               @PathVariable("whName") String whName, //
                                               @RequestBody NewMessageDto message, //
                                               @RequestParam(value = "bucketId", defaultValue = "none") String bucketId, //
                                               @RequestHeader(value = "Content-Type", defaultValue = APPLICATION_JSON_VALUE) String mediaType) {
        return
            // put json data in an envelope
            // @fixme apply hmac encryption
            webhookRepository.findByName(whName).map(webhook -> {
                log.debug("Publishing Message [{}] to existing Webhook {}", lazy(message::toString),
                    lazy(webhook::getName));
                final String id = UUID.randomUUID().toString();
                final String idempotencyKey = "" + message.hashCode();
                WebhookMessageDto webhookMessageDto = WebhookMessageDto.builder().id(id).data(idempotencyKey)
                    .type(message.getType()).data(message.getData()).idempotencyKey(idempotencyKey)
                    .mediaType(isEmpty(mediaType) ? APPLICATION_JSON_VALUE : mediaType).webhookUrl(webhook.getUrl())
                    .delivery(MessageDeliveryType.WEBHOOK_V1.toString()).build();
                Message msg = this.webhookRedisMessageProducer.asyncSend(webhook.getId(),
                    String.valueOf(bucketId.equals("none") ? randomBucketNumber(nbAutoBuckets)
                        : transformToAbsoluteBucketId(webhook.getId(), bucketId)),
                    webhookMessageDto);
                return new ResponseEntity<>(msg, HttpStatus.CREATED);
            }).orElse(ResponseEntity.notFound().build());
    }

    private String transformToAbsoluteBucketId(String whId, String bucketId) {
        return (bucketId.contains("|")) ? bucketId : (whId + "|" + bucketId);
    }

    private int randomBucketNumber(int nbBuckets) {
        return (ThreadLocalRandom.current().nextInt(1, nbBuckets) + 1);
    }

    @DeleteMapping(value = "/api" + WEBHOOKS_URL
        + "/{whName}/buckets/{bucketId}/{messageId}", produces = HAL_JSON_VALUE)
    public ResponseEntity<Void> deleteMessage(@PathVariable("whName") String whName,
                                              @PathVariable("bucketId") String bucketId, String messageId) {
        return webhookRepository.findByName(whName).map(wh -> {
            String absBucketId = transformToAbsoluteBucketId(wh.getId(), bucketId);
            redisClient.removeMessageById(absBucketId, messageId);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    public static class Endpoint extends RepresentationModel<Endpoint> {
        private String name;

        Endpoint(final String name) {
            this.name = name;
        }

        public static class EndpointBuilder {
            private String name;

            EndpointBuilder() {
            }

            /**
             * @return {@code this}.
             */
            public WebhookMgmtController.Endpoint.EndpointBuilder name(final String name) {
                this.name = name;
                return this;
            }

            public WebhookMgmtController.Endpoint build() {
                return new WebhookMgmtController.Endpoint(this.name);
            }

            @Override
            public String toString() {
                return "WebhookMgmtController.Endpoint.EndpointBuilder(name=" + this.name + ")";
            }
        }

        public static WebhookMgmtController.Endpoint.EndpointBuilder builder() {
            return new WebhookMgmtController.Endpoint.EndpointBuilder();
        }

        public String getName() {
            return this.name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof WebhookMgmtController.Endpoint))
                return false;
            final WebhookMgmtController.Endpoint other = (WebhookMgmtController.Endpoint) o;
            if (!other.canEqual((Object) this))
                return false;
            final Object this$name = this.getName();
            final Object other$name = other.getName();
            if (this$name == null ? other$name != null : !this$name.equals(other$name))
                return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof WebhookMgmtController.Endpoint;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $name = this.getName();
            result = result * PRIME + ($name == null ? 43 : $name.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "WebhookMgmtController.Endpoint(name=" + this.getName() + ")";
        }
    }

    public static class EmptyResource extends RepresentationModel<EmptyResource> {
        EmptyResource() {
        }

        public static class EmptyResourceBuilder {
            EmptyResourceBuilder() {
            }

            public WebhookMgmtController.EmptyResource build() {
                return new WebhookMgmtController.EmptyResource();
            }

            @Override
            public String toString() {
                return "WebhookMgmtController.EmptyResource.EmptyResourceBuilder()";
            }
        }

        public static WebhookMgmtController.EmptyResource.EmptyResourceBuilder builder() {
            return new WebhookMgmtController.EmptyResource.EmptyResourceBuilder();
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof WebhookMgmtController.EmptyResource))
                return false;
            final WebhookMgmtController.EmptyResource other = (WebhookMgmtController.EmptyResource) o;
            if (!other.canEqual((Object) this))
                return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof WebhookMgmtController.EmptyResource;
        }

        @Override
        public int hashCode() {
            final int result = 1;
            return result;
        }

        @Override
        public String toString() {
            return "WebhookMgmtController.EmptyResource()";
        }
    }

    @GetMapping(value = "/api" + REDIS_URL, produces = {HAL_JSON_VALUE})
    public ResponseEntity<EmptyResource> getEndpoints() {
        EmptyResource api = EmptyResource.builder().build();
        api.add(linkTo(methodOn(WebhookMgmtController.class).getEndpoints()).withSelfRel());
        api.add(linkTo(methodOn(WebhookMgmtController.class).getInfo()).withRel("some general statistics"));
        api.add(linkTo(methodOn(WebhookMgmtController.class).clear()).withRel("clear all buckets and control lists"));
        return ResponseEntity.ok(api);
    }

    @GetMapping(produces = HAL_JSON_VALUE, value = "/api" + REDIS_URL + "/info")
    public ResponseEntity<RedisInfoDto> getInfo() {
        return ok(redisStatusDto().withRedisStatus(Lists.newArrayList(redisClient).stream()
            .map(cl -> redisGroupInfoDto().withAantalBuckets(redisClient.getBuckets().keySet().size())
                .withBucketIds(redisClient.getBuckets().keySet())
                .withAantalWachtendeBuckets(redisClient.getAwaitRetries().keySet().size())
                .withWachtendeBuckets(redisClient.getAwaitRetries().keySet()))
            .collect(toList())));
    }

    @GetMapping(produces = HAL_JSON_VALUE, value = "/api" + REDIS_URL + "/clear")
    public ResponseEntity<Void> clear() {
        redisClient.cleanRedis();
        return ResponseEntity.noContent().<Void>build();
    }
}
