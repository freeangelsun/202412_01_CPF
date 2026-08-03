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
        try {
            CpfDataRow result = action.get();
            ledger.complete(command, write(result));
            return result;
        } catch (RuntimeException failure) {
            classify(command, failure);
            throw failure;
        }
    }

    public List<CpfDataRow> executeRows(CpfBatchRiskCommand command, Supplier<List<CpfDataRow>> action) {
        JdbcBatchRiskCommandLedger.Decision decision = ledger.reserve(command);
        if (decision.kind() == JdbcBatchRiskCommandLedger.Kind.REPLAY) {
            return readRows(decision.resultPayload());
        }
        assertExecutable(decision);
        try {
            List<CpfDataRow> result = List.copyOf(action.get());
            ledger.complete(command, write(result));
            return result;
        } catch (RuntimeException failure) {
            classify(command, failure);
            throw failure;
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

    private void classify(CpfBatchRiskCommand command, RuntimeException failure) {
        if (failure instanceof CpfBatchOwnerUnknownResultException
                || failure instanceof DataAccessResourceFailureException
                || failure instanceof QueryTimeoutException
                || failure instanceof RecoverableDataAccessException
                || failure instanceof TransientDataAccessException) {
            ledger.unknown(command, failure.getClass().getSimpleName(), failure.getMessage());
        } else {
            ledger.fail(command, failure.getClass().getSimpleName(), failure.getMessage());
        }
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
