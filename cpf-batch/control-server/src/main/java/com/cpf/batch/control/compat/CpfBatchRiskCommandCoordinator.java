package com.cpf.batch.control.compat;

import com.cpf.core.api.batch.CpfBatchOwnerUnknownResultException;
import com.cpf.core.api.batch.CpfBatchRiskCommand;
import com.cpf.core.api.data.CpfDataRow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** 위험조치의 reserve -> execute -> finalize/replay/UNKNOWN 상태기계입니다. */
@Component
public final class CpfBatchRiskCommandCoordinator {
    private static final String RESULT_SERIALIZATION_FAILED = "RESULT_SERIALIZATION_FAILED";
    private static final String LEDGER_FINALIZATION_FAILED = "LEDGER_FINALIZATION_FAILED";
    private static final String LEDGER_FAILURE_CLASSIFICATION_FAILED = "LEDGER_FAILURE_CLASSIFICATION_FAILED";

    private final JdbcBatchRiskCommandLedger ledger;
    private final ObjectMapper objectMapper;

    public CpfBatchRiskCommandCoordinator(
            JdbcBatchRiskCommandLedger ledger, ObjectMapper objectMapper) {
        this.ledger = ledger;
        this.objectMapper = objectMapper;
    }

    public CpfDataRow executeRow(CpfBatchRiskCommand command, Supplier<CpfDataRow> action) {
        JdbcBatchRiskCommandLedger.Decision decision = ledger.reserve(command);
        if (decision.kind() == JdbcBatchRiskCommandLedger.Kind.REPLAY) {
            return readRow(decision.resultPayload());
        }
        assertExecutable(decision);
        CpfDataRow result = executeAction(command, action);
        finalizeAfterSideEffect(command, result);
        return result;
    }

    public List<CpfDataRow> executeRows(CpfBatchRiskCommand command, Supplier<List<CpfDataRow>> action) {
        JdbcBatchRiskCommandLedger.Decision decision = ledger.reserve(command);
        if (decision.kind() == JdbcBatchRiskCommandLedger.Kind.REPLAY) {
            return readRows(decision.resultPayload());
        }
        assertExecutable(decision);
        List<CpfDataRow> result = executeAction(command, () -> List.copyOf(action.get()));
        finalizeAfterSideEffect(command, result);
        return result;
    }

    private <T> T executeAction(CpfBatchRiskCommand command, Supplier<T> action) {
        try {
            return action.get();
        } catch (RuntimeException failure) {
            throw classifyActionFailure(command, failure);
        }
    }

    private RuntimeException classifyActionFailure(
            CpfBatchRiskCommand command, RuntimeException failure) {
        if (failure instanceof CpfBatchOwnerUnknownResultException
                || failure instanceof DataAccessResourceFailureException
                || failure instanceof QueryTimeoutException
                || failure instanceof RecoverableDataAccessException
                || failure instanceof TransientDataAccessException) {
            return actionUnknown(command, failure);
        }
        try {
            ledger.fail(command, failure.getClass().getSimpleName(), failure.getMessage());
            return failure;
        } catch (RuntimeException ledgerFailure) {
            CpfBatchOwnerUnknownResultException unknown = new CpfBatchOwnerUnknownResultException(
                    LEDGER_FAILURE_CLASSIFICATION_FAILED,
                    "BAT risk command failure could not be durably classified; reconciliation required: "
                            + failure.getClass().getSimpleName());
            unknown.initCause(failure);
            unknown.addSuppressed(ledgerFailure);
            return unknown;
        }
    }

    private CpfBatchOwnerUnknownResultException actionUnknown(
            CpfBatchRiskCommand command, RuntimeException failure) {
        final CpfBatchOwnerUnknownResultException unknown;
        if (failure instanceof CpfBatchOwnerUnknownResultException existing) {
            unknown = existing;
        } else {
            unknown = new CpfBatchOwnerUnknownResultException(
                    failure.getClass().getSimpleName(),
                    "BAT risk command owner action outcome is unknown; reconciliation required: "
                            + failure.getClass().getSimpleName());
            unknown.initCause(failure);
        }
        try {
            ledger.unknown(command, unknown.failureCode(), unknown.getMessage());
        } catch (RuntimeException ledgerFailure) {
            unknown.addSuppressed(ledgerFailure);
        }
        return unknown;
    }

    private void finalizeAfterSideEffect(CpfBatchRiskCommand command, Object result) {
        final String payload;
        try {
            payload = write(result);
        } catch (RuntimeException failure) {
            throw postActionUnknown(command, RESULT_SERIALIZATION_FAILED, failure);
        }
        try {
            ledger.complete(command, payload);
        } catch (RuntimeException failure) {
            throw postActionUnknown(command, LEDGER_FINALIZATION_FAILED, failure);
        }
    }

    private void assertExecutable(JdbcBatchRiskCommandLedger.Decision decision) {
        switch (decision.kind()) {
            case CREATED -> { return; }
            case CONFLICT -> throw new IllegalArgumentException(decision.message());
            case FAILED -> throw new IllegalStateException(
                    "previous BAT risk command failed: " + decision.code() + ": " + decision.message());
            case IN_PROGRESS, UNKNOWN -> throw new CpfBatchOwnerUnknownResultException(
                    decision.code(), decision.message());
            case REPLAY -> throw new IllegalStateException("replay decision must be handled before execution");
        }
    }

    private CpfBatchOwnerUnknownResultException postActionUnknown(
            CpfBatchRiskCommand command, String code, RuntimeException failure) {
        String message = "BAT risk command side effect may have completed; reconciliation required: "
                + failure.getClass().getSimpleName();
        CpfBatchOwnerUnknownResultException unknown =
                new CpfBatchOwnerUnknownResultException(code, message);
        unknown.initCause(failure);
        try {
            ledger.unknown(command, code, message);
        } catch (RuntimeException ledgerFailure) {
            unknown.addSuppressed(ledgerFailure);
        }
        return unknown;
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException failure) {
            throw new IllegalStateException("BAT risk command result serialization failed", failure);
        }
    }
    private CpfDataRow readRow(String value) {
        try {
            Map<String,Object> decoded = objectMapper.readValue(value, new TypeReference<>() {});
            CpfDataRow row = CpfDataRow.copyOf(decoded);
            row.put("idempotentReplay", true);
            return row;
        } catch (JsonProcessingException failure) {
            throw new IllegalStateException("BAT risk command replay deserialization failed", failure);
        }
    }
    private List<CpfDataRow> readRows(String value) {
        try {
            List<Map<String,Object>> decoded = objectMapper.readValue(value, new TypeReference<>() {});
            return decoded.stream().map(CpfDataRow::copyOf).toList();
        } catch (JsonProcessingException failure) {
            throw new IllegalStateException("BAT risk command replay deserialization failed", failure);
        }
    }
}
