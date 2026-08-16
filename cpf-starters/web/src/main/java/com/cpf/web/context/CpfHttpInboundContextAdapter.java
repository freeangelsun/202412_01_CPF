package com.cpf.web.context;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.error.CpfFrameworkErrorCode;
import com.cpf.core.api.transaction.CpfTransactionIds;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** 외부 Header를 검증한 뒤 Core Context 의미로 변환하는 Web Owner adapter입니다. */
public final class CpfHttpInboundContextAdapter {
    private final CpfTransactionIdGenerator transactionIds;
    private final CpfExecutionIdGenerator executionIds;

    public CpfHttpInboundContextAdapter(CpfTransactionIdGenerator transactionIds, CpfExecutionIdGenerator executionIds) {
        this.transactionIds = Objects.requireNonNull(transactionIds, "transactionIds");
        this.executionIds = Objects.requireNonNull(executionIds, "executionIds");
    }

    public CpfHttpInboundResult resolve(Map<String, String> headers, CpfHttpIngressTrust trust,
            CpfContext.CpfIdentityContext authenticated, CpfContext.CpfTenantContext tenant,
            CpfHttpIngressMetadata edge, String standardExecutionId, LocalDate businessDate, Instant deadline) {
        Map<String, String> values = caseInsensitive(headers);
        CpfHttpIngressTrust effectiveTrust = trust == null ? CpfHttpIngressTrust.UNTRUSTED_EXTERNAL : trust;
        String correlation = safe(header(values,
                CpfHttpHeaderNames.CORRELATION_ID,
                CpfHttpHeaderNames.LEGACY_CORRELATION_ID), 160);
        String traceparent = validatedTraceParent(values.get(lower(CpfHttpHeaderNames.TRACEPARENT)));
        String traceId = traceparent == null ? null : traceparent.substring(3, 35);
        String channelCode = edge == null ? null : safe(edge.channelCode(), 64);
        String headerCaller = safe(values.get(lower(CpfHttpHeaderNames.CALLER)), 128);
        String headerTarget = safe(values.get(lower(CpfHttpHeaderNames.TARGET)), 128);
        String callerSystemCode = edge == null ? headerCaller : safe(edge.callerApplication(), 128);
        String idempotency = safe(header(values,
                CpfHttpHeaderNames.IDEMPOTENCY_KEY,
                CpfHttpHeaderNames.LEGACY_IDEMPOTENCY_KEY), 256);
        CpfContext.CpfOperationContext operation = idempotency == null ? null : new CpfContext.CpfOperationContext(
                null, standardExecutionId, null, idempotency, CpfContext.CpfIdempotencyScope.CURRENT_OPERATION,
                CpfContext.CpfIdempotencyMode.OPTIONAL, null, null);

        // 외부 최초 진입은 transactionId를 생성할 수 있지만 내부 hop은 표준 Header 누락을 허용하지 않습니다.
        String rawInboundTransactionId = safe(header(values,
                CpfHttpHeaderNames.TRANSACTION_ID,
                CpfHttpHeaderNames.LEGACY_TRANSACTION_ID), 160);
        String inboundExecutionId = safe(header(values,
                CpfHttpHeaderNames.EXECUTION_ID,
                CpfHttpHeaderNames.LEGACY_EXECUTION_ID), 160);
        String inboundTransactionId = null;
        if (effectiveTrust == CpfHttpIngressTrust.TRUSTED_INTERNAL) {
            requireInternalHeader(rawInboundTransactionId, CpfHttpHeaderNames.TRANSACTION_ID);
            inboundTransactionId = canonicalTransactionId(rawInboundTransactionId);
            requireInternalHeader(inboundExecutionId, CpfHttpHeaderNames.EXECUTION_ID);
            requireInternalHeader(headerCaller, CpfHttpHeaderNames.CALLER);
            requireInternalHeader(headerTarget, CpfHttpHeaderNames.TARGET);
            // Edge가 인증한 caller가 있다면 전달 Header와 반드시 일치해야 합니다.
            if (edge != null && edge.callerApplication() != null
                    && !edge.callerApplication().trim().equals(headerCaller)) {
                throw new CpfHeaderValidationException(
                        CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                        CpfHttpHeaderNames.CALLER,
                        "내부 거래 Caller Header가 인증된 caller와 일치하지 않습니다.");
            }
        }

        // 비신뢰 Client가 내부 transactionId를 주입해도 승계하지 않는다. 외부 값은 correlation으로만 보존한다.
        if (effectiveTrust != CpfHttpIngressTrust.TRUSTED_INTERNAL && rawInboundTransactionId != null && correlation == null) {
            correlation = rawInboundTransactionId;
        }
        String tx = inboundTransactionId == null
                ? requireGeneratedTransactionId(transactionIds.newTransactionId())
                : inboundTransactionId;
        String rootTx = first(safe(header(values,
                CpfHttpHeaderNames.ROOT_TRANSACTION_ID,
                CpfHttpHeaderNames.LEGACY_ROOT_TRANSACTION_ID), 160), tx);
        String rootExecution = first(safe(header(values,
                CpfHttpHeaderNames.ROOT_EXECUTION_ID,
                CpfHttpHeaderNames.LEGACY_ROOT_EXECUTION_ID), 160), inboundExecutionId);
        String parentSegment = safe(header(values,
                CpfHttpHeaderNames.SEGMENT_ID,
                CpfHttpHeaderNames.LEGACY_SEGMENT_ID), 160);

        String ex = executionIds.newExecutionId();
        String sg = executionIds.newSegmentId();
        Instant now = Instant.now();
        CpfContext context = new CpfContext(
                new CpfContext.CpfTransactionContext(tx, rootTx, null, correlation, traceId, channelCode, callerSystemCode,
                        businessDate, now, CpfContext.CpfTransactionOriginKind.HTTP, callerSystemCode, null),
                new CpfContext.CpfExecutionContext(standardExecutionId, ex,
                        rootExecution == null ? ex : rootExecution,
                        inboundExecutionId, sg, parentSegment,
                        CpfContext.CpfExecutionType.API, 1, 0, now, deadline, CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),
                operation, authenticated, tenant);
        CpfWebContext webContext = new CpfWebContext(
                UUID.randomUUID().toString(), safe(header(values,
                        CpfHttpHeaderNames.REQUEST_ID,
                        CpfHttpHeaderNames.LEGACY_REQUEST_ID), 160),
                safe(values.get(lower(CpfHttpHeaderNames.API_VERSION)), 64), channelCode,
                safe(values.get(lower(CpfHttpHeaderNames.CLIENT_APP)), 128), safe(values.get(lower(CpfHttpHeaderNames.CLIENT_VERSION)), 64),
                safe(values.get(lower(CpfHttpHeaderNames.SCREEN_ID)), 128), safe(values.get(lower(CpfHttpHeaderNames.DEVICE_ID)), 160),
                safe(values.get(lower(CpfHttpHeaderNames.LOCALE)), 64), safe(values.get(lower(CpfHttpHeaderNames.CLIENT_TIMEZONE)), 64),
                edge == null ? null : edge.clientIp(), safe(values.get(lower(CpfHttpHeaderNames.USER_AGENT)), 512),
                traceparent, safe(values.get(lower(CpfHttpHeaderNames.TRACESTATE)), 512), effectiveTrust);
        return new CpfHttpInboundResult(CpfContextSnapshot.capture(context), webContext);
    }

