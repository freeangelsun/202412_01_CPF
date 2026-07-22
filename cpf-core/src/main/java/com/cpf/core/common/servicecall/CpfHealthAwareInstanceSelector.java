package com.cpf.core.common.servicecall;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 서비스 instance 목록에서 호출 가능한 대상을 선택합니다.
 *
 * <p>요청 instance가 있으면 우선 사용하고, 장애로 제외된 instance는 failover 후보에서 제외합니다.
 * 기본 선택 순서는 active + UP, active, 전체 목록 순입니다.</p>
 */
public class CpfHealthAwareInstanceSelector {

    public Optional<Map<String, Object>> select(List<Map<String, Object>> instances, String requestedInstanceId) {
        return select(instances, requestedInstanceId, Set.of());
    }

    public Optional<Map<String, Object>> select(
            List<Map<String, Object>> instances,
            String requestedInstanceId,
            Set<String> excludedInstanceIds) {
        if (instances == null || instances.isEmpty()) {
            return Optional.empty();
        }
        if (requestedInstanceId != null && !requestedInstanceId.isBlank()) {
            return instances.stream()
                    .filter(row -> requestedInstanceId.equals(String.valueOf(row.get("instanceId"))))
                    .filter(row -> !excluded(excludedInstanceIds, row))
                    .findFirst();
        }
        return instances.stream()
                .filter(row -> !excluded(excludedInstanceIds, row))
                .filter(this::activeAndUp)
                .findFirst()
                .or(() -> instances.stream()
                        .filter(row -> !excluded(excludedInstanceIds, row))
                        .filter(this::active)
                        .findFirst())
                .or(() -> instances.stream()
                        .filter(row -> !excluded(excludedInstanceIds, row))
                        .findFirst());
    }

    private boolean activeAndUp(Map<String, Object> row) {
        return active(row) && "UP".equalsIgnoreCase(String.valueOf(row.get("instanceStatus")));
    }

    private boolean active(Map<String, Object> row) {
        return "Y".equalsIgnoreCase(String.valueOf(row.get("activeYn")));
    }

    private boolean excluded(Set<String> excludedInstanceIds, Map<String, Object> row) {
        if (excludedInstanceIds == null || excludedInstanceIds.isEmpty()) {
            return false;
        }
        Object instanceId = row.get("instanceId");
        return instanceId != null && excludedInstanceIds.contains(String.valueOf(instanceId));
    }
}
