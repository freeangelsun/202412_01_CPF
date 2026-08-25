package com.cpf.platform.operations.runtimecontrol;

import com.cpf.foundation.runtime.CpfRuntimeMetadata;
import com.cpf.platform.operations.runtimecontrol.internal.CpfRuntimeControlAgent;
import com.cpf.platform.operations.runtimecontrol.internal.CpfRuntimeHttpControlPlaneClient;
import com.cpf.platform.operations.runtimecontrol.internal.CpfRuntimeInstanceInboxStore;
import java.net.URI;
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
import org.springframework.core.env.Profiles;
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
                first(environment.getProperty("cpf.runtime.control.agent.service-id"), runtime.systemCode()),
                first(environment.getProperty("cpf.runtime.control.agent.endpoint-code"), runtime.systemCode() + "_API"),
                environmentName,
                environment.getProperty("cpf.runtime.zone"),
                environment.getProperty("cpf.runtime.cell"),
                resolveRuntimeBaseUrl(runtime, environment),
                environment.getProperty("cpf.runtime.artifact-version", "unknown"),
                environment.getProperty("cpf.runtime.artifact-commit", "unknown"),
                environment.getProperty("cpf.runtime.role", "APPLICATION"),
                "AUTO_CONFIGURATION",
                environment.getProperty("cpf.runtime.control.agent.schema-version", "1"),
                environment.getProperty("cpf.runtime.control.agent.config-hash", "UNSPECIFIED"),
                capabilities, labels,
                environment.getProperty("cpf.runtime.managed-server-id"),
                environment.getProperty("cpf.runtime.management-identity"),
                runtime.hostName(),
                runtime.systemCode(),
                runtime.application(),
                environment.getProperty("cpf.runtime.role", "APPLICATION"),
                ProcessHandle.current().pid(),
                System.getProperty("java.version"),
                environment.getProperty("cpf.framework.version", environment.getProperty("cpf.runtime.artifact-version", "unknown")),
                environment.getProperty("cpf.runtime.application-version", environment.getProperty("cpf.runtime.artifact-version", "unknown")),
                Instant.now(), Instant.now(),
                environment.getProperty("cpf.runtime.control.agent.lease-seconds", Integer.class, 60));
    }

    @Bean
    @ConditionalOnBean(CpfRuntimeAgentPort.class)
    @ConditionalOnMissingBean
    CpfRuntimeInstanceInboxStore cpfRuntimeInstanceInboxStore(CpfRuntimeMetadata runtime, Environment environment) {
        String configured = environment.getProperty("cpf.runtime.control.agent.inbox-directory");
        if ((configured == null || configured.isBlank()) && environment.acceptsProfiles(Profiles.of("prod"))) {
            throw new IllegalStateException("prod profile은 cpf.runtime.control.agent.inbox-directory 설정이 필요합니다.");
        }
        // 미설정 로컬 기본값은 현재 작업 디렉터리 상대경로("runtime/cpf-inbox")를 쓰지 않는다.
        // 리포지토리 루트 등 임의 CWD에서 기동하면 그 위치를 오염시키므로, 다른 CPF Starter 로컬 기본값과
        // 동일하게 java.io.tmpdir 기반 경로를 사용한다.
        String defaultDirectory = Path.of(System.getProperty("java.io.tmpdir"), "cpf-runtime-inbox", runtime.instanceId()).toString();
        Path path = configured == null || configured.isBlank() ? Path.of(defaultDirectory) : Path.of(configured);
        return new CpfRuntimeInstanceInboxStore(path);
    }

    @Bean
    @ConditionalOnBean({CpfRuntimeAgentPort.class, CpfRuntimeInstanceRegistration.class, CpfRuntimeInstanceInboxStore.class})
    @ConditionalOnMissingBean
    CpfRuntimeControlAgent cpfRuntimeControlAgent(
            CpfRuntimeAgentPort controlPlane, CpfRuntimeInstanceRegistration registration,
            ObjectProvider<CpfRuntimeChangeApplier> appliers, CpfRuntimeInstanceInboxStore inbox,
            Environment environment) {
        List<CpfRuntimeChangeApplier> installed = appliers.orderedStream().toList();
        int attempts = bounded(environment.getProperty(
                "cpf.runtime.control.agent.registration-max-attempts", Integer.class, 5), 1, 20,
                "cpf.runtime.control.agent.registration-max-attempts");
        long backoffMillis = bounded(environment.getProperty(
                "cpf.runtime.control.agent.registration-backoff-millis", Long.class, 100L), 10L, 5_000L,
                "cpf.runtime.control.agent.registration-backoff-millis");
        return new CpfRuntimeControlAgent(controlPlane, registration, installed, inbox, attempts, backoffMillis);
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
    private static int bounded(int value, int minimum, int maximum, String name) {
        if (value < minimum || value > maximum) throw new IllegalStateException(name + " must be between " + minimum + " and " + maximum);
        return value;
    }
    private static long bounded(long value, long minimum, long maximum, String name) {
        if (value < minimum || value > maximum) throw new IllegalStateException(name + " must be between " + minimum + " and " + maximum);
        return value;
    }
    private static String first(String first, String second) {
        return first != null && !first.isBlank() ? first.trim() : (second == null || second.isBlank() ? null : second.trim());
    }

    static String resolveRuntimeBaseUrl(CpfRuntimeMetadata runtime, Environment environment) {
        String explicit = environment.getProperty("cpf.runtime.control.agent.runtime-base-url");
        if (explicit != null && !explicit.isBlank()) return validateHttpUrl(explicit.trim());

        String configuredAddress = environment.getProperty("server.address");
        String host = isWildcard(configuredAddress) ? runtime.hostName() : configuredAddress.trim();
        host = first(host, runtime.hostIp());
        if (host == null) host = "127.0.0.1";
        if (host.indexOf(':') >= 0 && !host.startsWith("[")) host = "[" + host + "]";

        int port = environment.getProperty("server.port", Integer.class, 8080);
        if (port < 1 || port > 65_535) {
            throw new IllegalStateException("Runtime Agent server.port must be between 1 and 65535");
        }
        boolean ssl = environment.getProperty("server.ssl.enabled", Boolean.class, false);
        return validateHttpUrl((ssl ? "https" : "http") + "://" + host + ":" + port);
    }

    private static boolean isWildcard(String value) {
        return value == null || value.isBlank() || "0.0.0.0".equals(value.trim())
                || "::".equals(value.trim()) || "[::]".equals(value.trim());
    }

    private static String validateHttpUrl(String value) {
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    || uri.getHost() == null || uri.getUserInfo() != null || uri.getFragment() != null) {
                throw new IllegalArgumentException("invalid");
            }
            return value;
        } catch (RuntimeException failure) {
            throw new IllegalStateException("Runtime Agent base URL must be an http(s) URI without credentials or fragment");
        }
    }
}
