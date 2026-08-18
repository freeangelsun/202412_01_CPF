package com.cpf.platform.operations.runtimecontrol;

import com.cpf.platform.operations.runtimecontrol.internal.CpfRuntimeControlPlaneRepository;
import com.cpf.platform.operations.runtimecontrol.internal.CpfRuntimeControlPlaneService;
import com.cpf.platform.operations.runtimecontrol.internal.CpfRuntimeControlReconciler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Value;

/** Runtime Control Plane의 durable DB Owner와 Public API를 조립합니다. */
@AutoConfiguration
@ConditionalOnProperty(prefix="cpf.runtime.control", name="enabled", havingValue="true", matchIfMissing=true)
public class CpfRuntimeControlAutoConfiguration {
    @Bean
    @ConditionalOnBean(name="cpfJdbcTemplate")
    @ConditionalOnMissingBean
    CpfRuntimeControlPlaneRepository cpfRuntimeControlPlaneRepository(
            @Qualifier("cpfJdbcTemplate") org.springframework.beans.factory.ObjectProvider<JdbcTemplate> jdbcTemplate, ObjectMapper objectMapper) {
        return new CpfRuntimeControlPlaneRepository(jdbcTemplate, objectMapper);
    }

    @Bean
    @ConditionalOnBean(CpfRuntimeControlPlaneRepository.class)
    @ConditionalOnMissingBean(CpfRuntimeControlPlane.class)
    CpfRuntimeControlPlane cpfRuntimeControlPlane(CpfRuntimeControlPlaneRepository repository) {
        return new CpfRuntimeControlPlaneService(repository);
    }

    @Bean
    @ConditionalOnBean({CpfRuntimeControlPlaneRepository.class, CpfRuntimeControlPlane.class})
    @ConditionalOnMissingBean
    CpfRuntimeControlReconciler cpfRuntimeControlReconciler(
            CpfRuntimeControlPlaneRepository repository, CpfRuntimeControlPlane controlPlane,
            @Value("${cpf.runtime.control.controller.id:CPF_RUNTIME_CONTROLLER}") String controllerId,
            @Value("${cpf.runtime.control.controller.lease-seconds:30}") int leaseSeconds,
            @Value("${cpf.runtime.control.controller.ack-timeout-seconds:60}") int ackTimeoutSeconds,
            @Value("${cpf.runtime.control.self-healing.allowed-change-types:}") String allowedChangeTypes,
            @Value("${cpf.runtime.control.self-healing.rate-limit-per-minute:10}") int rateLimitPerMinute,
            @Value("${cpf.runtime.control.self-healing.circuit-failure-threshold:3}") int circuitFailureThreshold,
            @Value("${cpf.runtime.control.self-healing.circuit-window-seconds:900}") int circuitWindowSeconds) {
        return new CpfRuntimeControlReconciler(repository, controlPlane, controllerId, leaseSeconds, ackTimeoutSeconds,
                allowedChangeTypes, rateLimitPerMinute, circuitFailureThreshold, circuitWindowSeconds);
    }
}
