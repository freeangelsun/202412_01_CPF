package com.cpf.starter.logging;

import com.cpf.common.logging.CpfApplicationLoggingPolicy;
import com.cpf.common.logging.CpfApplicationLoggingPolicyValidator;
import com.cpf.foundation.runtime.CpfInstanceIdentity;
import com.cpf.starter.logging.internal.CpfRuntimeLoggingLifecycle;
import java.time.Clock;
import java.util.LinkedHashMap;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/** 기본 Logback adapter와 cpf-common lifecycle 정책을 기존 기본 Starter에서 연결합니다. */
@AutoConfiguration
@EnableConfigurationProperties(CpfApplicationLoggingProperties.class)
@ConditionalOnClass(name = "ch.qos.logback.classic.LoggerContext")
@ConditionalOnProperty(prefix = "cpf.logging", name = "enabled", matchIfMissing = true)
public class CpfRuntimeLoggingAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    CpfApplicationLoggingPolicy cpfApplicationLoggingPolicy(
            CpfApplicationLoggingProperties properties, Environment environment) {
        String application = environment.getProperty("spring.application.name", "");
        String configuredInstance = properties.getInstanceId();
        String instance = configuredInstance == null || configuredInstance.isBlank()
                ? CpfInstanceIdentity.instanceId() : configuredInstance;
        var files = new LinkedHashMap<String, com.cpf.common.logging.CpfLogFilePolicy>();
        properties.getFiles().forEach((name, file) -> files.put(name, file.toPolicy()));
        if (properties.getMaintenanceInterval() == null
                || properties.getMaintenanceInterval().isZero()
                || properties.getMaintenanceInterval().isNegative()) {
            throw new IllegalArgumentException(
                    "cpf.logging.maintenance-interval은 0보다 커야 합니다: "
                            + properties.getMaintenanceInterval());
        }
        return new CpfApplicationLoggingPolicyValidator().validate(
                new CpfApplicationLoggingPolicy(properties.getRoot(), application, instance, files));
    }

    @Bean
    @ConditionalOnMissingBean
    CpfRuntimeLoggingLifecycle cpfRuntimeLoggingLifecycle(
            CpfApplicationLoggingPolicy policy, CpfApplicationLoggingProperties properties) {
        return new CpfRuntimeLoggingLifecycle(
                policy, properties.getMaintenanceInterval(), Clock.systemUTC());
    }
}
