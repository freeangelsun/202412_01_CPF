package com.cpf.starter.data.cache.valkey;

import com.cpf.common.cache.CpfCacheAsideService;
import com.cpf.common.cache.CpfCacheInvalidationCoordinator;
import com.cpf.common.cache.CpfJdbcCacheInvalidationStore;
import com.cpf.common.cache.CpfRedisCacheProvider;
import com.cpf.common.cache.CpfRedisConnectionFactoryBuilder;
import com.cpf.common.cache.CpfRedisProperties;
import com.cpf.core.api.cache.CpfCacheInvalidationPort;
import com.cpf.core.api.cache.CpfCachePort;
import com.cpf.core.api.cache.CpfDistributedLockPort;
import com.cpf.core.api.security.secret.CpfSecretProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

/** 기존 cpf.cache.provider=REDIS 계약을 Valkey/Redis leaf Starter에서 지원합니다. */
@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(CpfRedisProperties.class)
@ConditionalOnProperty(prefix = "cpf.data.cache.caffeine", name = "provider", havingValue = "REDIS")
public class CpfRedisCompatibilityAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    RedisConnectionFactory cpfRedisConnectionFactory(CpfRedisProperties properties,
                                                       ObjectProvider<CpfSecretProvider> providers,
                                                       Environment environment) {
        properties.validate(isProduction(environment));
        String referenceProvider = properties.getSecretReference().contains(":")
                ? properties.getSecretReference().substring(0, properties.getSecretReference().indexOf(':')) : "";
        CpfSecretProvider provider = providers.orderedStream()
                .filter(item -> item.providerId().equalsIgnoreCase(referenceProvider))
                .findFirst().orElse(null);
        return CpfRedisConnectionFactoryBuilder.build(properties, provider);
    }

    @Bean
    @ConditionalOnMissingBean(name = "cpfRedisBinaryTemplate")
    RedisTemplate<String, byte[]> cpfRedisBinaryTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(RedisSerializer.byteArray());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(RedisSerializer.byteArray());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    StringRedisTemplate cpfRedisStringTemplate(RedisConnectionFactory factory) { return new StringRedisTemplate(factory); }

    @Bean
    @ConditionalOnMissingBean(value = {CpfCachePort.class, CpfDistributedLockPort.class})
    CpfRedisCacheProvider cpfRedisCacheProvider(RedisTemplate<String, byte[]> template,
                                                CpfRedisProperties properties,
                                                Environment environment) {
        properties.validate(isProduction(environment));
        return new CpfRedisCacheProvider(template, properties);
    }

    @Bean
    @ConditionalOnMissingBean(CpfCacheAsideService.class)
    CpfCacheAsideService cpfCacheAsideService(CpfCachePort cache, CpfDistributedLockPort locks) {
        return new CpfCacheAsideService(cache, locks);
    }

    @Bean
    @ConditionalOnBean(JdbcTemplate.class)
    @ConditionalOnMissingBean(CpfCacheInvalidationPort.class)
    CpfCacheInvalidationPort cpfCacheInvalidationStore(JdbcTemplate jdbc) { return new CpfJdbcCacheInvalidationStore(jdbc); }

    @Bean
    @ConditionalOnBean(CpfCacheInvalidationPort.class)
    @ConditionalOnMissingBean(CpfCacheInvalidationCoordinator.class)
    CpfCacheInvalidationCoordinator cpfCacheInvalidationCoordinator(
            CpfCachePort cache, CpfCacheInvalidationPort durable,
            ObjectProvider<StringRedisTemplate> redis, CpfRedisProperties properties) {
        return new CpfCacheInvalidationCoordinator(cache, durable, redis.getIfAvailable(), properties);
    }

    @Bean
    @ConditionalOnBean(CpfCacheInvalidationCoordinator.class)
    @ConditionalOnMissingBean(name = "cpfCacheInvalidationListener")
    RedisMessageListenerContainer cpfCacheInvalidationListener(
            RedisConnectionFactory factory, CpfCacheInvalidationCoordinator coordinator,
            CpfRedisProperties properties) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        MessageListener listener = (message, pattern) -> coordinator.onFastSignal(
                new String(message.getBody(), java.nio.charset.StandardCharsets.UTF_8));
        container.addMessageListener(listener, new ChannelTopic(properties.getInvalidationChannel()));
        return container;
    }

    private boolean isProduction(Environment environment) {
        return environment != null && environment.acceptsProfiles(Profiles.of("prod", "production"));
    }
}
