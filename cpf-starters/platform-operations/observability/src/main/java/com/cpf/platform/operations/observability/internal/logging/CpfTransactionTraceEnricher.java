package com.cpf.platform.operations.observability.internal.logging;

import com.cpf.platform.operations.observability.api.CpfTraceContext;
import com.cpf.platform.operations.observability.spi.logging.TransactionLogRecord;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

/** Canonical transaction/trace correlation used by DB and file-log consumers. */
public final class CpfTransactionTraceEnricher {
    private CpfTransactionTraceEnricher() { }

    public static CpfTraceContext enrich(TransactionLogRecord record) {
        if (record == null) throw new IllegalArgumentException("transaction log record is required");
        String transactionId = required(record.getTransactionId(), "transactionId");
        String traceId = validHex(record.getTraceId(), 32)
                ? record.getTraceId().trim().toLowerCase()
                : digest(transactionId).substring(0, 32);
        String operation = first(record.getExecutionSignature(), record.getUri(), record.getExecutionMethod(), "transaction");
        String spanId = validHex(record.getSpanId(), 16)
                ? record.getSpanId().trim().toLowerCase()
                : digest(traceId + '|' + operation + '|' + number(record.getSequenceNo())).substring(0, 16);
        String parent = validHex(record.getParentSpanId(), 16)
                ? record.getParentSpanId().trim().toLowerCase() : null;
        CpfTraceContext context = new CpfTraceContext(
                traceId, spanId, parent, transactionId,
                first(record.getWorkflowStepId(), record.getWorkflowInstanceId(), null),
                Math.max(0, number(record.getSequenceNo()) - 1),
                kind(record), operation,
                Map.of("cpf.module", first(record.getModuleId(), "UNKNOWN"),
                        "cpf.execution", first(record.getStandardExecutionId(), "UNKNOWN")),
                CpfTraceContext.CURRENT_POLICY_VERSION);
        record.setTraceId(context.traceId());
        record.setSpanId(context.spanId());
        record.setParentSpanId(context.parentSpanId());
        return context;
    }

    private static CpfTraceContext.SpanKind kind(TransactionLogRecord record) {
        String type = first(record.getRequestType(), record.getLogType(), "LOCAL").toUpperCase();
        if (type.contains("MESSAGE") || type.contains("BROKER") || type.contains("EVENT")) return CpfTraceContext.SpanKind.MESSAGE;
        if (type.contains("BATCH") || type.contains("JOB")) return CpfTraceContext.SpanKind.BATCH;
        if (type.contains("FILE")) return CpfTraceContext.SpanKind.FILE;
        if (record.getCallerService() != null || type.contains("REMOTE") || type.contains("HTTP")) return CpfTraceContext.SpanKind.REMOTE;
        return CpfTraceContext.SpanKind.LOCAL;
    }

    private static int number(Integer value) { return value == null || value < 1 ? 1 : value; }

    private static String first(String... values) {
        if (values == null) return null;
        for (String value : values) if (value != null && !value.isBlank()) return value.trim();
        return null;
    }

    private static String required(String value, String name) {
        String normalized = first(value);
        if (normalized == null) throw new IllegalArgumentException(name + " is required");
        return normalized;
    }

    private static boolean validHex(String value, int length) {
        return value != null && value.trim().matches("(?i)[0-9a-f]{" + length + "}")
                && !value.trim().matches("0{" + length + "}");
    }

    private static String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }
}
