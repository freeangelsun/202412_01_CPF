package com.cpf.core.common.servicecall;

import java.util.Map;

/**
 * 서비스 호출 이력과 health/circuit 상태를 cpfDB에 기록합니다.
 */
public class CpfServiceCallLogWriter {
    private final CpfServiceRegistryRepository repository;

    public CpfServiceCallLogWriter(CpfServiceRegistryRepository repository) {
        this.repository = repository;
    }

    public void write(
            ServiceCallRequest request,
            ServiceCallResolvedTarget target,
            String callStatus,
            Integer httpStatus,
            long durationMillis,
            String failureCode,
            String failureMessage) {
        repository.insertCallHistory(request, target, callStatus, httpStatus, durationMillis, failureCode, failureMessage);
    }

    public boolean isCircuitOpen(ServiceCallResolvedTarget target, long retryAfterMillis) {
        return repository.findCircuitState(target).map(row -> {
            Object state = row.get("circuitState");
            if (!"OPEN".equalsIgnoreCase(state == null ? "" : String.valueOf(state))) {
                return false;
            }
            Object openedAt = row.get("openedAt");
            if (openedAt instanceof java.sql.Timestamp timestamp && retryAfterMillis > 0) {
                boolean stillOpen = System.currentTimeMillis() - timestamp.getTime() < retryAfterMillis;
                if (!stillOpen) {
                    repository.recordCircuitHalfOpen(target);
                }
                return stillOpen;
            }
            return true;
        }).orElse(false);
    }

    public void markSuccess(ServiceCallResolvedTarget target, Integer httpStatus, long durationMillis) {
        repository.recordHealthStatus(target, "UP", httpStatus, durationMillis, null);
        repository.recordCircuitSuccess(target);
    }

    public void markFailure(
            ServiceCallResolvedTarget target,
            Integer httpStatus,
            long durationMillis,
            String failureMessage,
            int threshold) {
        repository.recordHealthStatus(target, "DOWN", httpStatus, durationMillis, failureMessage);
        repository.recordCircuitFailure(target, failureMessage, threshold);
    }

    public Map<String, Object> summarize(ServiceCallResolvedTarget target) {
        return repository.findCircuitState(target).orElse(Map.of());
    }
}
