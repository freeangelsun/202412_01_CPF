package com.cpf.platform.operations.observability.internal.logging;

import com.cpf.foundation.observability.CpfBoundaryFailureEvidence;
import com.cpf.platform.operations.observability.spi.logging.TransactionLogRecord;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Bridges pre-handler boundary failures into the canonical durable transaction-log pipeline.
 *
 * <p>The web boundary never writes a DB directly. This listener creates the minimum masked transaction summary
 * and hands it to {@link TransactionLogListener}, which owns bounded async persistence and the durable fallback
 * journal when the DB is unavailable.</p>
 */
@Component
public final class CpfBoundaryFailureEvidenceListener {
    private final TransactionLogListener transactionLogs;

    public CpfBoundaryFailureEvidenceListener(TransactionLogListener transactionLogs) {
        this.transactionLogs = Objects.requireNonNull(transactionLogs, "transactionLogs");
    }

    @EventListener
    public void onBoundaryFailure(CpfBoundaryFailureEvidence failure) {
        if (failure == null || failure.transactionId() == null || failure.transactionId().isBlank()) return;
        LocalDateTime occurredAt = LocalDateTime.ofInstant(
                failure.occurredAt() == null ? java.time.Instant.now() : failure.occurredAt(), ZoneOffset.UTC);
        TransactionLogRecord record = TransactionLogRecord.builder()
                .transactionId(failure.transactionId())
                .moduleId(text(failure.application(), failure.systemCode(), "CPF"))
                .logType("BOUNDARY_FAILURE")
                .instanceId(failure.instanceId())
                .hostName(failure.hostName())
                .hostIp(failure.hostIp())
                .processId(failure.processId())
                .httpMethod(failure.method())
                .uri(failure.uri())
                .httpStatus(failure.httpStatus())
                .responseCode(failure.errorCode())
                .errorCode(failure.errorCode())
                .errorMessage(text(failure.category(), "BOUNDARY_REJECTED"))
                .clientIp(failure.clientIp())
                .startTime(occurredAt)
                .endTime(occurredAt)
                .durationMs(0L)
                .build();
        LinkedHashMap<String, String> details = new LinkedHashMap<>();
        put(details, "boundary.type", failure.boundaryType());
        put(details, "runtime.systemCode", failure.systemCode());
        put(details, "failure.field", failure.fieldName());
        put(details, "failure.category", failure.category());
        put(details, "trace.reference", failure.traceReference());
        transactionLogs.handleTransactionLogEvent(new TransactionLogEvent(this, record, Map.copyOf(details)));
    }

    private static void put(Map<String, String> target, String key, String value) {
        if (value != null && !value.isBlank()) target.put(key, sanitize(value));
    }

    private static String sanitize(String value) {
        String normalized = value.replace('\r', '_').replace('\n', '_').replace('\t', '_');
        return normalized.length() > 256 ? normalized.substring(0, 256) : normalized;
    }

    private static String text(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return sanitize(value);
        return "";
    }
}
