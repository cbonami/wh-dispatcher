package be.acerta.webhook.dispatcher.restcontrollers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.validation.Valid;

import be.acerta.webhook.dispatcher.model.Application;
import be.acerta.webhook.dispatcher.model.Bucket;
import be.acerta.webhook.dispatcher.model.Message;
import be.acerta.webhook.dispatcher.persistence.ApplicationRepository;
import be.acerta.webhook.dispatcher.redis.RedisClient;
import be.acerta.webhook.dispatcher.redis.webhook.WebhookRedisMessageProducer;
import be.acerta.webhook.dispatcher.restcontrollers.dto.NewApplicationDto;
import be.acerta.webhook.dispatcher.restcontrollers.dto.NewMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableHypermediaSupport(type = HypermediaType.HAL)
@RequestMapping(value = "/applications")
@Slf4j
public class WebhookController {

    private static final String BUCKETS = "buckets";
    private static final String MESSAGES = "messages";

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private WebhookRedisMessageProducer webhookRedisMessageProducer;

    @Autowired
    private RedisClient redisClient;

    /**
     * Register a new application (URL) returning its id
     */
    @PostMapping(produces = { MediaTypes.HAL_JSON_VALUE })
    public ResponseEntity<Application> newApplication(@RequestBody @Valid NewApplicationDto body) {
        Application applicationRequest = Application.builder() //
                .name(body.getName()).url(body.getUrl()).online(true)//
                .build();
        Application application = applicationRepository.save(applicationRequest);
        return ResponseEntity.ok(application);
    }

    // https://github.com/spring-projects/spring-hateoas-examples/blob/master/affordances/src/main/java/org/springframework/hateoas/examples/EmployeeController.java
    /**
     * List registered applications [{id, URL},...]
     */
    @GetMapping(produces = { MediaTypes.HAL_JSON_VALUE })
    public ResponseEntity<CollectionModel<Application>> listApplications() {

        List<Application> appResources = StreamSupport.stream(applicationRepository.findAll().spliterator(), false)
                .map(app -> {
                    app.add(linkTo(methodOn(WebhookController.class).getApplication(app.getId())).withSelfRel());
                    app.add(linkTo(methodOn(WebhookController.class).listBuckets(app.getId())).withRel(BUCKETS));
                    app.add(linkTo(methodOn(WebhookController.class).listMessages(app.getId())).withRel(MESSAGES));
                    return app;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(appResources, //
                linkTo(methodOn(WebhookController.class).listApplications()).withSelfRel() //
                        .andAffordance(afford(methodOn(WebhookController.class).newApplication(null))) //
                        .andAffordance(
                                afford(methodOn(WebhookController.class).newMessage(null, null, null, null)))));
    }

    /**
     * Returns all current bucketIds for an app.
     * 
     * @param appId
     * @return
     */
    @GetMapping(value = "/{appId}/buckets", produces = { MediaTypes.HAL_JSON_VALUE })
    public ResponseEntity<CollectionModel<EntityModel<String>>> listBuckets(@PathVariable("appId") String appId) {

        // todo fix
        return ResponseEntity.ok(CollectionModel.of(Collections.emptyList(), //
                linkTo(methodOn(WebhookController.class).listBuckets(appId)).withSelfRel()));
    }

    @GetMapping(value = "/{appId}/messages", produces = { MediaTypes.HAL_JSON_VALUE })
    public ResponseEntity<CollectionModel<Bucket>> listMessages(@PathVariable("appId") String appId) {
        List<Bucket> bucketResources = StreamSupport.stream(this.redisClient.getBuckets().keySet().spliterator(), false)//
                .map(k -> {
                    // todo
                    final Bucket bucket = Bucket.builder().id(k).build();
                    bucket.add(
                            linkTo(methodOn(WebhookController.class).getBucket(appId, bucket.getId())).withSelfRel());
                    return bucket;
                }).collect(Collectors.toList());
        // todo fix
        return ResponseEntity.ok(CollectionModel.of(bucketResources, //
                linkTo(methodOn(WebhookController.class).listMessages(appId)).withSelfRel()));
    }

    @GetMapping(value = "/{appId}/buckets/{bucketId}/messages", produces = { MediaTypes.HAL_JSON_VALUE })
    public ResponseEntity<Bucket> getBucket(@PathVariable("appId") String appId,
            @PathVariable("bucketId") String bucketId) {
        // todo fix
        // return ResponseEntity.ok(CollectionModel.of(Collections.emptyList(), //
        // linkTo(methodOn(WebhookController.class).getBucket(appId,
        // bucketId)).withSelfRel()));

        return ResponseEntity.ok(Bucket.builder().id(bucketId).build());
    }

    @GetMapping(value = "/{appId}", produces = { MediaTypes.HAL_JSON_VALUE })
    public ResponseEntity<Application> getApplication(@PathVariable("appId") String appId) {
        return applicationRepository.findById(appId).map(app -> {
            app.add(linkTo(methodOn(WebhookController.class).getApplication(app.getId())).withSelfRel());
            app.add(linkTo(methodOn(WebhookController.class).listBuckets(app.getId())).withRel(BUCKETS));
            return ResponseEntity.ok(app);
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete an application by id.
     */
    @DeleteMapping("/{appId}")
    public void deleteApplication(@PathVariable("appId") String appId) {
        Application application = getApp(appId);
        applicationRepository.delete(application);
        log.debug("Deleted Application {}", application.getUrl());
    }

    /**
     * POST a message to this application; used during testing. Plz note that the
     * webhook dispatcher is kept as generic as possible, which means that it is up
     * to the poster to define the bucket that a message belongs to.
     */
    @PostMapping(value = "/{appId}/messages", produces = { MediaTypes.HAL_JSON_VALUE })
    // todo validate params
    // {"type":"Foo", "body":"bar"}
    public ResponseEntity<Message> newMessage( //
            @PathVariable("appId") String appId, //
            @RequestBody NewMessageDto message, //
            // https://spring.io/guides/gs/rest-hateoas/#_create_a_resource_controller
            @RequestParam(value = "bucket", defaultValue = "no") String bucketId, //
            @RequestHeader("Content-Type") String mimeType) {

        Application application = getApp(appId);
        log.debug("Publishing Message {} for existing Application {}", message.toString(), application.getName());
        Message msg = this.webhookRedisMessageProducer.publish(application.getName(), application.getUrl(), bucketId,
                "", message.getType(), message.getBody(),
                StringUtils.isEmpty(mimeType) ? MediaType.APPLICATION_JSON : MimeTypeUtils.parseMimeType(mimeType));
        return ResponseEntity.ok(msg);
    }

    private Application getApp(String id) throws NoSuchElementException {
        Optional<Application> application = applicationRepository.findById(id);
        if (application.isEmpty()) {
            throw new NoSuchElementException("Application with ID " + id + " doesn't exist");
        }
        return application.get();
    }

}
