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

/** Validates HTTP trust-boundary metadata and creates the topology-neutral CPF context. */
public final class CpfHttpInboundContextAdapter {
    private final CpfTransactionIdGenerator transactionIds;
    private final CpfExecutionIdGenerator executionIds;

    public CpfHttpInboundContextAdapter(CpfTransactionIdGenerator transactionIds, CpfExecutionIdGenerator executionIds) {
        this.transactionIds = Objects.requireNonNull(transactionIds, "transactionIds");
        this.executionIds = Objects.requireNonNull(executionIds, "executionIds");
    }

    /** Compatibility overload. Runtime identity verification is enabled by the web filter overload below. */
    public CpfHttpInboundResult resolve(Map<String,String> headers, CpfHttpIngressTrust trust,
            CpfContext.CpfIdentityContext authenticated, CpfContext.CpfTenantContext tenant,
            CpfHttpIngressMetadata edge, String standardExecutionId, LocalDate businessDate, Instant deadline) {
        return resolve(headers, trust, authenticated, tenant, edge, standardExecutionId, businessDate, deadline, null);
    }

    public CpfHttpInboundResult resolve(Map<String,String> headers, CpfHttpIngressTrust trust,
            CpfContext.CpfIdentityContext authenticated, CpfContext.CpfTenantContext tenant,
            CpfHttpIngressMetadata edge, String standardExecutionId, LocalDate businessDate, Instant deadline,
            CpfRuntimeIdentity runtime) {
        Map<String,String> values = caseInsensitive(headers);
        CpfHttpIngressTrust effectiveTrust = trust == null ? CpfHttpIngressTrust.UNTRUSTED_EXTERNAL : trust;
        String runtimeSystem = normalizeSystem(runtime == null
                ? (edge == null ? null : edge.currentChannel()) : runtime.systemCode());
        String runtimeChannel = normalizeChannel(runtime == null
                ? (edge == null ? null : edge.currentChannel()) : runtime.currentChannel());

        String rawTx = safe(values.get(lower(CpfHttpHeaderNames.TRANSACTION_ID)), 160);
        String originalSystem = safe(values.get(lower(CpfHttpHeaderNames.ORIGINAL_SYSTEM_CODE)), 32);
        String inboundSystem = safe(values.get(lower(CpfHttpHeaderNames.SYSTEM_CODE)), 32);
        String callerSystem = safe(values.get(lower(CpfHttpHeaderNames.CALLER_SYSTEM_CODE)), 32);
        String targetSystem = safe(values.get(lower(CpfHttpHeaderNames.TARGET_SYSTEM_CODE)), 32);
        String targetOperation = safe(values.get(lower(CpfHttpHeaderNames.TARGET_OPERATION_ID)), 160);

        // Channel is optional policy/context and never substitutes for System lineage.
        String originalChannel = safe(values.get(lower(CpfHttpHeaderNames.ORIGINAL_CHANNEL)), 16);
        String inboundCurrentChannel = safe(values.get(lower(CpfHttpHeaderNames.CURRENT_CHANNEL)), 16);
        String callerChannel = safe(values.get(lower(CpfHttpHeaderNames.CALLER_CHANNEL)), 16);
        String targetChannel = safe(values.get(lower(CpfHttpHeaderNames.TARGET_CHANNEL)), 16);

        String inboundTransactionId;
        if (effectiveTrust == CpfHttpIngressTrust.TRUSTED_INTERNAL) {
            requireInternal(rawTx, CpfHttpHeaderNames.TRANSACTION_ID);
            requireInternal(originalSystem, CpfHttpHeaderNames.ORIGINAL_SYSTEM_CODE);
            requireInternal(inboundSystem, CpfHttpHeaderNames.SYSTEM_CODE);
            requireInternal(callerSystem, CpfHttpHeaderNames.CALLER_SYSTEM_CODE);
            requireInternal(targetSystem, CpfHttpHeaderNames.TARGET_SYSTEM_CODE);
            requireInternal(targetOperation, CpfHttpHeaderNames.TARGET_OPERATION_ID);
            inboundTransactionId = canonicalTransactionId(rawTx);

            originalSystem = normalizeSystemRequired(originalSystem, CpfHttpHeaderNames.ORIGINAL_SYSTEM_CODE);
            inboundSystem = normalizeSystemRequired(inboundSystem, CpfHttpHeaderNames.SYSTEM_CODE);
            callerSystem = normalizeSystemRequired(callerSystem, CpfHttpHeaderNames.CALLER_SYSTEM_CODE);
            targetSystem = normalizeSystemRequired(targetSystem, CpfHttpHeaderNames.TARGET_SYSTEM_CODE);
            validateReceiverSystem(runtimeSystem, inboundSystem, targetSystem);

            String verifiedCallerSystem = edge == null ? null : normalizeSystem(edge.verifiedCallerSystemCode());
            if (verifiedCallerSystem != null && !verifiedCallerSystem.equals(callerSystem)) {
                throw trustViolation(CpfHttpHeaderNames.CALLER_SYSTEM_CODE,
                        "X-Caller-System-Code does not match the authenticated internal caller identity.");
            }
        } else {
            // External channel callers provide five transport values; X-System-Code remains receiver-owned.
            requireExternal(rawTx, CpfHttpHeaderNames.TRANSACTION_ID);
            requireExternal(originalSystem, CpfHttpHeaderNames.ORIGINAL_SYSTEM_CODE);
            requireExternal(callerSystem, CpfHttpHeaderNames.CALLER_SYSTEM_CODE);
            requireExternal(targetSystem, CpfHttpHeaderNames.TARGET_SYSTEM_CODE);
            requireExternal(targetOperation, CpfHttpHeaderNames.TARGET_OPERATION_ID);
            inboundTransactionId = canonicalTransactionId(rawTx);

            originalSystem = normalizeSystemRequired(originalSystem, CpfHttpHeaderNames.ORIGINAL_SYSTEM_CODE);
            callerSystem = normalizeSystemRequired(callerSystem, CpfHttpHeaderNames.CALLER_SYSTEM_CODE);
            targetSystem = normalizeSystemRequired(targetSystem, CpfHttpHeaderNames.TARGET_SYSTEM_CODE);
            if (runtimeSystem == null) {
                throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                        CpfHttpHeaderNames.SYSTEM_CODE,
                        "Receiver runtime System Code is required before Controller invocation.",
                        503, "RUNTIME_SYSTEM_UNAVAILABLE");
            }
            if (!runtimeSystem.equals(targetSystem)) {
                throw protocolMismatch(CpfHttpHeaderNames.TARGET_SYSTEM_CODE,
                        "X-Target-System-Code does not identify this receiver runtime.", "TARGET_SYSTEM_CODE_MISMATCH");
            }
            if (inboundSystem != null) {
                throw trustViolation(CpfHttpHeaderNames.SYSTEM_CODE,
                        "External ingress cannot assert receiver-owned X-System-Code.");
            }
            inboundSystem = runtimeSystem;
        }

