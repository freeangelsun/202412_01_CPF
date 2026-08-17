package com.cpf.data.cache.valkey;

import com.cpf.data.cache.api.CpfCacheInvalidationPort;
import com.cpf.data.cache.api.CpfCache;
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

/** Explicit Valkey provider. Redis compatibility aliases are intentionally not supported here. */
@AutoConfiguration
@EnableConfigurationProperties(CpfValkeyProperties.class)
@ConditionalOnBean(RedisConnectionFactory.class)
@ConditionalOnProperty(prefix="cpf.data.cache.valkey", name="enabled", havingValue="true")
public class CpfValkeyAutoConfiguration {
    @Bean @ConditionalOnMissingBean(name="cpfValkeyTemplate")
    StringRedisTemplate cpfValkeyTemplate(RedisConnectionFactory factory) { return new StringRedisTemplate(factory); }

    @Bean @ConditionalOnMissingBean(CpfCacheInvalidationPort.class)
    JdbcCpfCacheInvalidationStore cpfValkeyInvalidationStore(JdbcTemplate jdbc) { return new JdbcCpfCacheInvalidationStore(jdbc); }

    @Bean @ConditionalOnMissingBean
    CpfCacheInvalidationProperties cpfValkeyInvalidationProperties(CpfValkeyProperties valkey) {
        var properties = new CpfCacheInvalidationProperties();
        properties.setInvalidationChannel(valkey.getInvalidationChannel());
        properties.validate();
        return properties;
    }

    @Bean @ConditionalOnMissingBean(CpfCache.class)
    ValkeyCpfCache cpfValkeyCachePort(StringRedisTemplate cpfValkeyTemplate, CpfValkeyProperties properties, Environment environment) {
        properties.validate();
        CpfRedisProtocolProviderSelection.requireExclusive(
                environment.getProperty("cpf.data.cache.redis.enabled", Boolean.class, false), properties.isEnabled());
        return new ValkeyCpfCache(cpfValkeyTemplate, properties, true);
    }

    @Bean @ConditionalOnMissingBean
    CpfValkeyCache cpfValkeyCache(CpfCache cache, CpfValkeyProperties properties) {
        properties.validate(); return new CpfValkeyCache(cache, properties);
    }

    @Bean @ConditionalOnMissingBean
    CpfCacheInvalidationCoordinator cpfValkeyInvalidationCoordinator(CpfCache cache, CpfCacheInvalidationPort durable,
            StringRedisTemplate cpfValkeyTemplate, CpfCacheInvalidationProperties properties) {
        return new CpfCacheInvalidationCoordinator(cache, durable,
                eventKey -> cpfValkeyTemplate.convertAndSend(properties.getInvalidationChannel(), eventKey), properties);
    }

    @Bean("cpfValkeyInvalidationListenerContainer")
    RedisMessageListenerContainer cpfValkeyInvalidationListenerContainer(RedisConnectionFactory factory,
            CpfCacheInvalidationCoordinator coordinator, CpfCacheInvalidationProperties properties) {
        var container = new RedisMessageListenerContainer(); container.setConnectionFactory(factory);
        container.addMessageListener((message, pattern) -> coordinator.onFastSignal(new String(message.getBody(), StandardCharsets.UTF_8)),
                new ChannelTopic(properties.getInvalidationChannel()));
        return container;
    }

    @Bean("cpfValkeyStartupValidator")
    CpfRedisLikeStartupValidator cpfValkeyStartupValidator(CpfCache cache, CpfValkeyProperties properties) {
        return new CpfRedisLikeStartupValidator("VALKEY", cache, properties);
    }

    @Bean("cpfValkeyHealthIndicator")
    HealthIndicator health(ValkeyCpfCache cache, CpfCacheInvalidationPort durable, CpfCacheInvalidationCoordinator coordinator) {
        return () -> cache.health().ready() ? Health.up().withDetail("provider","VALKEY")
                .withDetail("consumerId",coordinator.consumerId()).withDetail("backlog",durable.backlog(coordinator.consumerId())).build()
                : Health.down().withDetail("reasonCodes",cache.health().reasonCodes()).build();
    }
}
