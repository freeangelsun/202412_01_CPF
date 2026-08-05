package com.cpf.batch.control;

import com.cpf.batch.api.CommandState;
import com.cpf.batch.api.RuntimeCommand;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Canonical request identity used to make runtime-command idempotency fail closed. */
final class RuntimeCommandIdentity {
    private RuntimeCommandIdentity() {
    }

    static RuntimeCommand normalize(RuntimeCommand command) {
        Objects.requireNonNull(command, "command");
        if (command.parameters() != null && !command.parameters().isEmpty()) {
            throw conflict(
                    "BATCH_RUNTIME_COMMAND_PARAMETERS_NOT_PERSISTED",
                    "Runtime command parameters are not part of the durable idempotency identity");
        }

        List<String> targets = new ArrayList<>(command.targetIds().size());
        for (String target : command.targetIds()) {
            if (target == null) {
                targets.add("");
            } else {
                targets.add(target.trim());
            }
        }
        String snapshot = canonicalTargetSnapshot(targets);
        String snapshotHash = sha256(snapshot);
        requireCompatibleOptional("targetSnapshot", command.targetSnapshot(), snapshot);
        requireCompatibleOptionalIgnoreCase(
                "targetSnapshotHash", command.targetSnapshotHash(), snapshotHash);

        return new RuntimeCommand(
                command.commandId().trim(),
                command.idempotencyKey().trim(),
                normalizeUpper(command.commandType()),
                command.targetType().trim(),
                List.copyOf(targets),
                snapshot,
                snapshotHash,
                command.expectedVersion(),
                command.requestedBy().trim(),
                command.reason().trim(),
                command.requestedAt(),
                trimNullable(command.approvalPolicyVersion()),
                trimNullable(command.approvalRequestId()),
                trimNullable(command.approvedBy()),
                command.expiresAt(),
                CommandState.APPROVED,
                0,
                Map.of(),
                null,
                null,
                null,
                null,
                trimNullable(command.transactionId()),
                null);
    }

    static void assertMatches(RuntimeCommand command, Map<String, Object> persisted) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(persisted, "persisted");
        compareString(persisted, "command_id", command.commandId(), false);
        compareString(persisted, "idempotency_key", command.idempotencyKey(), false);
        compareString(persisted, "command_type", command.commandType(), true);
        compareString(persisted, "target_type", command.targetType(), false);
        compareString(persisted, "target_snapshot", command.targetSnapshot(), false);
        compareString(persisted, "target_snapshot_hash", command.targetSnapshotHash(), true);
        compareLong(persisted, "expected_version", command.expectedVersion());
        compareString(persisted, "requested_by", command.requestedBy(), false);
        compareString(persisted, "reason_text", command.reason(), false);
        compareString(
                persisted, "approval_policy_version", command.approvalPolicyVersion(), false);
        compareString(persisted, "approval_request_id", command.approvalRequestId(), false);
        compareString(persisted, "approved_by", command.approvedBy(), false);
        compareInstant(persisted, "expires_at", command.expiresAt());
        compareString(persisted, "transaction_id", command.transactionId(), false);
    }

    static String canonicalTargetSnapshot(List<String> targetIds) {
        Objects.requireNonNull(targetIds, "targetIds");
        StringBuilder json = new StringBuilder("[");
        for (int index = 0; index < targetIds.size(); index++) {
            if (index > 0) {
                json.append(',');
            }
            json.append('"').append(escapeJson(Objects.toString(targetIds.get(index), ""))).append('"');
        }
        return json.append(']').toString();
    }

    static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private static void compareString(
            Map<String, Object> persisted, String key, String expected, boolean ignoreCase) {
        Object raw = requiredValue(persisted, key);
        String actual = raw == null ? null : String.valueOf(raw).trim();
        String normalizedExpected = trimNullable(expected);
        boolean equal = ignoreCase
                ? Objects.equals(lower(actual), lower(normalizedExpected))
                : Objects.equals(actual, normalizedExpected);
        if (!equal) {
            throw identityConflict(key);
        }
    }

    private static void compareLong(Map<String, Object> persisted, String key, long expected) {
        Object raw = requiredValue(persisted, key);
        long actual;
        if (raw instanceof Number number) {
            actual = number.longValue();
        } else {
            try {
                actual = new BigDecimal(String.valueOf(raw).trim()).longValueExact();
            } catch (RuntimeException failure) {
                throw identityConflict(key);
            }
        }
        if (actual != expected) {
            throw identityConflict(key);
        }
    }

    private static void compareInstant(
            Map<String, Object> persisted, String key, Instant expected) {
        Object raw = requiredValue(persisted, key);
        Instant actual = toInstant(raw);
        Instant normalizedExpected = truncateMicros(expected);
        if (!Objects.equals(truncateMicros(actual), normalizedExpected)) {
            throw identityConflict(key);
        }
    }

    private static Instant toInstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toInstant();
        }
        try {
            return Instant.parse(String.valueOf(value).trim());
        } catch (RuntimeException failure) {
            throw conflict(
                    "BATCH_RUNTIME_COMMAND_IDEMPOTENCY_CONFLICT",
                    "Persisted runtime command timestamp cannot be compared safely");
        }
    }

    private static Instant truncateMicros(Instant value) {
        return value == null ? null : value.truncatedTo(ChronoUnit.MICROS);
    }

    private static Object requiredValue(Map<String, Object> persisted, String key) {
        for (Map.Entry<String, Object> entry : persisted.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        throw conflict(
                "BATCH_RUNTIME_COMMAND_PERSISTED_IDENTITY_INCOMPLETE",
                "Persisted runtime command identity is missing column " + key);
    }

    private static void requireCompatibleOptional(String field, String supplied, String canonical) {
        if (supplied != null && !supplied.isBlank() && !supplied.equals(canonical)) {
            throw conflict(
                    "BATCH_RUNTIME_COMMAND_TARGET_SNAPSHOT_MISMATCH",
                    field + " does not match the canonical target list");
        }
    }

    private static void requireCompatibleOptionalIgnoreCase(
            String field, String supplied, String canonical) {
        if (supplied != null && !supplied.isBlank() && !supplied.equalsIgnoreCase(canonical)) {
            throw conflict(
                    "BATCH_RUNTIME_COMMAND_TARGET_SNAPSHOT_MISMATCH",
                    field + " does not match the canonical target list");
        }
    }

    private static RuntimeCommandExecutionException identityConflict(String field) {
        return conflict(
                "BATCH_RUNTIME_COMMAND_IDEMPOTENCY_CONFLICT",
                "Idempotency key is already bound to a different runtime command field: " + field);
    }

    private static RuntimeCommandExecutionException conflict(String code, String message) {
        return new RuntimeCommandExecutionException(code, CommandState.FAILED, message);
    }

    private static String normalizeUpper(String value) {
        return Objects.toString(value, "").trim().toUpperCase(Locale.ROOT);
    }

    private static String trimNullable(String value) {
        return value == null ? null : value.trim();
    }

    private static String lower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private static String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 8);
        for (int index = 0; index < value.length(); index++) {
            char ch = value.charAt(index);
            switch (ch) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (ch < 0x20) {
                        escaped.append(String.format(Locale.ROOT, "\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
