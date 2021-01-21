package be.acerta.webhook.dispatcher.redis.maintenance;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import javax.inject.Inject;

import be.acerta.webhook.dispatcher.redis.dto.v1.RedisInfoDtoV1;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableHypermediaSupport(type = HypermediaType.HAL)
@RequestMapping(RedisMaintenanceController.REDIS_URL)
public class RedisMaintenanceController {

    public static final String REDIS_URL = "/redis";

    @Inject
    private RedisMaintenanceService redisService;

    @GetMapping(produces = APPLICATION_JSON_VALUE, path = "/info")
    public ResponseEntity<RedisInfoDtoV1> getInfo() {
        return ok(redisService.getRedisInfo());
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, path = "/isprocessorlocked")
    public BooleanDto isProcessorLocked(@RequestParam(name = "groupId") String groupId,
            @RequestParam(name = "bucketId") String bucketId) {
        return BooleanDto.booleanDto().withValue(redisService.isProcessorLocked(groupId, bucketId));
    }

}
