package com.cpf.core.config;

import com.cpf.core.api.runtimecontrol.CpfRuntimeAgentPort;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeControlPlane;
import com.cpf.core.api.runtimecontrol.CpfRuntimeInstanceRegistration;
import com.cpf.core.common.reconciliation.CpfReconciliationPort;
import com.cpf.core.common.reconciliation.CpfReconciliationProbePort;
import com.cpf.core.common.reconciliation.CpfReconciliationRuntimePolicy;
import com.cpf.core.common.reconciliation.CpfReconciliationWorkPort;
import com.cpf.core.common.reconciliation.CpfReconciliationWorker;
import com.cpf.core.common.runtimecontrol.CpfRuntimeControlAgent;
import com.cpf.core.common.runtimecontrol.CpfRuntimeControlPlaneRepository;
import com.cpf.core.common.runtimecontrol.CpfRuntimeControlPlaneService;
import com.cpf.core.common.runtimecontrol.CpfRuntimeControlReconciler;
import com.cpf.core.common.runtimecontrol.CpfRuntimeHttpControlPlaneClient;
import com.cpf.core.common.runtimecontrol.CpfRuntimeInstanceInboxStore;
import com.cpf.core.common.runtimecontrol.applier.CpfReconciliationRuntimeApplier;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

/**
 * Topology-independent Runtime Control client.
 * Capability-specific appliers remain owned by their capability Starters.
 */
