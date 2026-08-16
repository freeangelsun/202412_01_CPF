package com.cpf.data.lock.valkey;

import com.cpf.data.lock.api.CpfLockManager;
import com.cpf.data.lock.api.CpfLockRuntimeStatus;
import com.cpf.data.lock.spi.CpfLockAuditSink;
import com.cpf.data.lock.spi.CpfLockStore;
import com.cpf.data.lock.api.CpfLockManagers;
import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Valkey CAS Store와 fencing 기반 CPF Lock Manager를 연결한다. */
@AutoConfiguration
@EnableConfigurationProperties(CpfValkeyLockProperties.class)
@ConditionalOnBean(StringRedisTemplate.class)
@ConditionalOnProperty(prefix = "cpf.data.lock.valkey", name = "enabled", havingValue = "true")
public class CpfValkeyLockAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(CpfLockStore.class)
    CpfLockStore cpfValkeyLockStore(StringRedisTemplate redis, CpfValkeyLockProperties properties) {
        properties.validate();
        return new ValkeyCpfLockStore(redis, properties.getNamespace(), properties.getCasRetries());
    }

    @Bean
    @ConditionalOnMissingBean(CpfLockAuditSink.class)
    CpfLockAuditSink cpfLockAuditSink() {
        return CpfLockAuditSink.unavailable();
    }

    @Bean
    @ConditionalOnMissingBean(CpfLockManager.class)
    CpfLockManager cpfLockManager(CpfLockStore store, CpfLockAuditSink audit) {
        return CpfLockManagers.create(store, audit, Clock.systemUTC());
    }

    @Bean
    @ConditionalOnMissingBean(CpfLockRuntimeStatus.class)
    CpfLockRuntimeStatus cpfLockRuntimeStatus(CpfLockManager manager) {
        if (manager instanceof CpfLockRuntimeStatus status) return status;
        throw new IllegalStateException("CPF Lock Manager does not expose runtime status");
    }
}