        originalChannel = normalizeChannel(originalChannel);
        inboundCurrentChannel = normalizeChannel(inboundCurrentChannel);
        callerChannel = normalizeChannel(callerChannel);
        targetChannel = normalizeChannel(targetChannel);
        validateOptionalReceiverChannel(runtimeChannel, inboundCurrentChannel, targetChannel);

        String correlation = safe(values.get(lower(CpfHttpHeaderNames.CORRELATION_ID)), 160);
        String tx = inboundTransactionId == null ? requireGeneratedTransactionId(transactionIds.newTransactionId()) : inboundTransactionId;
        String issuerCode = CpfTransactionIds.issuerCode(tx);

        String traceparent = validatedTraceParent(values.get(lower(CpfHttpHeaderNames.TRACEPARENT)));
        String traceId = traceparent == null ? null : traceparent.substring(3, 35);
        String idempotency = safe(firstHeader(values, CpfHttpHeaderNames.IDEMPOTENCY_KEY, CpfHttpHeaderNames.IDEMPOTENCY_LEGACY), 256);
        CpfContext.CpfOperationContext operation = (idempotency == null && targetOperation == null) ? null
                : new CpfContext.CpfOperationContext(targetOperation, standardExecutionId, null, idempotency,
                        CpfContext.CpfIdempotencyScope.CURRENT_OPERATION,
                        idempotency == null ? CpfContext.CpfIdempotencyMode.NONE : CpfContext.CpfIdempotencyMode.OPTIONAL,
                        null, null, targetOperation, 1L);

