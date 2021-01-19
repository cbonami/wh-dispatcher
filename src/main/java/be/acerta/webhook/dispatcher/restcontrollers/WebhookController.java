package be.acerta.webhook.dispatcher.restcontrollers;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.validation.Valid;

import be.acerta.webhook.dispatcher.model.Application;
import be.acerta.webhook.dispatcher.model.Bucket;
import be.acerta.webhook.dispatcher.persistence.ApplicationRepository;
import be.acerta.webhook.dispatcher.redis.webhook.WebhookRedisMessageProducer;
import be.acerta.webhook.dispatcher.vo.ApplicationDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
@RequestMapping("/applications")
@Slf4j
public class WebhookController {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private WebhookRedisMessageProducer webhookRedisMessageProducer;

    /**
     * Register a new application (URL) returning its id
     */
    @PostMapping
    public String registerNewApplication(@RequestBody @Valid ApplicationDto body) {
        Application applicationRequest = Application.builder().name(body.getName()).url(body.getUrl()).online(true)
                .build();
        Application application = applicationRepository.save(applicationRequest);
        return application.getId();
    }

    /**
     * List registered applications [{id, URL},...]
     */
    @GetMapping
    public Iterable<Application> listApplications() {
        return applicationRepository.findAll();
    }

    @GetMapping("/{appId}/buckets")
    public Iterable<Bucket> listBuckets(@PathVariable("appId") String appId) {
        // todo fix
        return Collections.emptyList();
    }

    @GetMapping("/{appId}/buckets/{bucketId}/messages")
    public Iterable<Bucket> listMessagesByBucket(@PathVariable("appId") String appId,
            @PathVariable("bucketId") String bucketId) {
        // todo fix
        return Collections.emptyList();
    }

    /**
     * Delete a application by id
     */
    @DeleteMapping("/{appId}")
    public void deleteApplication(@PathVariable("appId") String appId) {
        Application application = getApplication(appId);
        applicationRepository.delete(application);
        log.debug("Deleted Application {}", application.getUrl());
    }

    /**
     * POST a message to this application; used during testing
     */
    @PostMapping("/{appId}/buckets/{bucketId}/messages/{messageType}")
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

    private Application getApplication(String id) throws NoSuchElementException {
        Optional<Application> application = applicationRepository.findById(id);
        if (application.isEmpty()) {
            throw new NoSuchElementException("Does not exist application with ID " + id);
        }
        return application.get();
    }

}
