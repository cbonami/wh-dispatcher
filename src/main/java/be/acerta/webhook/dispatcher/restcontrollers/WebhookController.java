package be.acerta.webhook.dispatcher.restcontrollers;

import be.acerta.webhook.dispatcher.events.MessageReceivedEvent;
import be.acerta.webhook.dispatcher.model.Application;
import be.acerta.webhook.dispatcher.model.Message;
import be.acerta.webhook.dispatcher.persistence.ApplicationRepository;
import be.acerta.webhook.dispatcher.persistence.MessageRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;


@RestController
@RequestMapping("/applications")
@Slf4j
public class WebhookController implements ApplicationEventPublisherAware {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private MessageRepository messageRepository;

    // Event publisher
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * Register a new application (URL) returning its id
     */
    @PostMapping
    //@ApiOperation(value = "Register new application")
    public Long registerNewApplication(@RequestParam(required = true) String url, @RequestParam(required = true) String name) {

        Application applicationRequest = Application.builder().name(name).url(url).online(true).build();
        Application application = applicationRepository.save(applicationRequest);

        log.debug("Received Application {}", application.getUrl());

        return application.getId();
    }

    /**
     * List registered appliction [{id, URL},...]
     */
    //@ApiOperation(value = "List applications")
    @GetMapping
    public Iterable<Application> listApplications() {
        log.debug("Listing applications");

        return applicationRepository.findAll();
    }

    /**
     * Delete a application by id
     */
    //@ApiOperation(value = "Delete application by id")
    @DeleteMapping("/{id}")
    public void deleteApplication(@PathVariable("id") Long id) {
        Application application = getApplication(id);

        applicationRepository.delete(application);

        log.debug("Deleted Application {}", application.getUrl());
    }

    /**
     * POST a message to this application
     */
    //@ApiOperation(value = "Post message to application")
    @PostMapping("/{id}/message")
    public void postMessageToApplication(@PathVariable("id") Long id,
                                         @RequestBody String body,
                                         @RequestHeader("Content-Type") String contentType) {
        validateParam(body, "body");

        Application application = getApplication(id);

        Message message = messageRepository.save(new Message(body, contentType, application));

        log.debug("Received Message {} for Application {}", message.getId(), message.getApplication());

        // Publishes the received message's event
        applicationEventPublisher.publishEvent(new MessageReceivedEvent(this, message));
    }

    // Register event publisher
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    private Application getApplication(Long id) throws NoSuchElementException {
        Application application = applicationRepository.findById(id).get();
        if (application == null) {
            throw new NoSuchElementException("Does not exist application with ID " + id);
        }
        return application;
    }

    private void validateParam(String param, String paramName) throws IllegalArgumentException {
        if (param == null || param.isEmpty()) {
            throw new IllegalArgumentException("The '" + paramName + "' must not be null or empty");
        }
    }

}
