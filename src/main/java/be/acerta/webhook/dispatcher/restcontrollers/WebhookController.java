package be.acerta.webhook.dispatcher.restcontrollers;

import be.acerta.webhook.dispatcher.events.MessageReceivedEvent;
import be.acerta.webhook.dispatcher.model.Application;
import be.acerta.webhook.dispatcher.model.Message;
import be.acerta.webhook.dispatcher.persistence.ApplicationRepository;
import be.acerta.webhook.dispatcher.persistence.MessageRepository;
import be.acerta.webhook.dispatcher.vo.ApplicationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.Optional;

import javax.validation.Valid;

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
    public String registerNewApplication(@RequestBody @Valid ApplicationDto body) {
        Application applicationRequest = Application.builder().name(body.getName())
                .url(body.getUrl()).online(true).build();
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
    @PostMapping("/{id}/message")
    public void postMessageToApplication(@PathVariable("id") String id, @RequestBody String body,
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

    private Application getApplication(String id) throws NoSuchElementException {
        Optional<Application> application = applicationRepository.findById(id);
        if (application.isEmpty()) {
            throw new NoSuchElementException("Does not exist application with ID " + id);
        }
        return application.get();
    }

    private void validateParam(String param, String paramName) throws IllegalArgumentException {
        if (param == null || param.isEmpty()) {
            throw new IllegalArgumentException("The '" + paramName + "' must not be null or empty");
        }
    }

}
