package com.cpf.batch.execution;

import com.cpf.batch.api.BatchControlState;
import com.cpf.batch.spi.BatchExecutionLedgerPort;
import java.util.Objects;
import java.util.Set;

/**
 * CPF 원장 상태와 외부 Spring Batch abandon 부작용을 순서화하는 상태기계입니다.
 * 원장 선점 없이 외부 부작용을 먼저 수행하지 않으며 불명확한 결과는 대사 대상으로 보존합니다.
 */
final class CpfBatchAbandonCoordinator {
    @FunctionalInterface
    interface AbandonAction { void execute() throws Exception; }

    private final BatchExecutionLedgerPort ledger;

    CpfBatchAbandonCoordinator(BatchExecutionLedgerPort ledger) {
        this.ledger = Objects.requireNonNull(ledger, "ledger");
    }

    void abandon(String cpfExecutionId, String reason, AbandonAction action) {
        Objects.requireNonNull(action, "action");
        ledger.transition(cpfExecutionId,
                Set.of(BatchControlState.STOPPED, BatchControlState.FAILED,
                        BatchControlState.UNKNOWN_RESULT),
                BatchControlState.ABANDONING,
                "OPERATOR_ABANDON_REQUESTED", reason, null);
        try {
            action.execute();
        } catch (Exception failure) {
            recordUnknownSafely(cpfExecutionId, "BATCH_ABANDON_RESPONSE_UNKNOWN", failure);
            throw new CpfBatchUnknownResultException(
                    "BATCH_ABANDON_RESPONSE_UNKNOWN",
                    "Abandon outcome is unknown for " + cpfExecutionId
                            + ". Reconcile before retrying.");
        }
        try {
            ledger.transition(cpfExecutionId,
                    Set.of(BatchControlState.ABANDONING),
                    BatchControlState.ABANDONED,
                    "OPERATOR_ABANDON", reason, null);
        } catch (RuntimeException ledgerFailure) {
            recordUnknownSafely(cpfExecutionId,
                    "BATCH_ABANDON_LEDGER_CONFIRM_UNKNOWN", ledgerFailure);
            throw new CpfBatchUnknownResultException(
                    "BATCH_ABANDON_LEDGER_CONFIRM_UNKNOWN",
                    "External abandon succeeded but CPF ledger confirmation is unknown for "
                            + cpfExecutionId + ". Reconcile before retrying.");
        }
    }

    private void recordUnknownSafely(String id, String reasonCode, Throwable failure) {
        try {
            ledger.recordUnknown(id, reasonCode, safe(failure));
        } catch (RuntimeException evidenceFailure) {
            failure.addSuppressed(evidenceFailure);
        }
    }

    private static String safe(Throwable failure) {
        String text = failure == null ? ""
                : Objects.toString(failure.getMessage(), failure.getClass().getSimpleName());
        text = text
                .replaceAll("(?i)(password|token|secret|authorization|cookie|session(?:id)?)\\s*[=:]\\s*[^,;\\s]+", "$1=<masked>")
                .replaceAll("[\\r\\n\\t]+", " ").trim();
        return text.length() <= 2_000 ? text : text.substring(0, 2_000);
    }
}