        String executionId = executionIds.newExecutionId();
        String segmentId = executionIds.newSegmentId();
        Instant now = Instant.now();
        CpfContext context = new CpfContext(
                new CpfContext.CpfTransactionContext(
                        tx, tx, null, correlation, traceId,
                        originalSystem, inboundSystem, callerSystem, targetSystem,
                        originalChannel, runtimeChannel, callerChannel, targetChannel,
                        Objects.requireNonNull(businessDate, "businessDate"), now,
                        CpfContext.CpfTransactionOriginKind.HTTP, issuerCode, null),
                new CpfContext.CpfExecutionContext(standardExecutionId, executionId, executionId, null,
                        segmentId, null, CpfContext.CpfExecutionType.API, 1, 0, now, deadline,
                        CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),
                operation, authenticated, tenant);

        String country = firstNonBlank(safe(values.get(lower(CpfHttpHeaderNames.COUNTRY_CODE)), 16), edge == null ? null : safe(edge.countryCode(), 16));
        String clientId = safe(values.get(lower(CpfHttpHeaderNames.CLIENT_ID)), 160);
        String clientInstanceId = safe(values.get(lower(CpfHttpHeaderNames.CLIENT_INSTANCE_ID)), 160);
        String clientVersion = safe(values.get(lower(CpfHttpHeaderNames.CLIENT_VERSION)), 64);
        String deviceId = safe(values.get(lower(CpfHttpHeaderNames.DEVICE_ID)), 160);
        String locale = safe(values.get(lower(CpfHttpHeaderNames.ACCEPT_LANGUAGE)), 128);
        String userAgent = safe(values.get(lower(CpfHttpHeaderNames.USER_AGENT)), 512);

