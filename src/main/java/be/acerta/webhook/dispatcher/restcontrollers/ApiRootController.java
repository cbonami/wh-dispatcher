package be.acerta.webhook.dispatcher.restcontrollers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves the base url of the application, pointing to the main '/api'.
 */
@RestController
@EnableHypermediaSupport(type = HypermediaType.HAL)
public class ApiRootController {

    @Builder
    @Data
    public static class EmptyResource extends RepresentationModel<EmptyResource> {

    }

    @GetMapping(value = "/", produces = { MediaTypes.HAL_JSON_VALUE })
    public ResponseEntity<EmptyResource> root() {

        EmptyResource api = EmptyResource.builder().build();
        api.add(linkTo(methodOn(ApiRootController.class).root()).withSelfRel());
        api.add(linkTo(methodOn(ApiRootController.class).api()).withRel("api"));
        //api.add(linkTo(ApiRootController.class).slash("swagger-ui.html").withRel("swagger-ui"));

        return ResponseEntity.ok(api);
    }

    @GetMapping(value = "/api", produces = { MediaTypes.HAL_JSON_VALUE })
    public ResponseEntity<EmptyResource> api() {

        EmptyResource api = EmptyResource.builder().build();
        api.add(linkTo(methodOn(ApiRootController.class).api()).withSelfRel());
        api.add(linkTo(methodOn(WebhookMgmtController.class).getEndpoints()).withRel("redis management"));
        api.add(linkTo(methodOn(WebhookMgmtController.class).listApplications()).withRel("registered applications"));
        return ResponseEntity.ok(api);
    }

}
