package com.cpf.data.cache.redis;

import com.cpf.data.cache.api.CpfCacheInvalidationPort;
import com.cpf.data.cache.api.CpfCachePort;
import com.cpf.data.cache.rediscommon.CpfCacheInvalidationCoordinator;
import com.cpf.data.cache.rediscommon.CpfCacheInvalidationProperties;
import com.cpf.data.cache.rediscommon.JdbcCpfCacheInvalidationStore;
import com.cpf.data.cache.rediscommon.CpfRedisProtocolProviderSelection;
import com.cpf.data.cache.rediscommon.CpfRedisLikeStartupValidator;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.jdbc.core.JdbcTemplate;

/** Explicit Redis provider. Selection is independent from the Valkey provider. */
@AutoConfiguration
@EnableConfigurationProperties(CpfRedisCacheProperties.class)
@ConditionalOnBean(RedisConnectionFactory.class)
@ConditionalOnProperty(prefix="cpf.data.cache.redis", name="enabled", havingValue="true")
public class CpfRedisCacheAutoConfiguration {
    @Bean @ConditionalOnMissingBean(name="cpfRedisCacheTemplate")
    StringRedisTemplate cpfRedisCacheTemplate(RedisConnectionFactory factory) { return new StringRedisTemplate(factory); }

    @Bean @ConditionalOnMissingBean(CpfCacheInvalidationPort.class)
    JdbcCpfCacheInvalidationStore cpfRedisInvalidationStore(JdbcTemplate jdbc) { return new JdbcCpfCacheInvalidationStore(jdbc); }

    @Bean @ConditionalOnMissingBean
    CpfCacheInvalidationProperties cpfRedisInvalidationProperties(CpfRedisCacheProperties redis) {
        var properties = new CpfCacheInvalidationProperties();
        properties.setInvalidationChannel(redis.getInvalidationChannel());
        properties.validate();
        return properties;
    }

    @Bean @ConditionalOnMissingBean(CpfCachePort.class)
    RedisCpfCachePort cpfRedisCachePort(StringRedisTemplate cpfRedisCacheTemplate, CpfRedisCacheProperties properties, Environment environment) {
        properties.validate();
        CpfRedisProtocolProviderSelection.requireExclusive(properties.isEnabled(),
                environment.getProperty("cpf.data.cache.valkey.enabled", Boolean.class, false));
        return new RedisCpfCachePort(cpfRedisCacheTemplate, properties, true);
    }

    @Bean @ConditionalOnMissingBean
    CpfCacheInvalidationCoordinator cpfRedisInvalidationCoordinator(CpfCachePort cache, CpfCacheInvalidationPort durable,
            StringRedisTemplate cpfRedisCacheTemplate, CpfCacheInvalidationProperties properties) {
        return new CpfCacheInvalidationCoordinator(cache, durable,
                eventKey -> cpfRedisCacheTemplate.convertAndSend(properties.getInvalidationChannel(), eventKey), properties);
    }

    @Bean("cpfRedisInvalidationListenerContainer")
    RedisMessageListenerContainer cpfRedisInvalidationListenerContainer(RedisConnectionFactory factory,
            CpfCacheInvalidationCoordinator coordinator, CpfCacheInvalidationProperties properties) {
        var container = new RedisMessageListenerContainer(); container.setConnectionFactory(factory);
        container.addMessageListener((message, pattern) -> coordinator.onFastSignal(new String(message.getBody(), StandardCharsets.UTF_8)),
                new ChannelTopic(properties.getInvalidationChannel()));
        return container;
    }

    @Bean("cpfRedisStartupValidator")
    CpfRedisLikeStartupValidator cpfRedisStartupValidator(CpfCachePort cache, CpfRedisCacheProperties properties) {
        return new CpfRedisLikeStartupValidator("REDIS", cache, properties);
    }

    @Bean("cpfRedisHealthIndicator")
    HealthIndicator health(RedisCpfCachePort cache, CpfCacheInvalidationPort durable, CpfCacheInvalidationCoordinator coordinator) {
        return () -> cache.health().ready() ? Health.up().withDetail("provider","REDIS")
                .withDetail("consumerId",coordinator.consumerId()).withDetail("backlog",durable.backlog(coordinator.consumerId())).build()
                : Health.down().withDetail("reasonCodes",cache.health().reasonCodes()).build();
    }
}
