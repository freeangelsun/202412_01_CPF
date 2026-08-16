package com.cpf.messaging.context;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Provider 중립 메시지 경계에서 Core Context와 Message carrier를 변환합니다.
 * Registry/Descriptor/Transport SPI 없이 Messaging Owner가 carrier 정책을 직접 소유합니다.
 */
public final class CpfMessageContextAdapter {
    private final CpfExecutionIdGenerator executionIds;
    private final Clock clock;

    public CpfMessageContextAdapter(CpfExecutionIdGenerator executionIds) {
        this(executionIds, Clock.systemUTC());
    }

    public CpfMessageContextAdapter(CpfExecutionIdGenerator executionIds, Clock clock) {
        this.executionIds = Objects.requireNonNull(executionIds, "executionIds");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public Map<String, String> inject(CpfContext context, CpfMessageContext message, String idempotencyKey) {
        Objects.requireNonNull(context, "context");
        if (idempotencyKey != null && (context.operation() == null || context.operation().idempotencyKey() == null)) {
            throw new IllegalStateException("idempotency header without operation semantics");
        }
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(CpfMessageHeaderNames.TRANSACTION_ID, context.transaction().transactionId());
        headers.put(CpfMessageHeaderNames.ROOT_TRANSACTION_ID, context.transaction().rootTransactionId());
        headers.put(CpfMessageHeaderNames.BUSINESS_DATE, context.transaction().businessDate().toString());
        headers.put(CpfMessageHeaderNames.PARENT_EXECUTION_ID, context.execution().executionId());
        headers.put(CpfMessageHeaderNames.ROOT_EXECUTION_ID, context.execution().rootExecutionId());
        headers.put(CpfMessageHeaderNames.PARENT_SEGMENT_ID, context.execution().segmentId());
        if (context.transaction().correlationId() != null) {
            headers.put(CpfMessageHeaderNames.CORRELATION_ID, context.transaction().correlationId());
        }
        if (idempotencyKey != null) headers.put(CpfMessageHeaderNames.IDEMPOTENCY_KEY, idempotencyKey);
        if (context.execution().deadline() != null) headers.put(CpfMessageHeaderNames.DEADLINE, context.execution().deadline().toString());
        return Map.copyOf(headers);
    }

    public CpfMessageContextBundle extract(
            Map<String, String> headers,
            CpfMessageContext message,
            String standardExecutionId,
            Instant explicitDeadline) {
        Map<String, String> h = headers == null ? Map.of() : headers;
        String tx = required(h, CpfMessageHeaderNames.TRANSACTION_ID);
        String rootTx = text(h.get(CpfMessageHeaderNames.ROOT_TRANSACTION_ID));
        if (rootTx == null) rootTx = tx;
        LocalDate businessDate = LocalDate.parse(required(h, CpfMessageHeaderNames.BUSINESS_DATE));
        String executionId = executionIds.newExecutionId();
        String segmentId = executionIds.newSegmentId();
        String rootExecutionId = text(h.get(CpfMessageHeaderNames.ROOT_EXECUTION_ID));
        if (rootExecutionId == null) rootExecutionId = executionId;
        String parentExecutionId = text(h.get(CpfMessageHeaderNames.PARENT_EXECUTION_ID));
        String parentSegmentId = text(h.get(CpfMessageHeaderNames.PARENT_SEGMENT_ID));
        Instant deadline = explicitDeadline;
        if (deadline == null && text(h.get(CpfMessageHeaderNames.DEADLINE)) != null) {
            deadline = Instant.parse(h.get(CpfMessageHeaderNames.DEADLINE));
        }
        int attempt = Math.max(1, message == null ? 1 : message.deliveryAttempt());
        String idem = text(h.get(CpfMessageHeaderNames.IDEMPOTENCY_KEY));
        CpfContext.CpfOperationContext operation = idem == null ? null : new CpfContext.CpfOperationContext(
                message == null ? null : message.messageId(), standardExecutionId,
                message == null ? null : message.messageId(), idem,
                CpfContext.CpfIdempotencyScope.CURRENT_OPERATION,
                CpfContext.CpfIdempotencyMode.REQUIRED, null, null);
        Instant now = clock.instant();
        CpfContext context = new CpfContext(
                new CpfContext.CpfTransactionContext(
                        tx, rootTx, null, text(h.get(CpfMessageHeaderNames.CORRELATION_ID)), businessDate, now,
                        CpfContext.CpfTransactionOriginKind.MESSAGE, null, null),
                new CpfContext.CpfExecutionContext(
                        standardExecutionId, executionId, rootExecutionId, parentExecutionId,
                        segmentId, parentSegmentId, CpfContext.CpfExecutionType.MESSAGE,
                        attempt, parentExecutionId == null ? 0 : 1, now, deadline,
                        CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),
                operation, null, null);
        return new CpfMessageContextBundle(CpfContextSnapshot.capture(context), message);
    }

    public void consume(CpfMessageContextBundle bundle, Runnable work) {
        Objects.requireNonNull(bundle, "bundle");
        Objects.requireNonNull(work, "work");
        CpfContexts.run(bundle.snapshot(), work);
    }

    public static Map<String, String> mergeUserHeaders(Map<String, String> canonical, Map<String, String> user) {
        Map<String, String> result = new LinkedHashMap<>(canonical == null ? Map.of() : canonical);
        if (user != null) {
            for (var entry : user.entrySet()) {
                String key = entry.getKey().toLowerCase(Locale.ROOT);
                if (key.contains("authorization") || key.contains("api-key") || key.contains("cookie")
                        || key.contains("password") || key.contains("secret")) {
                    throw new SecurityException("credential metadata forbidden");
                }
                if (result.keySet().stream().anyMatch(existing -> existing.equalsIgnoreCase(entry.getKey()))) {
                    throw new IllegalArgumentException("cannot override canonical context header");
                }
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return Map.copyOf(result);
    }

    private static String required(Map<String, String> headers, String key) {
        String value = text(headers.get(key));
        if (value == null) throw new IllegalArgumentException("missing message context header " + key);
        return value;
    }

    private static String text(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
