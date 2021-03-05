package be.acerta.webhook.dispatcher.redis;

import javax.inject.Inject;

import be.acerta.webhook.dispatcher.ApplicationProperties;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

    @Inject
    ApplicationProperties applicationProperties;

    @Bean
    @Lazy
    @ConditionalOnMissingBean(name = "RedisContainer")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.setTransportMode(TransportMode.NIO);
        config.setThreads(64);
        // config.useSingleServer()
        //     .setAddress(applicationProperties.getRedisUrl())
        //     .setPassword(applicationProperties.getRedisPassword());
        config.useSingleServer()
            .setAddress(applicationProperties.getRedisUrl());

        return Redisson.create(config);
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName(applicationProperties.getRedisHost());
        // jedisConnectionFactory.setHostName("host.docker.internal");
        jedisConnectionFactory.setPort(applicationProperties.getRedisPort());
        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

}
