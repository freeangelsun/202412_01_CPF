package com.cpf.security.session.valkey;

import com.cpf.security.api.CpfSessionMetrics;
import com.cpf.security.api.CpfSessionOperations;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/** 명시적으로 선택된 Valkey Session Provider를 연결한다. */
@AutoConfiguration
@EnableConfigurationProperties(CpfValkeySessionProperties.class)
@ConditionalOnBean(StringRedisTemplate.class)
@ConditionalOnProperty(prefix = "cpf.security.session.valkey", name = "enabled", havingValue = "true")
public class CpfValkeySessionAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    CpfSessionAuditSink cpfSessionAuditSink() {
        return CpfSessionAuditSink.NOOP;
    }

    @Bean
    @ConditionalOnMissingBean(CpfSessionOperations.class)
    MeteredCpfSessionOperations cpfValkeySessionOperations(
            StringRedisTemplate redis, CpfValkeySessionProperties properties, CpfSessionAuditSink audit) {
        properties.validate();
        return new MeteredCpfSessionOperations(new ValkeyCpfSessionOperations(redis, properties, audit));
    }

    @Bean
    @ConditionalOnMissingBean(CpfSessionMetrics.class)
    CpfSessionMetrics cpfSessionMetrics(MeteredCpfSessionOperations operations) {
        return operations;
    }
}
