package be.acerta.webhook.dispatcher.redis.maintenance;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

import javax.inject.Inject;

import be.acerta.webhook.dispatcher.redis.maintenance.dto.BooleanDto;
import be.acerta.webhook.dispatcher.redis.maintenance.dto.RedisInfoDto;
import be.acerta.webhook.dispatcher.redis.webhook.WebhookRedisClient;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//@RestController
//@RequestMapping(RedisMaintenanceController.REDIS_URL)
public class RedisMaintenanceController {

    public static final String REDIS_URL = "/redis";

    @Builder
    @Data
    public static class Endpoint extends RepresentationModel<Endpoint> {

        private String name;

    }

    @Inject
    private RedisMaintenanceService redisService;

    @GetMapping(produces = { MediaTypes.HAL_JSON_VALUE })
    public ResponseEntity<CollectionModel<Endpoint>> getEndpoints() {

        Endpoint getInfo = Endpoint.builder().name("getInfo").build();
        getInfo.add(linkTo(methodOn(RedisMaintenanceController.class).getInfo()).withRel(getInfo.getName()));
        Endpoint isProcessorLocked = Endpoint.builder().name("isProcessorLocked").build();
        isProcessorLocked.add(linkTo(methodOn(RedisMaintenanceController.class).isProcessorLocked(null))
                .withRel(isProcessorLocked.getName()));
        Endpoint clear = Endpoint.builder().name("clear").build();
        clear.add(linkTo(methodOn(RedisMaintenanceController.class).clear()).withRel(clear.getName()));
        List<Endpoint> apis = Lists.newArrayList(getInfo, isProcessorLocked, clear);
        return ResponseEntity.ok(CollectionModel.of(apis, WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(RedisMaintenanceController.class).getEndpoints()).withSelfRel()));
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, path = "/info")
    public ResponseEntity<RedisInfoDto> getInfo() {
        return ok(redisService.getRedisInfo());
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, path = "/isprocessorlocked")
    public BooleanDto isProcessorLocked(@RequestParam(name = "bucketId") String bucketId) {
        return BooleanDto.booleanDto()
                .withValue(redisService.isProcessorLocked(WebhookRedisClient.WEBHOOK_REDIS_GROUP, bucketId));
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, path = "/clear")
    public ResponseEntity<Void> clear() {
        redisService.clear();
        return ResponseEntity.noContent().<Void>build();
    }

}
