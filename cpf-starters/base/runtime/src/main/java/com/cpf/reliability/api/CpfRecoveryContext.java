package com.cpf.reliability.api;

/**
 * 복구·재조정 실행의 식별자와 lineage를 보존하는 불변 의미 값입니다.
 * Generic Context Component Registry에 등록하지 않고 실제 Recovery Runtime이 명시적으로 소유합니다.
 */
public record CpfRecoveryContext(
        String recoveryId,
        String originalExecutionId,
        String recoveryExecutionId,
        String unknownOutcomeId,
        String reconciliationId,
        String checkpointId,
        int attempt,
        String strategy) {
    public CpfRecoveryContext {
        recoveryId = required(recoveryId, "recoveryId", 180);
        originalExecutionId = required(originalExecutionId, "originalExecutionId", 180);
        recoveryExecutionId = required(recoveryExecutionId, "recoveryExecutionId", 180);
        unknownOutcomeId = optional(unknownOutcomeId, "unknownOutcomeId", 180);
        reconciliationId = optional(reconciliationId, "reconciliationId", 180);
        checkpointId = optional(checkpointId, "checkpointId", 180);
        if (attempt < 1) throw new IllegalArgumentException("attempt는 1 이상이어야 합니다.");
        strategy = required(strategy, "strategy", 80);
    }

    private static String required(String value, String name, int max) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty() || normalized.length() > max) {
            throw new IllegalArgumentException(name + "은(는) 1~" + max + "자여야 합니다.");
        }
        return normalized;
    }

    private static String optional(String value, String name, int max) {
        if (value == null) return null;
        String normalized = value.trim();
        if (normalized.isEmpty()) return null;
        if (normalized.length() > max) throw new IllegalArgumentException(name + "은(는) " + max + "자 이하여야 합니다.");
        return normalized;
    }
}
