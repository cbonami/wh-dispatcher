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

        @Operation(//
                        summary = "Register a webhook (URL).", //
                        description = "Used by an external party to register a url to which the dispatcher will POST messages", //
                        tags = { "webhook" })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "successful operation", content = @Content(schema = @Schema(implementation = Webhook.class))) })
        @PostMapping(value = "/api" + WEBHOOKS_URL, produces = HAL_JSON_VALUE)
        public ResponseEntity<Webhook> newWebhook(@RequestBody @Valid NewWebhookDto dto) {
                Webhook applicationRequest = Webhook.builder() //
                                .name(dto.getName()).url(dto.getUrl())//
                                .pubSub(dto.isPubSub())//
                                .build();
                Webhook webhook = webhookRepository.save(applicationRequest);
                return ResponseEntity.ok(webhook);
        }

        @Operation(//
                        summary = "List registered webhooks.", //
                        tags = { "webhook" })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Webhook.class)))) })
        @GetMapping(value = "/api" + WEBHOOKS_URL, produces = HAL_JSON_VALUE)
        public ResponseEntity<CollectionModel<Webhook>> listWebhooks() {

                List<Webhook> appResources = StreamSupport.stream(webhookRepository.findAll().spliterator(), false)
                                .map(wh -> {
                                        wh.add(linkTo(methodOn(WebhookMgmtController.class).getWebhook(wh.getId()))
                                                        .withSelfRel());
                                        wh.add(linkTo(methodOn(WebhookMgmtController.class).listBuckets(wh.getId()))
                                                        .withRel(BUCKETS));
                                        wh.add(linkTo(methodOn(WebhookMgmtController.class).listMessages(wh.getId()))
                                                        .withRel(MESSAGES));
                                        return wh;
                                }).collect(Collectors.toList());
                return ResponseEntity.ok(CollectionModel.of(appResources, //
                                linkTo(methodOn(WebhookMgmtController.class).listWebhooks()).withSelfRel() //
                                                .andAffordance(afford(
                                                                methodOn(WebhookMgmtController.class).newWebhook(null))) //
                                                .andAffordance(afford(methodOn(WebhookMgmtController.class)
                                                                .newMessage(null, null, "none", false, null)))));
        }

        @Operation(//
                        summary = "Returns an webhook by id.", //
                        tags = { "webhook" })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Bucket.class))) })
        @GetMapping(value = "/api" + WEBHOOKS_URL + "/{whId}", produces = HAL_JSON_VALUE)
        public ResponseEntity<Webhook> getWebhook(@PathVariable("whId") String whId) {
                return webhookRepository.findById(whId).map(wh -> {
                        wh.add(linkTo(methodOn(WebhookMgmtController.class).getWebhook(wh.getId())).withSelfRel());
                        wh.add(linkTo(methodOn(WebhookMgmtController.class).listBuckets(wh.getId())).withRel(BUCKETS));
                        return ResponseEntity.ok(wh);
                }).orElse(ResponseEntity.notFound().build());
        }

        @Operation(//
                        summary = "List all logical buckets, and the messages that they contain, that are currently being processed for a given webhook.", //
                        description = "Plz note that the results evaporate i.e. will vary from msec to msec as buckets (mini-queues) are emptied.", //
                        tags = { "webhook" })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Bucket.class)))) })
        @GetMapping(value = "/api" + WEBHOOKS_URL + "/{whId}/buckets", produces = HAL_JSON_VALUE)
        public ResponseEntity<CollectionModel<Bucket>> listBuckets(//
                        @PathVariable("whId") String whId) {

                return webhookRepository.findById(whId).map(wh -> {

                        List<Bucket> bucketResources = getBucketsByWebhook(whId);
                        return ResponseEntity.ok(CollectionModel.of(bucketResources, //
                                        linkTo(methodOn(WebhookMgmtController.class).listBuckets(whId)).withSelfRel()));

                }).orElse(ResponseEntity.notFound().build());

        }

        @Operation(//
                        summary = "List all messages that are currently being processed for a given webhook, grouped by logical bucket. ", //
                        description = "Messages are grouped per bucket. Plz note that the results evaporate i.e. will vary from msec to msec as buckets (mini-queues) are processed and emptied by the dispatcher processes.", //
                        tags = { "webhook" })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Bucket.class)))) })
        @GetMapping(value = "/api" + WEBHOOKS_URL + "/{whId}/messages", produces = HAL_JSON_VALUE)
        public ResponseEntity<CollectionModel<Bucket>> listMessages(@PathVariable("whId") String whId) {
                return webhookRepository.findById(whId).map(wh -> {

                        List<Bucket> bucketResources = getBucketsByWebhook(whId);

                        return ResponseEntity.ok(CollectionModel.of(bucketResources, //
                                        linkTo(methodOn(WebhookMgmtController.class).listMessages(whId))
                                                        .withSelfRel()));

                }).orElse(ResponseEntity.notFound().build());

        }

        @Operation(//
                        summary = "Return bucket with all its contained messages", //
                        tags = { "bucket" })
        @GetMapping(value = "/api" + WEBHOOKS_URL + "/{whId}/buckets/{bucketId}", produces = HAL_JSON_VALUE)
        public ResponseEntity<Bucket> getBucket(//
                        @PathVariable("whId") String whId, //
                        @PathVariable("bucketId") String bucketId) {

                return webhookRepository.findById(whId).map(wh -> {

                        List<Message> messages = getMessagesInBucket(bucketId);
                        /*
                         * if (bstr == null) return ResponseEntity.notFound().<Bucket>build();
                         */
                        Bucket bucket = Bucket.builder()//
                                        .id(bucketId)//
                                        .messages(messages)//
                                        .build();
                        bucket.add(linkTo(methodOn(WebhookMgmtController.class).getBucket(whId, bucket.getId()))
                                        .withSelfRel());
                        bucket.add(linkTo(methodOn(WebhookMgmtController.class).forceProcessing(whId, bucketId))
                                        .withRel("force"));
                        bucket.add(linkTo(methodOn(WebhookMgmtController.class).triggerProcessing(whId, bucketId))
                                        .withRel("trigger"));

                        return ResponseEntity.ok(bucket);
                }).orElse(ResponseEntity.notFound().build());

        }

        private void removeMessagesByApp(String whId) {
                redisClient.getBuckets().keySet().stream().forEach(key -> {
                        try {
                                if (key.startsWith(whId))
                                        redisClient.removeBucket(key);
                        } catch (Exception e) {
                                log.warn("Error while removing all messages by webhook", e);
                        }
                });
        }

        private List<Message> getMessagesInBucket(String bucketId) {
                return redisClient.getBuckets().getAll(bucketId).stream()//
                                .map(message -> JsonUtil.jsonToObject(message, Message.class))//
                                .collect(Collectors.toList());
        }

        private List<Bucket> getBucketsByWebhook(String whId) {
                return StreamSupport.stream(this.redisClient.getBuckets().keySet().spliterator(), false)//
                                .filter(bucketId -> bucketId.startsWith(whId))//
                                .map(bucketId -> {
                                        List<Message> messages = getMessagesInBucket(bucketId);
                                        Bucket bucket = Bucket.builder().id(bucketId).messages(messages).build();
                                        bucket.add(linkTo(methodOn(WebhookMgmtController.class).getBucket(whId,
                                                        bucket.getId())).withSelfRel());
                                        return bucket;
                                })//
                                .collect(Collectors.toList());
        }

        @Operation(//
                        summary = "Remove bucket with all its messages", //
                        tags = { "bucket" })
        @DeleteMapping(value = "/api" + WEBHOOKS_URL + "/{whId}/buckets/{bucketId}", produces = HAL_JSON_VALUE)
        public ResponseEntity<Void> removeBucket(@PathVariable("whId") String whId,
                        @PathVariable("bucketId") String bucketId) {
                return webhookRepository.findById(whId).map(wh -> {
                        // todo fix
                        redisClient.removeBucket(bucketId);
                        return ResponseEntity.noContent().<Void>build();
                }).orElse(ResponseEntity.notFound().build());

        }

        @Operation(//
                        summary = "Trigger processing of bucket's messages", //
                        tags = { "bucket" })
        @GetMapping(value = "/api" + WEBHOOKS_URL + "/{whId}/buckets/{bucketId}/trigger", produces = HAL_JSON_VALUE)
        public ResponseEntity<Void> triggerProcessing(@PathVariable("whId") String whId,
                        @PathVariable("bucketId") String bucketId) {

                return webhookRepository.findById(whId).map(wh -> {
                        redisClient.triggerProcessing(bucketId);
                        return ResponseEntity.noContent().<Void>build();
                }).orElse(ResponseEntity.notFound().build());

        }

        @Operation(//
                        summary = "Is processor of given bucket locked ?", //
                        tags = { "bucket" })
        @GetMapping(value = "/api" + WEBHOOKS_URL + "/{whId}/buckets/{bucketId}/locked", produces = HAL_JSON_VALUE)
        public ResponseEntity<BooleanDto> isProcessorLocked(@PathVariable("whId") String whId,
                        @PathVariable("bucketId") String bucketId) {
                return ok(BooleanDto.booleanDto().withValue(redisClient.isProcessorLocked(bucketId)));
        }

        @Operation(//
                        summary = "Force processing of bucket's messages", //
                        tags = { "bucket" })
        @GetMapping(value = "/api" + WEBHOOKS_URL + "/{whId}/buckets/{bucketId}/force", produces = HAL_JSON_VALUE)
        public ResponseEntity<Void> forceProcessing(@PathVariable("whId") String whId,
                        @PathVariable("bucketId") String bucketId) {
                redisClient.forceProcessing(bucketId);
                return ResponseEntity.noContent().<Void>build();
        }

        @Operation(//
                        summary = "Delete webhook by id.", //
                        description = "Deletes an webhook and deregisters all its webhooks.", //
                        tags = { "webhook" })
        @DeleteMapping(value = "/api" + WEBHOOKS_URL + "/{whId}")
        public ResponseEntity<String> deleteApplication(@PathVariable("whId") String whId) {
                return webhookRepository.findById(whId).map(wh -> {
                        webhookRepository.delete(wh);
                        removeMessagesByApp(whId);
                        return ResponseEntity.ok(wh.getId());
                }).orElse(ResponseEntity.notFound().build());
        }

        @Operation(//
                        summary = "POST a message to specified registered webhook (url).", //
                        description = "Mainly geared towards testing of the webhook-api of the destination-application."
                                        + " Plz note that the webhook dispatcher is kept as generic as possible, which means"
                                        + "that it is up to the poster to define the bucket that a message belongs to.", //
                        tags = { "message" })
        @PostMapping(value = "/api" + WEBHOOKS_URL + "/{whId}/messages", produces = { HAL_JSON_VALUE })
        public ResponseEntity<Message> newMessage( //
                        @PathVariable("whId") String whId, //
                        @RequestBody NewMessageDto message, //
                        @RequestParam(value = "bucket", defaultValue = "none") String bucketId, //
                        @RequestParam(required = false, defaultValue = "false") boolean absoluteBucketId, //
                        @RequestHeader("Content-Type") String mimeType) {

                return webhookRepository.findById(whId).map(webhook -> {
                        log.debug("Publishing Message [{}] to existing Webhook {}", lazy(message::toString),
                                        lazy(webhook::getName));

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
                                        .webhookUrl(webhook.getUrl()) //
                                        .delivery(MessageDeliveryType.WEBHOOK_V1.toString()) //
                                        .build();

                        Message msg = this.webhookRedisMessageProducer.asyncSend(whId, String.valueOf(//
                                        bucketId.equals("none") ? randomBucketNumber(30)
                                                        : transformToAbsoluteBucketId(absoluteBucketId, whId,
                                                                        bucketId)),
                                        webhookMessageDto);
                        return new ResponseEntity<>(msg, HttpStatus.CREATED);

                }).orElse(ResponseEntity.notFound().build());

        }

        private String transformToAbsoluteBucketId(boolean absoluteBucketId, String whId, String bucketId) {
                return (absoluteBucketId) ? bucketId : (whId + "|" + bucketId);
        }

        private int randomBucketNumber(int nbBuckets) {
                return (ThreadLocalRandom.current().nextInt(1, nbBuckets) + 1);
        }

        @DeleteMapping(value = "/api" + WEBHOOKS_URL + "/{whId}/messages/{messageId}", produces = HAL_JSON_VALUE)
        public ResponseEntity<Void> deleteMessage(@PathVariable("whId") String whId, String bucketId,
                        String messageId) {

                return webhookRepository.findById(whId).map(wh -> {
                        webhookRepository.delete(wh);
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
