package com.cpf.starter.security.session.valkey;
import com.cpf.core.api.security.CpfSessionOperations;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
@AutoConfiguration
@EnableConfigurationProperties(CpfValkeySessionProperties.class)
@ConditionalOnBean(StringRedisTemplate.class)
@ConditionalOnProperty(prefix="cpf.security.session.valkey", name="enabled", havingValue="true")
public class CpfValkeySessionAutoConfiguration {
    @Bean @ConditionalOnMissingBean CpfSessionAuditSink cpfSessionAuditSink() { return CpfSessionAuditSink.NOOP; }
    @Bean @ConditionalOnMissingBean(CpfSessionOperations.class)
    CpfSessionOperations cpfValkeySessionOperations(StringRedisTemplate redis, CpfValkeySessionProperties p, CpfSessionAuditSink audit) {
        if (p.getDefaultTtl() == null || p.getDefaultTtl().isNegative() || p.getDefaultTtl().isZero())
            throw new IllegalStateException("cpf.security.session.valkey.default-ttl must be positive");
        return new ValkeyCpfSessionOperations(redis, p, audit);
    }
}
