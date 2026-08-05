package com.cpf.core.api.runtimecontrol;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Runtime Agent의 자기 등록/재등록 계약입니다. */
public record CpfRuntimeInstanceRegistration(
        String instanceId,
        String serviceId,
        String endpointCode,
        String environment,
        String zone,
        String cell,
        String baseUrl,
        String artifactVersion,
        String artifactCommit,
        String runtimeRole,
        String registrationSource,
        String schemaVersion,
        String configHash,
        Map<String, String> capabilities,
        Map<String, String> labels,
        Instant agentTime,
        int leaseSeconds) {

    public CpfRuntimeInstanceRegistration {
        instanceId = require(instanceId, "instanceId");
        serviceId = require(serviceId, "serviceId");
        endpointCode = require(endpointCode, "endpointCode");
        baseUrl = require(baseUrl, "baseUrl");
        registrationSource = require(registrationSource, "registrationSource");
        environment = trimToNull(environment);
        zone = trimToNull(zone);
        cell = trimToNull(cell);
        artifactVersion = trimToNull(artifactVersion);
        artifactCommit = trimToNull(artifactCommit);
        runtimeRole = trimToNull(runtimeRole);
        schemaVersion = trimToNull(schemaVersion);
        configHash = trimToNull(configHash);
        capabilities = normalizeMap(capabilities, true, "capabilities");
        labels = normalizeMap(labels, false, "labels");
        agentTime = agentTime == null ? Instant.now() : agentTime;
        leaseSeconds = leaseSeconds <= 0 ? 60 : leaseSeconds;
        if (leaseSeconds < 10 || leaseSeconds > 3600) {
            throw new IllegalArgumentException("leaseSeconds는 10..3600 범위여야 합니다.");
        }
    }

    /** 기존 생성 코드 호환입니다. */
    public CpfRuntimeInstanceRegistration(String instanceId, String serviceId, String endpointCode,
                                          String environment, String zone, String cell, String baseUrl,
                                          String artifactVersion, String schemaVersion, String configHash,
                                          Map<String, String> capabilities, Map<String, String> labels,
                                          int leaseSeconds) {
        this(instanceId, serviceId, endpointCode, environment, zone, cell, baseUrl,
                artifactVersion, "unknown", "APPLICATION", "SELF", schemaVersion, configHash,
                capabilities, labels, Instant.now(), leaseSeconds);
    }

    private static Map<String, String> normalizeMap(
            Map<String, String> values, boolean upperCaseKey, String name) {
        if (values == null || values.isEmpty()) return Map.of();
        LinkedHashMap<String, String> normalized = new LinkedHashMap<>();
        values.forEach((key, value) -> {
            String normalizedKey = require(key, name + ".key");
            if (upperCaseKey) normalizedKey = normalizedKey.toUpperCase(Locale.ROOT);
            String normalizedValue = require(value, name + ".value");
            String previous = normalized.putIfAbsent(normalizedKey, normalizedValue);
            if (previous != null && !previous.equals(normalizedValue)) {
                throw new IllegalArgumentException("정규화 후 중복 " + name + " key입니다: " + normalizedKey);
            }
        });
        return Map.copyOf(normalized);
    }

    private static String require(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + "가 필요합니다.");
        }
        return value.trim();
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