@AutoConfiguration
public class CpfRuntimeControlAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = "cpfJdbcTemplate")
    CpfRuntimeControlPlaneRepository runtimeControlPlaneRepository(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbc,
            ObjectMapper objectMapper) {
        return new CpfRuntimeControlPlaneRepository(jdbc, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(CpfRuntimeControlPlaneRepository.class)
    CpfRuntimeControlPlane runtimeControlPlane(CpfRuntimeControlPlaneRepository repository) {
        return new CpfRuntimeControlPlaneService(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(CpfRuntimeControlPlaneRepository.class)
    CpfRuntimeControlReconciler runtimeControlReconciler(
            CpfRuntimeControlPlaneRepository repository,
            CpfRuntimeControlPlane controlPlane,
            @Value("${cpf.runtime.control.controller-id:${cpf.runtime.instance-id:${cpf.framework.was-id:CPF-CONTROLLER}}}")
                    String controllerId,
            @Value("${cpf.runtime.control.controller.lease-seconds:30}") int leaseSeconds,
            @Value("${cpf.runtime.control.controller.ack-timeout-seconds:60}") int ackTimeoutSeconds) {
        return new CpfRuntimeControlReconciler(
                repository,
                controlPlane,
                controllerId,
                leaseSeconds,
                ackTimeoutSeconds);
    }

    @Bean
    @ConditionalOnMissingBean(CpfRuntimeAgentPort.class)
    @ConditionalOnProperty(prefix = "cpf.runtime.control", name = "base-url")
    CpfRuntimeAgentPort remoteRuntimeAgentPort(
            RestClient.Builder builder,
            @Value("${cpf.runtime.control.base-url}") String baseUrl,
            @Value("${cpf.runtime.control.agent-token:}") String agentToken) {
        if (agentToken == null || agentToken.isBlank()) {
            throw new IllegalStateException(
                    "분리 WAS Runtime Agent에는 cpf.runtime.control.agent-token이 필요합니다.");
        }
        return new CpfRuntimeHttpControlPlaneClient(
                builder.baseUrl(baseUrl).build(), agentToken);
    }

    @Bean
    @ConditionalOnMissingBean
    CpfReconciliationRuntimePolicy reconciliationRuntimePolicy() {
        return new CpfReconciliationRuntimePolicy();
    }

    @Bean(name = "cpfReconciliationRuntimeApplier")
    @ConditionalOnBean(CpfReconciliationRuntimePolicy.class)
    @ConditionalOnMissingBean(name = "cpfReconciliationRuntimeApplier")
    CpfRuntimeChangeApplier reconciliationRuntimeApplier(
            CpfReconciliationRuntimePolicy policy) {
        return new CpfReconciliationRuntimeApplier(policy);
    }

    @Bean
    @ConditionalOnBean({CpfReconciliationPort.class, CpfReconciliationWorkPort.class})
    @ConditionalOnMissingBean
    CpfReconciliationWorker reconciliationWorker(
            CpfReconciliationPort reconciliationPort,
            CpfReconciliationWorkPort workPort,
            CpfReconciliationRuntimePolicy policy,
            ObjectProvider<CpfReconciliationProbePort> probes,
            @Value("${cpf.runtime.instance-id:${cpf.framework.was-id:CPF-RECONCILIATION}}")
                    String instanceId) {
        return new CpfReconciliationWorker(
                reconciliationPort,
                workPort,
                policy,
                probes.orderedStream().toList(),
                instanceId);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "cpf.runtime.control.agent",
            name = "enabled",
            havingValue = "true")
    CpfRuntimeInstanceInboxStore runtimeInstanceInboxStore(
            @Value("${cpf.runtime.control.agent.inbox-path:${java.io.tmpdir}/cpf-runtime-inbox}")
                    String inboxPath,
            @Value("${cpf.runtime.instance-id:${cpf.framework.was-id:}}") String instanceId) {
        requireText(instanceId, "Runtime Agent inbox에는 instance-id가 필요합니다.");
        return new CpfRuntimeInstanceInboxStore(Path.of(inboxPath, instanceId));
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "cpf.runtime.control.agent",
            name = "enabled",
            havingValue = "true")
    CpfRuntimeControlAgent runtimeControlAgent(
            CpfRuntimeAgentPort controlPlane,
            ObjectProvider<CpfRuntimeChangeApplier> appliers,
            CpfRuntimeInstanceInboxStore inbox,
            ObjectProvider<Clock> clocks,
            @Value("${cpf.runtime.instance-id:${cpf.framework.was-id:}}") String instanceId,
            @Value("${cpf.runtime.service-id:${cpf.framework.module-id:}}") String serviceId,
            @Value("${cpf.runtime.endpoint-code:${cpf.framework.module-id:}}") String endpointCode,
            @Value("${cpf.runtime.base-url:}") String baseUrl,
            @Value("${cpf.runtime.environment:${spring.profiles.active:default}}") String environment,
            @Value("${cpf.runtime.zone:}") String zone,
            @Value("${cpf.runtime.cell:}") String cell,
            @Value("${cpf.runtime.artifact-version:${CPF_ARTIFACT_VERSION:unknown}}")
                    String artifactVersion,
            @Value("${cpf.runtime.artifact-commit:${CPF_ARTIFACT_COMMIT:unknown}}")
                    String artifactCommit,
            @Value("${cpf.runtime.role:APPLICATION}") String role,
            @Value("${cpf.runtime.registration-source:${CPF_RUNTIME_REGISTRATION_SOURCE:SELF}}")
                    String registrationSource,
            @Value("${cpf.runtime.schema-version:unknown}") String schemaVersion,
            @Value("${cpf.runtime.config-hash:unknown}") String configHash,
            @Value("${cpf.runtime.control.agent.lease-seconds:60}") int leaseSeconds) {
        requireText(
                instanceId,
                "Runtime Agent enabled=true이면 instance-id/service-id/endpoint-code/base-url이 모두 필요합니다.");
        requireText(
                serviceId,
                "Runtime Agent enabled=true이면 instance-id/service-id/endpoint-code/base-url이 모두 필요합니다.");
        requireText(
                endpointCode,
                "Runtime Agent enabled=true이면 instance-id/service-id/endpoint-code/base-url이 모두 필요합니다.");
        requireText(
                baseUrl,
                "Runtime Agent enabled=true이면 instance-id/service-id/endpoint-code/base-url이 모두 필요합니다.");

        List<CpfRuntimeChangeApplier> orderedAppliers = appliers.orderedStream().toList();
        Map<String, String> capabilities = capabilityManifest(orderedAppliers);
        Clock clock = clocks.getIfAvailable(Clock::systemUTC);
        CpfRuntimeInstanceRegistration registration = new CpfRuntimeInstanceRegistration(
                instanceId,
                serviceId,
                endpointCode,
                environment,
                zone,
                cell,
                baseUrl,
                artifactVersion,
                artifactCommit,
                role,
                registrationSource,
                schemaVersion,
                configHash,
                capabilities,
                Map.of(),
                Instant.now(clock),
                leaseSeconds);
        return new CpfRuntimeControlAgent(controlPlane, registration, orderedAppliers, inbox);
    }

    private static Map<String, String> capabilityManifest(
            List<CpfRuntimeChangeApplier> appliers) {
        Map<String, String> capabilities = new LinkedHashMap<>();
        for (CpfRuntimeChangeApplier applier : appliers) {
            String changeType = applier.changeType() == null
                    ? ""
                    : applier.changeType().trim().toUpperCase();
            if (changeType.isBlank()) {
                throw new IllegalStateException("Runtime ChangeApplier changeType이 비어 있습니다.");
            }
            String value = applier.payloadSchemaVersion()
                    + "|"
                    + applier.restartImpact().name()
                    + "|replay="
                    + applier.supportsIdempotentReplay()
                    + "|snapshot="
                    + applier.snapshotCapable();
            if (capabilities.putIfAbsent(changeType, value) != null) {
                throw new IllegalStateException("Runtime capability 중복: " + changeType);
            }
        }
        return Map.copyOf(capabilities);
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    @ConditionalOnBean(CpfRuntimeControlPlaneRepository.class)
    static class ControllerSchedulingConfiguration {}

    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    @ConditionalOnBean(CpfReconciliationWorker.class)
    static class ReconciliationSchedulingConfiguration {}

    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    @ConditionalOnProperty(
            prefix = "cpf.runtime.control.agent",
            name = "enabled",
            havingValue = "true")
    static class AgentSchedulingConfiguration {}
}
