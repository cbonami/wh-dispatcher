package be.acerta.webhook.dispatcher.restcontrollers;

import java.util.NoSuchElementException;
import java.util.Optional;

import javax.validation.Valid;

import be.acerta.webhook.dispatcher.model.Application;
import be.acerta.webhook.dispatcher.persistence.ApplicationRepository;
import be.acerta.webhook.dispatcher.persistence.MessageRepository;
import be.acerta.webhook.dispatcher.redis.webhook.WebhookRedisMessageProducer;
import be.acerta.webhook.dispatcher.vo.ApplicationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class WebhookController /* implements ApplicationEventPublisherAware */ {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private WebhookRedisMessageProducer webhookRedisMessageProducer;

    /*
     * // Event publisher private ApplicationEventPublisher
     * applicationEventPublisher;
     */
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

    /**
     * Delete a application by id
     */
    @DeleteMapping("/{id}")
    public void deleteApplication(@PathVariable("id") String id) {
        Application application = getApplication(id);
        applicationRepository.delete(application);
        log.debug("Deleted Application {}", application.getUrl());
    }

    /**
     * POST a message to this application; used during testing
     */
    @PostMapping("/{id}/queue/{queueId}/message/{messageType}")
    // todo validate params
    public void postMessageToApplication(@PathVariable("id") String appId, @PathVariable("queueId") String queueId,
            @PathVariable("messageType") String messageType, @RequestBody String messageBody) {

        Application application = getApplication(appId);
        log.debug("Publishing Message {} for existing Application {}", messageBody, application.getName());
        this.webhookRedisMessageProducer.publish(application.getName(), application.getUrl(), queueId, "", messageType,
                messageBody);
    }

    private Application getApplication(String id) throws NoSuchElementException {
        Optional<Application> application = applicationRepository.findById(id);
        if (application.isEmpty()) {
            throw new NoSuchElementException("Does not exist application with ID " + id);
        }
        return application.get();
    }

}
