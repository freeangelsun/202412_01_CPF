package com.cpf.web.context;

import com.cpf.core.api.context.CpfContext;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Builds outbound HTTP headers at the exact internal/external trust boundary. */
public final class CpfHttpOutboundContextAdapter {
    private final CpfRuntimeIdentity runtime;
    private final CpfHeaderPolicyRegistry policies;

    /** Compatibility constructor for tests/legacy consumers; runtime Channel is inferred from context when possible. */
    public CpfHttpOutboundContextAdapter() { this(null, new CpfHeaderPolicyRegistry(null)); }
    public CpfHttpOutboundContextAdapter(CpfRuntimeIdentity runtime, CpfHeaderPolicyRegistry policies) {
        this.runtime = runtime;
        this.policies = policies == null ? new CpfHeaderPolicyRegistry(null) : policies;
    }

    public Map<String,String> headers(CpfContext context, CpfWebContext interaction, CpfHttpOutboundRequest request) {
        Objects.requireNonNull(context, "context");
        CpfHttpOutboundRequest target = request == null
                ? new CpfHttpOutboundRequest(null, null, null, null, false, Map.of()) : request;
        LinkedHashMap<String,String> headers = new LinkedHashMap<>();

        if (target.trustedInternal()) {
            String callerSystem = localSystem(context);
            String targetSystem = requiredSystem(target.targetSystemCode(), CpfHttpHeaderNames.TARGET_SYSTEM_CODE);
            String operation = required(target.targetOperation(), CpfHttpHeaderNames.TARGET_OPERATION_ID);
            String originalSystem = first(context.originalSystemCode(), callerSystem);

            putRequired(headers, CpfHttpHeaderNames.TRANSACTION_ID, context.transactionId());
            putRequired(headers, CpfHttpHeaderNames.ORIGINAL_SYSTEM_CODE, originalSystem);
            // On an internal hop the sender asserts the receiver's System Code; the receiver verifies it against runtime identity.
            putRequired(headers, CpfHttpHeaderNames.SYSTEM_CODE, targetSystem);
            putRequired(headers, CpfHttpHeaderNames.CALLER_SYSTEM_CODE, callerSystem);
            putRequired(headers, CpfHttpHeaderNames.TARGET_SYSTEM_CODE, targetSystem);
            putRequired(headers, CpfHttpHeaderNames.TARGET_OPERATION_ID, operation);

            String targetChannel = optionalChannel(target.targetChannel());
            String callerChannel = localChannel(context);
            String originalChannel = first(context.originalChannel(), callerChannel);
            put(headers, CpfHttpHeaderNames.ORIGINAL_CHANNEL, originalChannel);
            put(headers, CpfHttpHeaderNames.CURRENT_CHANNEL, targetChannel);
            put(headers, CpfHttpHeaderNames.CALLER_CHANNEL, callerChannel);
            put(headers, CpfHttpHeaderNames.TARGET_CHANNEL, targetChannel);

            if (interaction != null) {
                put(headers, CpfHttpHeaderNames.COUNTRY_CODE, interaction.countryCode());
                put(headers, CpfHttpHeaderNames.CLIENT_ID, interaction.clientId());
                put(headers, CpfHttpHeaderNames.CLIENT_INSTANCE_ID, interaction.clientInstanceId());
                put(headers, CpfHttpHeaderNames.CLIENT_VERSION, interaction.clientVersion());
                put(headers, CpfHttpHeaderNames.DEVICE_ID, interaction.deviceId());
                put(headers, CpfHttpHeaderNames.USER_AGENT, interaction.userAgent());
                put(headers, CpfHttpHeaderNames.ACCEPT_LANGUAGE, interaction.locale());
                put(headers, CpfHttpHeaderNames.TRACEPARENT, interaction.traceparent());
                put(headers, CpfHttpHeaderNames.TRACESTATE, interaction.tracestate());
            }
            put(headers, CpfHttpHeaderNames.CORRELATION_ID, context.correlationId());
            if (context.operation() != null) put(headers, CpfHttpHeaderNames.IDEMPOTENCY_KEY, context.operation().idempotencyKey());
            putAllowedCustom(headers, target.customHeaders(), true);
            return Map.copyOf(headers);
        }

        // External institutions receive no CPF internal transaction/Channel headers by default.
        putAllowedCustom(headers, target.customHeaders(), false);
        return Map.copyOf(headers);
    }

    private void putAllowedCustom(Map<String,String> destination, Map<String,String> custom, boolean internal) {
        custom.forEach((name, value) -> {
            if (name == null || name.isBlank() || value == null || value.isBlank()) return;
            if (CpfHttpHeaderCatalog.isProtected(name)) {
                throw new CpfHeaderValidationException(
                        com.cpf.core.api.error.CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                        name, "Protected CPF headers cannot be overridden through custom outbound headers.",
                        403, "PROTECTED_HEADER_MUTATION");
            }
            boolean allowed = internal ? policies.internalPropagationAllowed(name) : policies.externalOutboundAllowed(name);
            if (allowed) put(destination, name, value);
        });
    }

    private String localSystem(CpfContext context) {
        if (runtime != null) return runtime.systemCode();
        String inferred = first(context.currentSystemCode(), context.originalSystemCode());
        return requiredSystem(inferred, CpfHttpHeaderNames.CALLER_SYSTEM_CODE);
    }

    private String localChannel(CpfContext context) {
        if (runtime != null && runtime.currentChannel() != null) return runtime.currentChannel();
        return optionalChannel(first(context.currentChannel(), context.originalChannel()));
    }

    private static String requiredSystem(String value, String header) {
        String normalized = required(value, header).toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9][A-Z0-9_-]{0,31}")) {
            throw new CpfHeaderValidationException(
                    com.cpf.core.api.error.CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    header, "Invalid CPF System Code.", 400, "SYSTEM_CODE_INVALID");
        }
        return normalized;
    }

    private static String optionalChannel(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9][A-Z0-9_-]{0,15}")) {
            throw new CpfHeaderValidationException(
                    com.cpf.core.api.error.CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    "*", "Invalid CPF Channel identity.", 400, "CHANNEL_INVALID");
        }
        return normalized;
    }


    private static String required(String value, String header) {
        if (value == null || value.isBlank()) {
            throw new CpfHeaderValidationException(
                    com.cpf.core.api.error.CpfFrameworkErrorCode.MISSING_TRANSACTION_HEADER,
                    header, "Missing outbound CPF protocol value: " + header, 400, "HEADER_REQUIRED");
        }
        return value.trim();
    }

    private static String first(String a, String b) { return a != null && !a.isBlank() ? a : b; }
    private static void put(Map<String,String> headers, String name, String value) {
        if (value != null && !value.isBlank()) headers.put(name, value.trim());
    }
    private static void putRequired(Map<String,String> headers, String name, String value) { headers.put(name, required(value, name)); }
}
