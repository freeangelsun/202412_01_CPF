package com.cpf.platform.operations.runtimecontrol;

import com.cpf.foundation.runtime.CpfRuntimeMetadata;
import com.cpf.platform.operations.runtimecontrol.internal.CpfRuntimeControlAgent;
import com.cpf.platform.operations.runtimecontrol.internal.CpfRuntimeHttpControlPlaneClient;
import com.cpf.platform.operations.runtimecontrol.internal.CpfRuntimeInstanceInboxStore;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

/** Runtime Control Agent의 exactly-one 자동 wiring입니다. */
@AutoConfiguration(after = CpfRuntimeControlAutoConfiguration.class)
@EnableScheduling
public class CpfRuntimeControlAgentAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RestClient.class)
    @ConditionalOnProperty(prefix = "cpf.runtime.control.agent", name = "base-url")
    static class RemotePortConfiguration {
        @Bean
        @ConditionalOnMissingBean(CpfRuntimeAgentPort.class)
        CpfRuntimeAgentPort cpfRuntimeAgentPort(Environment environment) {
            String baseUrl = required(environment.getProperty("cpf.runtime.control.agent.base-url"), "cpf.runtime.control.agent.base-url");
            String token = first(environment.getProperty("cpf.runtime.control.agent-token"),
                    System.getenv("CPF_RUNTIME_CONTROL_AGENT_TOKEN"));
            return new CpfRuntimeHttpControlPlaneClient(RestClient.builder().baseUrl(baseUrl).build(), token);
        }
    }

    @Bean
    @ConditionalOnBean(CpfRuntimeAgentPort.class)
    @ConditionalOnMissingBean
    CpfRuntimeInstanceRegistration cpfRuntimeInstanceRegistration(CpfRuntimeMetadata runtime, Environment environment) {
        Map<String,String> capabilities = prefixed(environment, "cpf.runtime.control.agent.capability.");
        Map<String,String> labels = prefixed(environment, "cpf.runtime.control.agent.label.");
        String environmentName = first(environment.getProperty("cpf.environment"), environment.getProperty("spring.profiles.active"));
        return new CpfRuntimeInstanceRegistration(
                runtime.instanceId(),
                first(environment.getProperty("cpf.runtime.control.agent.service-id"), runtime.application()),
                first(environment.getProperty("cpf.runtime.control.agent.endpoint-code"), runtime.systemCode()),
                environmentName,
                environment.getProperty("cpf.runtime.zone"),
                environment.getProperty("cpf.runtime.cell"),
                environment.getProperty("cpf.runtime.control.agent.runtime-base-url"),
                environment.getProperty("cpf.runtime.artifact-version", "unknown"),
                environment.getProperty("cpf.runtime.artifact-commit", "unknown"),
                environment.getProperty("cpf.runtime.role", "APPLICATION"),
                "AUTO_CONFIGURATION",
                environment.getProperty("cpf.runtime.control.agent.schema-version", "1"),
                environment.getProperty("cpf.runtime.control.agent.config-hash", "UNSPECIFIED"),
                capabilities, labels, Instant.now(),
                environment.getProperty("cpf.runtime.control.agent.lease-seconds", Integer.class, 60));
    }

    @Bean
    @ConditionalOnBean(CpfRuntimeAgentPort.class)
    @ConditionalOnMissingBean
    CpfRuntimeInstanceInboxStore cpfRuntimeInstanceInboxStore(CpfRuntimeMetadata runtime, Environment environment) {
        String configured = environment.getProperty("cpf.runtime.control.agent.inbox-directory");
        Path path = configured == null || configured.isBlank()
                ? Path.of("runtime", "cpf-inbox", runtime.instanceId())
                : Path.of(configured);
        return new CpfRuntimeInstanceInboxStore(path);
    }

    @Bean
    @ConditionalOnBean({CpfRuntimeAgentPort.class, CpfRuntimeInstanceRegistration.class, CpfRuntimeInstanceInboxStore.class})
    @ConditionalOnMissingBean
    CpfRuntimeControlAgent cpfRuntimeControlAgent(
            CpfRuntimeAgentPort controlPlane, CpfRuntimeInstanceRegistration registration,
            ObjectProvider<CpfRuntimeChangeApplier> appliers, CpfRuntimeInstanceInboxStore inbox) {
        List<CpfRuntimeChangeApplier> installed = appliers.orderedStream().toList();
        return new CpfRuntimeControlAgent(controlPlane, registration, installed, inbox);
    }

    private static Map<String,String> prefixed(Environment environment, String prefix) {
        if (!(environment instanceof org.springframework.core.env.ConfigurableEnvironment configurable)) return Map.of();
        LinkedHashMap<String,String> result = new LinkedHashMap<>();
        for (org.springframework.core.env.PropertySource<?> source : configurable.getPropertySources()) {
            if (!(source instanceof org.springframework.core.env.EnumerablePropertySource<?> enumerable)) continue;
            for (String name : enumerable.getPropertyNames()) {
                if (!name.startsWith(prefix)) continue;
                String key = name.substring(prefix.length());
                String value = environment.getProperty(name);
                if (!key.isBlank() && value != null && !value.isBlank()) result.putIfAbsent(key, value.trim());
            }
        }
        return Map.copyOf(result);
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalStateException(name + " is required");
        return value.trim();
    }
    private static String first(String first, String second) {
        return first != null && !first.isBlank() ? first.trim() : (second == null || second.isBlank() ? null : second.trim());
    }
}
