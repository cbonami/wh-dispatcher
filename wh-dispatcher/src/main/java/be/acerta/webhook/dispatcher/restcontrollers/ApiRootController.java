package be.acerta.webhook.dispatcher.restcontrollers;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves the base url of the application, pointing to the main '/api'.
 * Required to make this API 100% hyperlinked (HAL) from the root up.
 */
@RestController
@EnableHypermediaSupport(type = HypermediaType.HAL)
@Tag(name = "Root")
public class ApiRootController {

    @Builder
    @Data
    public static class EmptyResource extends RepresentationModel<EmptyResource> {

    }

    @GetMapping(value = "/", produces = { HAL_JSON_VALUE })
    public ResponseEntity<EmptyResource> root() {

        EmptyResource api = EmptyResource.builder().build();
        api.add(linkTo(methodOn(ApiRootController.class).root()).withSelfRel());
        api.add(linkTo(methodOn(ApiRootController.class).api()).withRel("main resource api"));

        return ResponseEntity.ok(api);
    }

    @GetMapping(value = "/api", produces = { HAL_JSON_VALUE })
    public ResponseEntity<EmptyResource> api() {

        EmptyResource api = EmptyResource.builder().build();
        api.add(linkTo(methodOn(ApiRootController.class).api()).withSelfRel());
        api.add(linkTo(methodOn(WebhookMgmtController.class).getEndpoints()).withRel("redis management"));
        api.add(linkTo(methodOn(WebhookMgmtController.class).listWebhooks()).withRel("registered webhooks"));
        return ResponseEntity.ok(api);
    }

}
