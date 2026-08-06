package com.cpf.admin.approval.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Canonicalizes and hashes the complete immutable approval command envelope.
 * Payload values are never logged by this component.
 */
@Component
public final class AdmApprovalSnapshotIntegrity {
    private static final List<String> ENVELOPE_FIELDS = List.of(
            "actionType", "ownerModule", "ownerCommand", "targetType", "targetId",
            "requestKey", "requestedBy", "requestReason", "policyCode", "policyVersion",
            "expireAt", "transactionId");

    private final ObjectMapper objectMapper;

    public AdmApprovalSnapshotIntegrity(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    public String canonicalPayload(String source) {
        String json = source == null || source.isBlank() ? "{}" : source;
        try {
            Object parsed = objectMapper.readValue(json, new TypeReference<Object>() { });
            if (!(parsed instanceof Map<?, ?>)) {
                throw new IllegalArgumentException("payloadSnapshot must be a JSON object");
            }
            return canonicalJson(parsed);
        } catch (JsonProcessingException invalid) {
            throw new IllegalArgumentException("payloadSnapshot must be a JSON object", invalid);
        }
    }

    public String hash(Map<String, ?> request) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        for (String field : ENVELOPE_FIELDS) {
            envelope.put(field, normalizedScalar(request.get(field)));
        }
        String payload = canonicalPayload(Objects.toString(request.get("payloadSnapshot"), "{}"));
        try {
            Map<String, Object> payloadMap = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() { });
            Object topLevelExpectedVersion = request.get("expectedVersion");
            envelope.put("expectedVersion", topLevelExpectedVersion == null
                    ? expectedVersion(payloadMap)
                    : canonicalExpectedVersion(topLevelExpectedVersion));
            envelope.put("payload", payloadMap);
        } catch (JsonProcessingException impossible) {
            throw new IllegalStateException("canonical payload cannot be reparsed", impossible);
        }
        return sha256Canonical(canonicalJson(envelope));
    }

    public Verification verify(Map<String, ?> request) {
        String stored = Objects.toString(request.get("payloadHash"), "").trim();
        String calculated = hash(request);
        boolean wellFormed = stored.matches("[0-9a-fA-F]{64}");
        String safeStored = wellFormed ? stored.toLowerCase() : "INVALID";
        boolean valid = wellFormed
                && MessageDigest.isEqual(safeStored.getBytes(StandardCharsets.US_ASCII),
                calculated.getBytes(StandardCharsets.US_ASCII));
        return new Verification(valid, safeStored, calculated);
    }

    public boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) return false;
        return MessageDigest.isEqual(left.toLowerCase().getBytes(StandardCharsets.US_ASCII),
                right.toLowerCase().getBytes(StandardCharsets.US_ASCII));
    }

    private String canonicalJson(Object value) {
        StringBuilder out = new StringBuilder();
        appendCanonical(out, value);
        return out.toString();
    }

    @SuppressWarnings("unchecked")
    private void appendCanonical(StringBuilder out, Object value) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof Map<?, ?> map) {
            out.append('{');
            boolean first = true;
            TreeMap<String, Object> sorted = new TreeMap<>();
            map.forEach((key, item) -> {
                String normalizedKey = normalize(String.valueOf(key));
                if (sorted.containsKey(normalizedKey)) {
                    throw new IllegalArgumentException("duplicate JSON key after Unicode normalization");
                }
                sorted.put(normalizedKey, item);
            });
            for (Map.Entry<String, Object> entry : sorted.entrySet()) {
                if (!first) out.append(',');
                first = false;
                appendString(out, entry.getKey());
                out.append(':');
                appendCanonical(out, entry.getValue());
            }
            out.append('}');
        } else if (value instanceof Iterable<?> iterable) {
            out.append('[');
            boolean first = true;
            for (Object item : iterable) {
                if (!first) out.append(',');
                first = false;
                appendCanonical(out, item);
            }
            out.append(']');
        } else if (value.getClass().isArray()) {
            List<Object> items = new ArrayList<>();
            int length = java.lang.reflect.Array.getLength(value);
            for (int i = 0; i < length; i++) items.add(java.lang.reflect.Array.get(value, i));
            appendCanonical(out, items);
        } else if (value instanceof Number number) {
            BigDecimal decimal = new BigDecimal(number.toString()).stripTrailingZeros();
            out.append(decimal.scale() < 0 ? decimal.setScale(0).toPlainString() : decimal.toPlainString());
        } else if (value instanceof Boolean bool) {
            out.append(bool ? "true" : "false");
        } else if (value instanceof Instant instant) {
            appendString(out, canonicalInstant(instant).toString());
        } else {
            appendString(out, normalize(String.valueOf(value)));
        }
    }

    private void appendString(StringBuilder out, String value) {
        try {
            out.append(objectMapper.writeValueAsString(normalize(value)));
        } catch (JsonProcessingException failure) {
            throw new IllegalStateException("canonical JSON string failure", failure);
        }
    }

    private static Object normalizedScalar(Object value) {
        if (value == null) return null;
        if (value instanceof Number || value instanceof Boolean) return value;
        if (value instanceof Instant instant) return canonicalInstant(instant);
        if (value instanceof java.sql.Timestamp timestamp) return canonicalInstant(timestamp.toInstant());
        if (value instanceof java.util.Date date) return canonicalInstant(date.toInstant());
        return normalize(String.valueOf(value).trim());
    }

    private static Long expectedVersion(Map<String, Object> payload) {
        return canonicalExpectedVersion(payload.get("expectedVersion"));
    }

    private static Long canonicalExpectedVersion(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        try {
            BigDecimal decimal = value instanceof Number number
                    ? new BigDecimal(number.toString())
                    : new BigDecimal(String.valueOf(value).trim());
            return decimal.longValueExact();
        } catch (ArithmeticException | NumberFormatException invalid) {
            throw new IllegalArgumentException("expectedVersion must be an integer", invalid);
        }
    }

    private static Instant canonicalInstant(Instant value) {
        return value.truncatedTo(ChronoUnit.MILLIS);
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFC);
    }

    public String sha256Canonical(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception failure) {
            throw new IllegalStateException("SHA-256 unavailable", failure);
        }
    }

    public record Verification(boolean valid, String storedHash, String calculatedHash) { }
}
