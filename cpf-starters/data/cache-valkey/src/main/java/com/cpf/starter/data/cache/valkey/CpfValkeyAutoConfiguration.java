package com.cpf.starter.data.cache.valkey;

import com.cpf.common.cache.CpfCacheInvalidationCoordinator;
import com.cpf.common.cache.CpfRedisProperties;
import com.cpf.core.api.cache.CpfCacheInvalidationPort;
import com.cpf.core.api.cache.CpfCachePort;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration
@AutoConfigureBefore(name = "com.cpf.starter.data.cache.caffeine.CpfCacheAutoConfiguration")
@EnableConfigurationProperties(CpfValkeyProperties.class)
@ConditionalOnProperty(prefix = "cpf.data.cache.valkey", name = "enabled", havingValue = "true")
public class CpfValkeyAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    StringRedisTemplate cpfValkeyTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    @Bean
    @ConditionalOnMissingBean(CpfCacheInvalidationPort.class)
    JdbcCpfCacheInvalidationStore cpfCacheInvalidationStore(JdbcTemplate jdbc) {
        return new JdbcCpfCacheInvalidationStore(jdbc);
    }

    @Bean
    @ConditionalOnMissingBean
    CpfRedisProperties cpfCacheInvalidationProperties(CpfValkeyProperties valkey) {
        CpfRedisProperties properties = new CpfRedisProperties();
        properties.setInvalidationChannel(valkey.getInvalidationChannel());
        properties.validate();
        return properties;
    }

    @Bean
    @ConditionalOnMissingBean(CpfCachePort.class)
    ValkeyCpfCachePort cpfValkeyCachePort(StringRedisTemplate template, CpfValkeyProperties properties) {
        properties.validate();
        return new ValkeyCpfCachePort(template, properties, true);
    }

    /** Compatibility bean for legacy String consumers; new consumers use CpfCachePort. */
    @Bean
    @ConditionalOnMissingBean
    CpfValkeyCache cpfValkeyCache(StringRedisTemplate template, CpfValkeyProperties properties) {
        properties.validate();
        return new CpfValkeyCache(template, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    CpfCacheInvalidationCoordinator cpfCacheInvalidationCoordinator(
            CpfCachePort cache,
            CpfCacheInvalidationPort durable,
            StringRedisTemplate template,
            CpfRedisProperties properties) {
        return new CpfCacheInvalidationCoordinator(
                cache, durable,
                eventKey -> template.convertAndSend(properties.getInvalidationChannel(), eventKey),
                properties);
    }

    @Bean("cpfValkeyInvalidationListenerContainer")
    RedisMessageListenerContainer cpfValkeyInvalidationListenerContainer(
            RedisConnectionFactory factory,
            CpfCacheInvalidationCoordinator coordinator,
            CpfRedisProperties properties) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(
                (message, pattern) -> coordinator.onFastSignal(
                        new String(message.getBody(), StandardCharsets.UTF_8)),
                new ChannelTopic(properties.getInvalidationChannel()));
        return container;
    }

    @Bean("cpfValkeyHealthIndicator")
    HealthIndicator health(ValkeyCpfCachePort cache, CpfCacheInvalidationPort durable,
                           CpfCacheInvalidationCoordinator coordinator) {
        return () -> cache.health().ready()
                ? Health.up()
                    .withDetail("provider", "VALKEY")
                    .withDetail("consumerId", coordinator.consumerId())
                    .withDetail("backlog", durable.backlog(coordinator.consumerId()))
                    .build()
                : Health.down().withDetail("reasonCodes", cache.health().reasonCodes()).build();
    }
}
