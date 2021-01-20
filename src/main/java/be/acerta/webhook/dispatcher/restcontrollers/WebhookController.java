package be.acerta.webhook.dispatcher.restcontrollers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.validation.Valid;

import be.acerta.webhook.dispatcher.model.Application;
import be.acerta.webhook.dispatcher.model.Bucket;
import be.acerta.webhook.dispatcher.persistence.ApplicationRepository;
import be.acerta.webhook.dispatcher.redis.webhook.WebhookRedisMessageProducer;
import be.acerta.webhook.dispatcher.vo.ApplicationDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableHypermediaSupport(type = HypermediaType.HAL)
@RequestMapping(value = "/applications")
@Slf4j
public class WebhookController {

    private static final String BUCKETS = "buckets";

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private WebhookRedisMessageProducer webhookRedisMessageProducer;

    /**
     * Register a new application (URL) returning its id
     */
    @PostMapping(produces = {MediaTypes.HAL_JSON_VALUE})
    public ResponseEntity<Application> newApplication(@RequestBody @Valid ApplicationDto body) {
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
    @GetMapping(produces = {MediaTypes.HAL_JSON_VALUE})
    public ResponseEntity<CollectionModel<Application>> listApplications() {

        List<Application> appResources = StreamSupport.stream(applicationRepository.findAll().spliterator(), false)
                .map(app -> {
                    app.add(linkTo(methodOn(WebhookController.class).getApplication(app.getId())).withSelfRel());
                    app.add(linkTo(methodOn(WebhookController.class).listBuckets(app.getId())).withRel(BUCKETS));
                    return app;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(appResources, //
                linkTo(methodOn(WebhookController.class).listApplications()).withSelfRel() //
                        .andAffordance(afford(methodOn(WebhookController.class).newApplication(null))) //
        ));
    }

    @GetMapping(value = "/{appId}/buckets", produces = {MediaTypes.HAL_JSON_VALUE})
    public CollectionModel<Bucket> listBuckets(@PathVariable("appId") String appId) {
        // todo fix
        return CollectionModel.of(Collections.emptyList());
    }

    @GetMapping(value = "/{appId}/buckets/{bucketId}/messages", produces = {MediaTypes.HAL_JSON_VALUE})
    public CollectionModel<Bucket> listMessagesByBucket(@PathVariable("appId") String appId,
            @PathVariable("bucketId") String bucketId) {
        // todo fix
        return CollectionModel.of(Collections.emptyList());
    }

    @GetMapping(value = "/{appId}", produces = {MediaTypes.HAL_JSON_VALUE})
    public Application getApplication(@PathVariable("appId") String appId) {
        Application app = getApp(appId);
        app.add(linkTo(methodOn(WebhookController.class).getApplication(app.getId())).withSelfRel());
        app.add(linkTo(methodOn(WebhookController.class).listBuckets(app.getId())).withRel(BUCKETS));
        return app;
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
    @PostMapping(value="/{appId}/buckets/{bucketId}/messages/{messageType}", produces = {MediaTypes.HAL_JSON_VALUE})
    // todo validate params
    public void postMessageToApplication(@PathVariable("appId") String appId, @PathVariable("bucketId") String bucketId,
            @PathVariable("messageType") String messageType, @RequestBody String messageBody,
            @RequestHeader("Content-Type") String mimeType) {

        Application application = getApplication(appId);
        log.debug("Publishing Message {} for existing Application {}", messageBody, application.getName());
        this.webhookRedisMessageProducer.publish(application.getName(), application.getUrl(), bucketId, "", messageType,
                messageBody,
                StringUtils.isEmpty(mimeType) ? MediaType.APPLICATION_JSON : MimeTypeUtils.parseMimeType(mimeType));
    }

    private Application getApp(String id) throws NoSuchElementException {
        Optional<Application> application = applicationRepository.findById(id);
        if (application.isEmpty()) {
            throw new NoSuchElementException("Does not exist application with ID " + id);
        }
        return application.get();
    }

}