    static String canonicalTransactionId(String value) {
        String n = safe(value, 160);
        if (n == null) return null;
        if (!CpfTransactionIds.isCanonical(n)) {
            throw new CpfHeaderValidationException(
                    CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    CpfHttpHeaderNames.TRANSACTION_ID,
                    "transactionId는 CPF 34자리 표준 형식이어야 합니다.");
        }
        return n;
    }

    private static String requireGeneratedTransactionId(String value) {
        String n = canonicalTransactionId(value);
        if (n == null) throw new IllegalStateException("transaction id generator returned blank value");
        return n;
    }

    private static Map<String, String> caseInsensitive(Map<String, String> input) {
        if (input == null || input.isEmpty()) return Map.of();
        Map<String, String> result = new LinkedHashMap<>();
        input.forEach((k, v) -> { if (k != null) result.put(lower(k), v); });
        return result;
    }

    private static String lower(String v) { return v.toLowerCase(Locale.ROOT); }
    private static String first(String a, String b) { return a != null ? a : b; }

    private static String header(Map<String, String> values, String canonical, String... aliases) {
        String value = values.get(lower(canonical));
        if (value != null) return value;
        for (String alias : aliases) {
            value = values.get(lower(alias));
            if (value != null) return value;
        }
        return null;
    }

    private static void requireInternalHeader(String value, String headerName) {
        if (value == null || value.isBlank()) {
            throw new CpfHeaderValidationException(
                    CpfFrameworkErrorCode.MISSING_TRANSACTION_HEADER,
                    headerName,
                    "내부 거래 필수 Header가 없습니다: " + headerName);
        }
    }
    private static String safe(String value, int max) {
        if (value == null || value.isBlank()) return null;
        String n = value.trim();
        if (n.length() > max || n.chars().anyMatch(Character::isISOControl)) throw new IllegalArgumentException("invalid header");
        return n;
    }

    private static String validatedTraceParent(String value) {
        String n = safe(value, 256);
        if (n == null) return null;
        if (!n.matches("^[0-9a-fA-F]{2}-[0-9a-fA-F]{32}-[0-9a-fA-F]{16}-[0-9a-fA-F]{2}$")) {
            throw new IllegalArgumentException("invalid traceparent");
        }
        String lower = n.toLowerCase(Locale.ROOT);
        if (lower.substring(3, 35).equals("00000000000000000000000000000000")
                || lower.substring(36, 52).equals("0000000000000000")) {
            throw new IllegalArgumentException("invalid traceparent");
        }
        return lower;
    }
}
