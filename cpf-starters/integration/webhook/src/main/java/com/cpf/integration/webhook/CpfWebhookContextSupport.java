package com.cpf.integration.webhook;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Webhook enqueue 시 bounded lineage를 저장하고 retry/reconcile 시 동일 transactionId로 복원합니다. */
public final class CpfWebhookContextSupport {
    private final CpfContextExecutionFactory factory;

    public CpfWebhookContextSupport(CpfContextExecutionFactory factory) {
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    public Map<String, String> capture() {
        CpfContext context = CpfContexts.requireCurrent();
        Map<String, String> values = new LinkedHashMap<>();
        put(values, "tx", context.transaction().transactionId());
        put(values, "rootTx", context.transaction().rootTransactionId());
        put(values, "correlation", context.transaction().correlationId());
        put(values, "rootExec", context.execution().rootExecutionId());
        put(values, "parentExec", context.execution().executionId());
        put(values, "parentSeg", context.execution().segmentId());
        put(values, "businessDate", context.transaction().businessDate().toString());
        put(values, "startedAt", context.transaction().startedAt().toString());
        if (context.execution().deadline() != null) {
            put(values, "deadline", context.execution().deadline().toString());
        }
        return Map.copyOf(values);
    }

    public CpfContextSnapshot restore(
            Map<String, String> captured,
            String endpointId,
            String eventId,
            int attempt,
            boolean reconcile) {
        Objects.requireNonNull(captured, "captured");
        LocalDate businessDate = LocalDate.parse(required(captured, "businessDate"));
        Instant startedAt = Instant.parse(required(captured, "startedAt"));
        Instant deadline = text(captured.get("deadline")) == null
                ? null : Instant.parse(captured.get("deadline"));
        CpfContext context = factory.fromTrustedPropagation(
                required(captured, "tx"),
                captured.get("rootTx"),
                captured.get("correlation"),
                businessDate,
                startedAt,
                reconcile ? CpfContext.CpfTransactionOriginKind.RECOVERY : CpfContext.CpfTransactionOriginKind.INTEGRATION,
                "webhook",
                eventId,
                "webhook." + endpointId,
                captured.get("parentExec"),
                captured.get("rootExec"),
                captured.get("parentSeg"),
                reconcile ? CpfContext.CpfExecutionType.INTERNAL : CpfContext.CpfExecutionType.INTEGRATION,
                Math.max(1, attempt),
                1,
                null,
                null,
                null,
                deadline);
        return CpfContextSnapshot.capture(context);
    }

    public AutoCloseable bind(
            Map<String, String> captured,
            String endpointId,
            String eventId,
            int attempt,
            boolean reconcile) {
        return CpfContexts.bind(restore(captured, endpointId, eventId, attempt, reconcile));
    }

    private static void put(Map<String, String> map, String key, String value) {
        if (text(value) != null) map.put(key, value.trim());
    }

    private static String required(Map<String, String> map, String key) {
        String value = text(map.get(key));
        if (value == null) throw new IllegalStateException("missing webhook context " + key);
        return value;
    }

    private static String text(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
