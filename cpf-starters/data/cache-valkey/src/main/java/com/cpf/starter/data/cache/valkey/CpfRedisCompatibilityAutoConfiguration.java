package com.cpf.starter.data.cache.valkey;

import com.cpf.common.cache.CpfCacheInvalidationCoordinator;
import com.cpf.common.cache.CpfRedisProperties;
import com.cpf.core.api.cache.CpfCacheInvalidationPort;
import com.cpf.core.api.cache.CpfCachePort;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Backward-compatible alias for the former {@code cpf.data.cache.caffeine.provider=REDIS} profile.
 * The compatibility profile is still durable-first and therefore requires both Redis and JDBC.
 */
@AutoConfiguration
@AutoConfigureBefore(name = "com.cpf.starter.data.cache.caffeine.CpfCacheAutoConfiguration")
@EnableConfigurationProperties(CpfValkeyProperties.class)
@ConditionalOnBean(RedisConnectionFactory.class)
@ConditionalOnProperty(prefix = "cpf.data.cache.caffeine", name = "provider", havingValue = "REDIS")
public class CpfRedisCompatibilityAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    StringRedisTemplate cpfRedisCompatibilityTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    @Bean
    @ConditionalOnMissingBean(CpfCacheInvalidationPort.class)
    JdbcCpfCacheInvalidationStore cpfRedisCompatibilityInvalidationStore(JdbcTemplate jdbc) {
        return new JdbcCpfCacheInvalidationStore(jdbc);
    }

    @Bean
    @ConditionalOnMissingBean
    CpfRedisProperties cpfRedisCompatibilityInvalidationProperties(CpfValkeyProperties valkey) {
        CpfRedisProperties properties = new CpfRedisProperties();
        properties.setInvalidationChannel(valkey.getInvalidationChannel());
        properties.validate();
        return properties;
    }

    @Bean
    @ConditionalOnMissingBean(CpfCachePort.class)
    ValkeyCpfCachePort cpfRedisCompatibilityCachePort(
            StringRedisTemplate template,
            CpfValkeyProperties properties) {
        properties.setEnabled(true);
        properties.validate();
        return new ValkeyCpfCachePort(template, properties, true);
    }

    @Bean
    @ConditionalOnMissingBean
    CpfValkeyCache cpfRedisCompatibilityLegacyCache(
            StringRedisTemplate template,
            CpfValkeyProperties properties) {
        properties.setEnabled(true);
        properties.validate();
        return new CpfValkeyCache(template, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    CpfCacheInvalidationCoordinator cpfRedisCompatibilityInvalidationCoordinator(
            CpfCachePort cache,
            CpfCacheInvalidationPort durable,
            StringRedisTemplate template,
            CpfRedisProperties properties) {
        return new CpfCacheInvalidationCoordinator(
                cache, durable,
                eventKey -> template.convertAndSend(properties.getInvalidationChannel(), eventKey),
                properties);
    }

    @Bean("cpfRedisCompatibilityInvalidationListenerContainer")
    RedisMessageListenerContainer cpfRedisCompatibilityInvalidationListenerContainer(
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
}
