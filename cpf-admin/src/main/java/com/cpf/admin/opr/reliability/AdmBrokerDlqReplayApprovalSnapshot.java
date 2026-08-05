package com.cpf.admin.opr.reliability;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;

/**
 * DLQ 승인 요청과 실행 시점이 동일한 상태를 바라보는지 검증하기 위한 canonical snapshot입니다.
 * 실패 상세나 payload는 포함하지 않아 승인·감사 저장소에 민감 원문이 복제되지 않습니다.
 */
public final class AdmBrokerDlqReplayApprovalSnapshot {
    private AdmBrokerDlqReplayApprovalSnapshot() {
    }

    public static Snapshot from(Map<String, ?> row) {
        Objects.requireNonNull(row, "row");
        String messageId = required(row, "message_id");
        long dlqId = number(row, "dlq_id").longValue();
        int replayCount = number(row, "replay_count").intValue();
        if (replayCount < 0) {
            throw new IllegalArgumentException("replay_count는 0 이상이어야 합니다.");
        }
        Instant updatedAt = instant(value(row, "updated_at"));
        String json = "{" +
                "\"dlqId\":" + dlqId + ',' +
                "\"messageId\":\"" + escape(messageId) + "\"," +
                "\"topic\":\"" + escape(text(row, "topic")) + "\"," +
                "\"transactionId\":\"" + escape(text(row, "transaction_id")) + "\"," +
                "\"segmentId\":\"" + escape(text(row, "segment_id")) + "\"," +
                "\"replayStatus\":\"" + escape(required(row, "replay_status")) + "\"," +
                "\"replayCount\":" + replayCount + ',' +
                "\"updatedAtEpochMilli\":" + updatedAt.toEpochMilli() +
                '}';
        return new Snapshot(json, replayCount, updatedAt, sha256(json));
    }

    public static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(Objects.requireNonNull(value, "value").getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 계산 실패", ex);
        }
    }

    public static boolean sameHash(String expected, String actual) {
        try {
            return MessageDigest.isEqual(
                    HexFormat.of().parseHex(Objects.toString(expected, "")),
                    HexFormat.of().parseHex(Objects.toString(actual, "")));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static String required(Map<String, ?> row, String key) {
        String value = text(row, key);
        if (value.isBlank()) {
            throw new IllegalArgumentException(key + "는 필수입니다.");
        }
        return value;
    }

    private static String text(Map<String, ?> row, String key) {
        Object value = value(row, key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static Number number(Map<String, ?> row, String key) {
        Object value = value(row, key);
        if (value instanceof Number number) {
            return number;
        }
        if (value == null || String.valueOf(value).isBlank()) {
            throw new IllegalArgumentException(key + "는 필수입니다.");
        }
        return Long.parseLong(String.valueOf(value));
    }

    private static Object value(Map<String, ?> row, String key) {
        String normalizedKey = normalize(key);
        for (Map.Entry<String, ?> entry : row.entrySet()) {
            if (normalize(entry.getKey()).equals(normalizedKey)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static String normalize(String value) {
        return Objects.toString(value, "").replace("_", "").toLowerCase(java.util.Locale.ROOT);
    }

    private static Instant instant(Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        if (value instanceof java.util.Date date) {
            return date.toInstant();
        }
        String text = Objects.toString(value, "").trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("updated_at은 필수입니다.");
        }
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException ignored) {
            return Timestamp.valueOf(text).toInstant();
        }
    }

    private static String escape(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\' -> escaped.append("\\\\");
                case '"' -> escaped.append("\\\"");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (ch < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
                }
            }
        }
        return escaped.toString();
    }

    public record Snapshot(String json, int replayCount, Instant updatedAt, String hash) {
    }
}
