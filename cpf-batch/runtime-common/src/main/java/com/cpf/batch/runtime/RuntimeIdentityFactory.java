package com.cpf.batch.runtime;

import com.cpf.batch.api.RuntimeRegistration;
import com.cpf.batch.api.RuntimeRole;
import org.springframework.core.env.Environment;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class RuntimeIdentityFactory {
    private RuntimeIdentityFactory() {}

    public static RuntimeRegistration fromEnvironment(
            Environment environment,
            RuntimeRole role,
            String serviceId,
            int defaultPort) {
        String instanceId = property(
                environment, "cpf.batch.runtime.instance-id", "CPF_INSTANCE_ID", serviceId + "-local-01");
        int port = Integer.parseInt(property(
                environment, "server.port", "CPF_PORT", Integer.toString(defaultPort)));
        List<String> capabilities = Arrays.stream(property(
                        environment, "cpf.batch.runtime.capabilities", "CPF_CAPABILITIES", role.name()).split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();

        return new RuntimeRegistration(
                role,
                serviceId,
                instanceId,
                property(environment, "cpf.framework.module-id", null, "BAT"),
                property(environment, "cpf.framework.was-id", "CPF_WAS_ID", instanceId),
                property(environment, "cpf.batch.runtime.host-alias", "CPF_HOST_ALIAS", "localhost"),
                property(environment, "cpf.batch.runtime.zone", "CPF_ZONE", "local"),
                property(environment, "cpf.batch.runtime.pool", "CPF_POOL", "default"),
                property(environment, "cpf.batch.runtime.artifact-version", "CPF_ARTIFACT_VERSION", "dev"),
                property(environment, "cpf.batch.runtime.git-sha", "CPF_GIT_SHA", "UNKNOWN"),
                property(environment, "cpf.batch.runtime.artifact-sha256", "CPF_ARTIFACT_SHA256", "UNKNOWN"),
                property(environment, "spring.profiles.active", "SPRING_PROFILES_ACTIVE", "local"),
                capabilities,
                Map.of(
                        "health", "http://127.0.0.1:" + port + "/actuator/health/readiness",
                        "base", "http://127.0.0.1:" + port),
                property(environment, "cpf.batch.runtime.config-version", "CPF_CONFIG_VERSION", "local"),
                property(environment, "cpf.batch.runtime.schema-compatibility", "CPF_SCHEMA_COMPATIBILITY", "UNKNOWN"),
                "v1",
                Instant.now());
    }

    private static String property(
            Environment environment,
            String propertyName,
            String environmentName,
            String defaultValue) {
        String value = environment.getProperty(propertyName);
        if (value == null || value.isBlank()) {
            value = environmentName == null ? null : environment.getProperty(environmentName);
        }
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