        CpfWebContext webContext = new CpfWebContext(
                UUID.randomUUID().toString(), safe(values.get(lower(CpfHttpHeaderNames.REQUEST_ID)), 160),
                edge == null ? null : safe(edge.apiVersion(), 64), country, clientId, clientInstanceId,
                clientVersion, deviceId, locale, edge == null ? null : safe(edge.clientIp(), 128), userAgent,
                traceparent, safe(values.get(lower(CpfHttpHeaderNames.TRACESTATE)), 512), effectiveTrust);
        return new CpfHttpInboundResult(CpfContextSnapshot.capture(context), webContext);
    }

    private static void requireExternal(String value, String header) {
        if (value == null || value.isBlank()) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    header, "External CPF protocol call requires " + header + ".", 400, "EXTERNAL_HEADER_REQUIRED");
        }
    }

    private static void validateReceiverSystem(String runtimeSystem, String inboundSystem, String targetSystem) {
        if (runtimeSystem == null) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    CpfHttpHeaderNames.SYSTEM_CODE,
                    "Receiver runtime System Code is required before Controller invocation.",
                    503, "RUNTIME_SYSTEM_UNAVAILABLE");
        }
        if (!runtimeSystem.equals(inboundSystem)) {
            throw protocolMismatch(CpfHttpHeaderNames.SYSTEM_CODE,
                    "X-System-Code does not match the receiver runtime System Code.", "SYSTEM_CODE_MISMATCH");
        }
        if (!runtimeSystem.equals(targetSystem)) {
            throw protocolMismatch(CpfHttpHeaderNames.TARGET_SYSTEM_CODE,
                    "X-Target-System-Code does not identify this receiver runtime.", "TARGET_SYSTEM_CODE_MISMATCH");
        }
    }

    private static void validateOptionalReceiverChannel(String runtimeChannel, String inboundCurrentChannel, String targetChannel) {
        if (runtimeChannel == null) return;
        if (inboundCurrentChannel != null && !runtimeChannel.equals(inboundCurrentChannel)) {
            throw protocolMismatch(CpfHttpHeaderNames.CURRENT_CHANNEL,
                    "X-Current-Channel does not match the receiver runtime Channel.", "CURRENT_CHANNEL_MISMATCH");
        }
        if (targetChannel != null && !runtimeChannel.equals(targetChannel)) {
            throw protocolMismatch(CpfHttpHeaderNames.TARGET_CHANNEL,
                    "X-Target-Channel does not identify this receiver runtime Channel.", "TARGET_CHANNEL_MISMATCH");
        }
    }

    static String canonicalTransactionId(String value) {
        String normalized = safe(value, 160);
        if (normalized == null) return null;
        if (!CpfTransactionIds.isCanonical(normalized)) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    CpfHttpHeaderNames.TRANSACTION_ID,
                    "transactionId must use the CPF canonical format.", 400, "TRANSACTION_ID_INVALID");
        }
        return normalized;
    }

    private static String requireGeneratedTransactionId(String value) {
        String normalized = canonicalTransactionId(value);
        if (normalized == null) throw new IllegalStateException("transaction id generator returned blank value");
        return normalized;
    }

    private static void rejectUntrustedProtocolAssertion(Map<String,String> values, String header) {
        if (safe(values.get(lower(header)), 256) != null) {
            throw trustViolation(header, "External ingress cannot assert CPF protected routing headers.");
        }
    }

    private static CpfHeaderValidationException protocolMismatch(String header, String message, String category) {
        return new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                header, message, 409, category);
    }

    private static CpfHeaderValidationException trustViolation(String header, String message) {
        return new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                header, message, 403, "HEADER_TRUST_VIOLATION");
    }

    private static void requireInternal(String value, String header) {
        if (value == null || value.isBlank()) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.MISSING_TRANSACTION_HEADER,
                    header, "Missing required internal CPF header: " + header, 400, "HEADER_REQUIRED");
        }
    }

    private static Map<String,String> caseInsensitive(Map<String,String> input) {
        if (input == null || input.isEmpty()) return Map.of();
        LinkedHashMap<String,String> result = new LinkedHashMap<>();
        input.forEach((k,v) -> { if (k != null) result.put(lower(k), v); });
        return result;
    }

    private static String firstHeader(Map<String,String> values, String... names) {
        for (String name : names) {
            String value = values.get(lower(name));
            if (value != null) return value;
        }
        return null;
    }

    private static String normalizeChannel(String value) {
        String normalized = safe(value, 16);
        if (normalized == null) return null;
        if (!normalized.matches("[A-Z0-9][A-Z0-9_-]{0,15}")) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    "*", "Channel must match [A-Z0-9][A-Z0-9_-]{0,15}.", 400, "CHANNEL_INVALID");
        }
        return normalized;
    }

    private static String normalizeSystem(String value) {
        String normalized = safe(value, 32);
        if (normalized == null) return null;
        normalized = normalized.toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9][A-Z0-9_-]{0,31}")) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    "*", "System Code must match [A-Z0-9][A-Z0-9_-]{0,31}.", 400, "SYSTEM_CODE_INVALID");
        }
        return normalized;
    }

    private static String normalizeSystemRequired(String value, String header) {
        String normalized = normalizeSystem(value);
        if (normalized == null) requireInternal(value, header);
        return normalized;
    }

    private static String firstNonBlank(String first, String second) { return first != null && !first.isBlank() ? first : second; }
    private static String lower(String value) { return value.toLowerCase(Locale.ROOT); }

    private static String safe(String value, int max) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.length() > max || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    "*", "Header value is malformed or oversized.", 400, "HEADER_INVALID");
        }
        return normalized;
    }

    private static String validatedTraceParent(String value) {
        String normalized = safe(value, 256);
        if (normalized == null) return null;
        if (!normalized.matches("^[0-9a-fA-F]{2}-[0-9a-fA-F]{32}-[0-9a-fA-F]{16}-[0-9a-fA-F]{2}$")) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    CpfHttpHeaderNames.TRACEPARENT, "Invalid W3C traceparent.", 400, "TRACEPARENT_INVALID");
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (lower.substring(3, 35).equals("00000000000000000000000000000000")
                || lower.substring(36, 52).equals("0000000000000000")) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    CpfHttpHeaderNames.TRACEPARENT, "Invalid W3C traceparent.", 400, "TRACEPARENT_INVALID");
        }
        return lower;
    }
}
