package com.cpf.common.cache;

import com.cpf.core.api.cache.*;
import com.cpf.core.api.security.secret.CpfSecretProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.listener.*;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

/** CPF Cache Provider 선택, 보안 검증, Durable/Fast invalidation을 구성합니다. */
@AutoConfiguration
@AutoConfigureBefore(DataRedisAutoConfiguration.class)
@EnableScheduling
@EnableConfigurationProperties(CpfRedisProperties.class)
public class CpfCacheAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix="cpf.cache", name="provider", havingValue="LOCAL", matchIfMissing=true)
    @ConditionalOnMissingBean(value={CpfCachePort.class, CpfDistributedLockPort.class})
    CpfLocalCacheProvider cpfLocalCacheProvider(CpfRedisProperties properties, Environment environment) {
        properties.validate(isProduction(environment));
        return new CpfLocalCacheProvider();
    }

    @Bean
    @ConditionalOnProperty(prefix="cpf.cache", name="provider", havingValue="REDIS")
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    RedisConnectionFactory cpfRedisConnectionFactory(CpfRedisProperties properties,
            ObjectProvider<CpfSecretProvider> providers) {
        String referenceProvider = properties.getSecretReference().contains(":")
                ? properties.getSecretReference().substring(0, properties.getSecretReference().indexOf(':')) : "";
        CpfSecretProvider provider = providers.orderedStream()
                .filter(item -> item.providerId().equalsIgnoreCase(referenceProvider))
                .findFirst().orElse(null);
        return CpfRedisConnectionFactoryBuilder.build(properties, provider);
    }

    @Bean
    @ConditionalOnProperty(prefix="cpf.cache", name="provider", havingValue="REDIS")
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
    @ConditionalOnProperty(prefix="cpf.cache", name="provider", havingValue="REDIS")
    StringRedisTemplate cpfRedisStringTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    @Bean
    @ConditionalOnProperty(prefix="cpf.cache", name="provider", havingValue="REDIS")
    @ConditionalOnMissingBean(value={CpfCachePort.class, CpfDistributedLockPort.class})
    CpfRedisCacheProvider cpfRedisCacheProvider(RedisTemplate<String, byte[]> template,
                                                CpfRedisProperties properties, Environment environment) {
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
    CpfCacheInvalidationPort cpfCacheInvalidationStore(JdbcTemplate jdbc) {
        return new CpfJdbcCacheInvalidationStore(jdbc);
    }

    @Bean
    @ConditionalOnBean(CpfCacheInvalidationPort.class)
    CpfCacheInvalidationCoordinator cpfCacheInvalidationCoordinator(
            CpfCachePort cache, CpfCacheInvalidationPort durable,
            ObjectProvider<StringRedisTemplate> redis, CpfRedisProperties properties) {
        return new CpfCacheInvalidationCoordinator(cache, durable, redis.getIfAvailable(), properties);
    }

    @Bean
    @ConditionalOnBean({RedisConnectionFactory.class, CpfCacheInvalidationCoordinator.class})
    RedisMessageListenerContainer cpfCacheInvalidationListener(
            RedisConnectionFactory factory, CpfCacheInvalidationCoordinator coordinator,
            CpfRedisProperties properties) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        MessageListener listener = (message, pattern) ->
                coordinator.onFastSignal(new String(message.getBody(), java.nio.charset.StandardCharsets.UTF_8));
        container.addMessageListener(listener, new ChannelTopic(properties.getInvalidationChannel()));
        return container;
    }
    private boolean isProduction(Environment environment) {
        return environment != null && environment.acceptsProfiles(Profiles.of("prod", "production"));
    }
}
