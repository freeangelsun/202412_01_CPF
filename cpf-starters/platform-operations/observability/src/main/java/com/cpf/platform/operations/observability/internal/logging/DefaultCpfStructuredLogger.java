package com.cpf.platform.operations.observability.internal.logging;

import com.cpf.security.api.CpfMaskingRuntime;

import com.cpf.platform.operations.observability.api.logging.CpfStructuredLogger;
import com.cpf.platform.operations.observability.api.logging.CpfTransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/** 구조화 Application Log의 기본 구현입니다. 저장 Provider 세부사항은 Public API에 노출하지 않습니다. */
public final class DefaultCpfStructuredLogger implements CpfStructuredLogger {
    private static final Logger LOG = LoggerFactory.getLogger("cpf.structured");
    private static final int MAX_FIELDS = 64;

    @Override public void business(String event, Map<String, ?> fields) { write("BUSINESS", event, null, fields); }
    @Override public void operation(String event, Map<String, ?> fields) { write("OPERATION", event, null, fields); }
    @Override public void security(String event, Map<String, ?> fields) { write("SECURITY", event, null, fields); }
    @Override public void error(String event, Throwable error, Map<String, ?> fields) { write("ERROR", event, error, fields); }

    private void write(String category, String event, Throwable error, Map<String, ?> fields) {
        String normalizedEvent = requiredEvent(event);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("category", category);
        payload.put("event", normalizedEvent);
        payload.put("transactionId", safe(CpfTransactionContext.currentTransactionId()));
        payload.put("executionId", safe(CpfTransactionContext.executionId()));
        payload.put("segmentId", safe(CpfTransactionContext.currentSegmentId()));
        payload.put("traceId", safe(CpfTransactionContext.currentTraceId()));
        payload.put("attempt", CpfTransactionContext.attempt());
        if (error != null) {
            payload.put("errorType", error.getClass().getName());
            payload.put("errorMessage", safe(error.getMessage()));
        }
        if (fields != null) {
            int count = 0;
            for (Map.Entry<String, ?> entry : fields.entrySet()) {
                if (count++ >= MAX_FIELDS) break;
                String key = safeKey(entry.getKey());
                if (key != null && !payload.containsKey(key)) payload.put(key, safe(entry.getValue()));
            }
        }
        String line = payload.entrySet().stream()
                .map(entry -> entry.getKey() + '=' + String.valueOf(entry.getValue()))
                .collect(java.util.stream.Collectors.joining(" "));
        if ("ERROR".equals(category)) LOG.error(line); else if ("SECURITY".equals(category)) LOG.warn(line); else LOG.info(line);
    }

    private String requiredEvent(String event) {
        if (event == null || event.isBlank()) throw new IllegalArgumentException("structured log event is required");
        return CpfMaskingRuntime.truncate(event.trim(), 120);
    }

    private String safeKey(String key) {
        if (key == null || key.isBlank()) return null;
        return CpfMaskingRuntime.truncate(key.trim().replaceAll("[^A-Za-z0-9_.-]", "_"), 80);
    }

    private Object safe(Object value) {
        if (value == null) return null;
        if (value instanceof Number || value instanceof Boolean || value instanceof Enum<?>) return value;
        return CpfMaskingRuntime.truncate(CpfMaskingRuntime.mask(String.valueOf(value)), 1000);
    }
}
