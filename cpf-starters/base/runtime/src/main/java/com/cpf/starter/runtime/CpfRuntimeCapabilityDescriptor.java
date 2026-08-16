package com.cpf.starter.runtime;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Runtime에서 자동 발견되는 Public Starter/Capability 설명입니다. */
public record CpfRuntimeCapabilityDescriptor(
        String id,
        String starterArtifactId,
        String capability,
        String provider,
        String configPrefix,
        String ownerGroup,
        boolean runtimeRequired,
        String usageLevel,
        String managementCategory,
        boolean dedicatedWorkflow,
        boolean operatorVisible,
        boolean automaticRegistration,
        String managementScope,
        List<String> commonAreas,
        Support support,
        Map<String, String> metadata) {
    public CpfRuntimeCapabilityDescriptor {
        id = required(id, "id");
        starterArtifactId = required(starterArtifactId, "starterArtifactId");
        capability = required(capability, "capability");
        provider = required(provider, "provider");
        configPrefix = configPrefix == null ? "" : configPrefix.trim();
        ownerGroup = required(ownerGroup, "ownerGroup");
        usageLevel = required(usageLevel, "usageLevel");
        managementCategory = required(managementCategory, "managementCategory");
        managementScope = required(managementScope, "managementScope");
        commonAreas = List.copyOf(Objects.requireNonNullElse(commonAreas, List.of()));
        if (commonAreas.isEmpty()) throw new IllegalArgumentException("commonAreas must not be empty");
        support = Objects.requireNonNull(support, "support");
        metadata = Map.copyOf(Objects.requireNonNullElse(metadata, Map.of()));
    }

    public record Support(boolean health, boolean metrics, boolean logs, boolean trace, boolean effectiveConfig,
            boolean failure, boolean audit, boolean dynamicConfig, boolean runtimeControl, boolean recovery) {}

    private static String required(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " must not be blank");
        return value.trim();
    }
}
