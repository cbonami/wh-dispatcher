package be.acerta.webhook.dispatcher.restcontrollers;

import java.util.List;

import be.acerta.webhook.dispatcher.redis.maintenance.RedisMaintenanceController;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

//@RestController
//@RequestMapping("/api")
//@EnableHypermediaSupport(type = HypermediaType.HAL)
public class ApiRootController {

    @Builder
    @Data
    public static class Api extends RepresentationModel<Api> {

        private String name;

    }

    @GetMapping(produces = { MediaTypes.HAL_JSON_VALUE })
    public ResponseEntity<CollectionModel<Api>> getApis() {
        /*
         * List<ContextResource> resources =
         * contextNmscRetailerService.findAllContexts().stream()
         * .map(ContextResource::from) .collect(Collectors.toList());
         */
        Api applications = Api.builder().name("applications").build();
        applications.add(
                linkTo(methodOn(WebhookController.class).listApplications()).withRel("api/" + applications.getName()));
        Api redis = Api.builder().name("redis").build();
        redis.add(
                linkTo(methodOn(RedisMaintenanceController.class).getEndpoints()).withRel("api/" + redis.getName()));
        List<Api> apis = Lists.newArrayList(applications, redis);
        return ResponseEntity.ok(CollectionModel.of(apis,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ApiRootController.class).getApis()).withSelfRel()));
    }

}
