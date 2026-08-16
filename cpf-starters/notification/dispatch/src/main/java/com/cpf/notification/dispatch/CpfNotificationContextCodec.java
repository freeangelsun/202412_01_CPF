package com.cpf.notification.dispatch;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.notification.api.CpfNotificationRequest;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.notification.context.CpfNotificationContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Outbox에 민감정보 없이 bounded CPF lineage를 저장/복원합니다. */
public final class CpfNotificationContextCodec {
    private final CpfContextExecutionFactory factory;

    public CpfNotificationContextCodec(CpfContextExecutionFactory factory) {
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    public String capture() {
        CpfContext context = CpfContexts.requireCurrent();
        Map<String, String> values = new LinkedHashMap<>();
        put(values, "tx", context.transaction().transactionId());
        put(values, "rt", context.transaction().rootTransactionId());
        put(values, "co", context.transaction().correlationId());
        put(values, "bd", context.transaction().businessDate().toString());
        put(values, "ts", context.transaction().startedAt().toString());
        put(values, "re", context.execution().rootExecutionId());
        put(values, "pe", context.execution().executionId());
        put(values, "ps", context.execution().segmentId());
        if (context.tenant() != null) {
            put(values, "te", context.tenant().tenantId());
            put(values, "tr", context.tenant().tenantRealm());
        }
        if (context.execution().deadline() != null) put(values, "dl", context.execution().deadline().toString());
        String encoded = encode(values);
        if (encoded.length() > 3500) throw new IllegalStateException("NOTIFICATION_CONTEXT_TOO_LARGE");
        return encoded;
    }

    public AutoCloseable bind(
            String encoded, CpfNotificationRequest request, int attempt, boolean reconcile, String provider) {
        return CpfContexts.bind(restore(encoded, request, attempt, reconcile, provider));
    }

    public CpfContextSnapshot restore(
            String encoded, CpfNotificationRequest request, int attempt, boolean reconcile, String provider) {
        Map<String, String> values = decode(encoded);
        if (!required(values, "tx").equals(request.transactionId())) {
            throw new SecurityException("NOTIFICATION_TRANSACTION_CONTEXT_MISMATCH");
        }
        LocalDate businessDate = LocalDate.parse(required(values, "bd"));
        Instant startedAt = Instant.parse(required(values, "ts"));
        Instant deadline = text(values.get("dl")) == null ? null : Instant.parse(values.get("dl"));
        int normalizedAttempt = Math.max(1, attempt);
        CpfContext context = factory.fromTrustedPropagation(
                required(values, "tx"), values.get("rt"), values.get("co"), businessDate, startedAt,
                reconcile ? CpfContext.CpfTransactionOriginKind.RECOVERY : CpfContext.CpfTransactionOriginKind.INTERNAL,
                "notification-outbox", request.notificationId(), "notification." + request.channel(),
                values.get("pe"), values.get("re"), values.get("ps"),
                reconcile ? CpfContext.CpfExecutionType.INTERNAL : CpfContext.CpfExecutionType.INTEGRATION,
                normalizedAttempt, 1, null, null,
                text(values.get("te")) == null ? null : new CpfContext.CpfTenantContext(values.get("te"), values.get("tr")),
                deadline);
        // Create owner-local metadata only for audit/metrics hooks. Recipient is always hashed.
        new CpfNotificationContext(
                request.notificationId(), request.channel(), request.templateId(), provider,
                sha256(request.recipient()), normalizedAttempt, request.notificationId(),
                reconcile ? request.notificationId() : null);
        return CpfContextSnapshot.capture(context);
    }

    private static String encode(Map<String, String> values) {
        StringBuilder raw = new StringBuilder();
        values.forEach((key, value) -> raw.append(escape(key)).append('=').append(escape(value)).append('\n'));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static Map<String, String> decode(String encoded) {
        if (encoded == null || encoded.isBlank()) throw new IllegalArgumentException("notification context is required");
        String raw;
        try { raw = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("invalid notification context", e); }
        Map<String, String> values = new LinkedHashMap<>();
        for (String line : raw.split("\\n")) {
            if (line.isBlank()) continue;
            int split = line.indexOf('=');
            if (split <= 0) throw new IllegalArgumentException("invalid notification context entry");
            String key = unescape(line.substring(0, split));
            String value = unescape(line.substring(split + 1));
            if (values.put(key, value) != null) throw new IllegalArgumentException("duplicate notification context key");
        }
        return Map.copyOf(values);
    }

    private static String escape(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
    private static String unescape(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(Objects.toString(value, "").getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException("SHA-256 unavailable", e); }
    }
    private static void put(Map<String, String> map, String key, String value) {
        if (text(value) != null) map.put(key, value.trim());
    }
    private static String required(Map<String, String> map, String key) {
        String value = text(map.get(key));
        if (value == null) throw new IllegalStateException("missing notification context " + key);
        return value;
    }
    private static String text(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
