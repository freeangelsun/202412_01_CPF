package com.cpf.security.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Optimistic, idempotent command that replaces the complete active masking policy. */
/** CpfMaskingPolicyUpdateCommand 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfMaskingPolicyUpdateCommand(
        String commandId,
        long expectedVersion,
        Set<String> sensitiveKeys,
        int maxLength,
        boolean maskBearerToken,
        String actor,
        String reason,
        CpfMaskingPolicyApproval approval) {
    private static final Pattern ID = Pattern.compile("[A-Za-z0-9_.:-]{8,128}");
    private static final Pattern KEY = Pattern.compile("[a-z0-9_.-]{2,64}");

    public CpfMaskingPolicyUpdateCommand {
        commandId = identifier(commandId, "commandId");
        if (expectedVersion < 1L) throw new IllegalArgumentException("expectedVersion must be positive");
        if (sensitiveKeys == null || sensitiveKeys.isEmpty() || sensitiveKeys.size() > 256) {
            throw new IllegalArgumentException("sensitiveKeys must contain 1..256 values");
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String key : sensitiveKeys) {
            if (key == null || key.isBlank()) throw new IllegalArgumentException("sensitive key is blank");
            String value = key.trim().toLowerCase(Locale.ROOT);
            if (!KEY.matcher(value).matches()) throw new IllegalArgumentException("invalid sensitive key");
            if (!normalized.add(value)) throw new IllegalArgumentException("duplicate sensitive key");
        }
        sensitiveKeys = Set.copyOf(normalized);
        if (maxLength < 256 || maxLength > 65_536) {
            throw new IllegalArgumentException("maxLength must be between 256 and 65536");
        }
        actor = identifier(actor, "actor");
        reason = CpfSensitiveData.sanitizeAuditReason(reason);
    }

    /** commandHash 작업을 CPF 표준 계약에 따라 수행한다. */
    public String commandHash() {
        String canonical = "UPDATE|" + commandId + "|" + expectedVersion + "|" + String.join(",", sensitiveKeys.stream().sorted().toList())
                + "|" + maxLength + "|" + maskBearerToken + "|" + actor + "|" + reason;
        return sha256(canonical);
    }

    private static String identifier(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        String normalized = value.trim();
        if (!ID.matcher(normalized).matches()) throw new IllegalArgumentException(field + " is invalid");
        return normalized;
    }

    static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (NoSuchAlgorithmException unavailable) {
            throw new IllegalStateException("SHA-256 unavailable", unavailable);
        }
    }
}
