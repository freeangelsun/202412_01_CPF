package com.cpf.core.common.runtimecontrol.applier;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.common.runtimecontrol.CpfRuntimePayloadJson;
import com.cpf.core.common.servicecall.CpfServiceRegistryRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Service Call Engine이 실제 사용하는 Registry 정본을 Runtime에서 검증합니다. */
public final class CpfServiceRegistryRuntimeVerifierApplier implements CpfRuntimeChangeApplier {
    private final String changeType;
    private final CpfServiceRegistryRepository repository;

    public CpfServiceRegistryRuntimeVerifierApplier(String changeType, CpfServiceRegistryRepository repository) {
        this.changeType = Objects.requireNonNull(changeType).trim().toUpperCase();
        this.repository = Objects.requireNonNull(repository);
        if (!List.of("SERVICE_ROUTE", "CIRCUIT", "MAINTENANCE").contains(this.changeType)) {
            throw new IllegalArgumentException("지원하지 않는 Service Registry Runtime 기능입니다.");
        }
    }

    @Override public String changeType() { return changeType; }
    @Override public boolean supportsIdempotentReplay() { return true; }
    @Override public boolean snapshotCapable() { return true; }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        try {
            boolean matched = switch (changeType) {
                case "SERVICE_ROUTE" -> verifyRoute(CpfRuntimePayloadJson.asMap(delivery.payload()));
                case "CIRCUIT" -> verifyCircuit(CpfRuntimePayloadJson.asMap(delivery.payload()));
                case "MAINTENANCE" -> verifyMaintenance(CpfRuntimePayloadJson.asMap(delivery.payload()));
                default -> false;
            };
            return matched
                    ? CpfRuntimeApplyResult.success(delivery.payloadHash())
                    : CpfRuntimeApplyResult.failure(changeType + "_NOT_APPLIED", "Service Registry 정본이 기대 상태와 일치하지 않습니다.");
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure(changeType + "_VERIFY_FAILED", "Service Registry 정본 검증에 실패했습니다.");
        }
    }

    private boolean verifyRoute(Map<String,Object> payload) {
        String serviceId = required(payload, "serviceId");
        String endpointCode = required(payload, "endpointCode");
        return repository.findEndpoints(serviceId, endpointCode, null, 2).stream()
                .filter(row -> endpointCode.equals(text(row.get("endpointCode"))))
                .anyMatch(row -> equalsIfPresent(payload, "expectedBaseUrl", row.get("baseUrl"))
                        && equalsIfPresent(payload, "expectedUseYn", row.get("useYn"))
                        && numberIfPresent(payload, "expectedRowVersion", row.get("rowVersion")));
    }

    private boolean verifyCircuit(Map<String,Object> payload) {
        String serviceId = required(payload, "serviceId");
        String endpointCode = required(payload, "endpointCode");
        String instanceId = optional(payload, "instanceId");
        return repository.findCircuitStates(serviceId, endpointCode, 100).stream()
                .filter(row -> instanceId.isBlank() || instanceId.equals(text(row.get("instanceId"))))
                .anyMatch(row -> equalsIfPresent(payload, "expectedState", row.get("circuitState")));
    }

    private boolean verifyMaintenance(Map<String,Object> payload) {
        String serviceId = required(payload, "serviceId");
        String endpointCode = required(payload, "endpointCode");
        String instanceId = required(payload, "instanceId");
        return repository.findInstances(serviceId, endpointCode, null, 500).stream()
                .filter(row -> instanceId.equals(text(row.get("instanceId"))))
                .anyMatch(row -> equalsIfPresent(payload, "expectedMaintenanceYn", row.get("maintenanceYn"))
                        && equalsIfPresent(payload, "expectedDrainYn", row.get("drainYn"))
                        && equalsIfPresent(payload, "expectedActiveYn", row.get("activeYn"))
                        && numberIfPresent(payload, "expectedRowVersion", row.get("rowVersion")));
    }

    private boolean equalsIfPresent(Map<String,Object> payload, String key, Object actual) {
        Object expected = payload.get(key);
        return expected == null || text(expected).equalsIgnoreCase(text(actual));
    }
    private boolean numberIfPresent(Map<String,Object> payload, String key, Object actual) {
        Object expected = payload.get(key);
        if (expected == null) return true;
        return Long.parseLong(text(expected)) == Long.parseLong(text(actual));
    }
    private String required(Map<String,Object> payload, String key) {
        String value = optional(payload, key);
        if (value.isBlank()) throw new IllegalArgumentException(key + " 필수");
        return value;
    }
    private String optional(Map<String,Object> payload, String key) { return text(payload.get(key)); }
    private String text(Object value) { return value == null ? "" : String.valueOf(value).trim(); }
}
