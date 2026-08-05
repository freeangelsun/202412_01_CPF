package com.cpf.core.api.runtimecontrol;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/** Runtime Control Plane 변경의 대상 선택자입니다. */
public record CpfRuntimeTargetSelector(
        String environment,
        String serviceId,
        String groupId,
        List<String> instanceIds,
        List<String> excludeInstanceIds,
        Map<String, String> labels,
        String zone,
        String cell,
        boolean includeDraining,
        boolean includeMaintenance,
        boolean allowAll) {

    public CpfRuntimeTargetSelector {
        environment = trimToNull(environment);
        serviceId = trimToNull(serviceId);
        groupId = trimToNull(groupId);
        zone = trimToNull(zone);
        cell = trimToNull(cell);
        instanceIds = normalizeIds(instanceIds, "instanceIds");
        excludeInstanceIds = normalizeIds(excludeInstanceIds, "excludeInstanceIds");
        labels = normalizeLabels(labels);
    }

    /** 기존 9-인자 생성 코드 호환입니다. */
    public CpfRuntimeTargetSelector(String environment, String serviceId, String groupId,
                                    List<String> instanceIds, Map<String, String> labels,
                                    String zone, String cell, boolean includeDraining,
                                    boolean includeMaintenance) {
        this(environment, serviceId, groupId, instanceIds, List.of(), labels, zone, cell,
                includeDraining, includeMaintenance, false);
    }

    private static List<String> normalizeIds(List<String> values, String name) {
        if (values == null || values.isEmpty()) return List.of();
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            String id = trimToNull(value);
            if (id == null) throw new IllegalArgumentException(name + "에 빈 instanceId가 포함될 수 없습니다.");
            normalized.add(id);
        }
        return List.copyOf(normalized);
    }

    private static Map<String, String> normalizeLabels(Map<String, String> values) {
        if (values == null || values.isEmpty()) return Map.of();
        LinkedHashMap<String, String> normalized = new LinkedHashMap<>();
        values.forEach((key, value) -> {
            String normalizedKey = trimToNull(key);
            String normalizedValue = trimToNull(value);
            if (normalizedKey == null || normalizedValue == null) {
                throw new IllegalArgumentException("labels에는 빈 key/value가 포함될 수 없습니다.");
            }
            String previous = normalized.putIfAbsent(normalizedKey, normalizedValue);
            if (previous != null && !previous.equals(normalizedValue)) {
                throw new IllegalArgumentException("정규화 후 중복 Runtime target label key입니다: " + normalizedKey);
            }
        });
        return Map.copyOf(normalized);
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
