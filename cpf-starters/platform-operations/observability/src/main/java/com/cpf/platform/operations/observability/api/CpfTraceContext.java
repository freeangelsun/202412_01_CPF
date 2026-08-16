package com.cpf.platform.operations.observability.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Vendor-neutral CPF trace context with low-cardinality span naming and a fail-closed baggage policy.
 * OTel SDK types are deliberately not exposed from the public API.
 */
/** CpfTraceContext 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfTraceContext(
        String traceId,
        String spanId,
        String parentSpanId,
        String transactionId,
        String segmentId,
        int attempt,
        SpanKind kind,
        String spanName,
        Map<String, String> baggage,
        long policyVersion) {

    public static final long CURRENT_POLICY_VERSION = 1L;
    private static final int MAX_SPAN_NAME = 96;
    private static final int MAX_BAGGAGE_VALUE = 128;
    private static final int MAX_BAGGAGE_ITEMS = 16;
    private static final Set<String> BAGGAGE_ALLOWLIST = Set.of(
            "cpf.module", "cpf.execution", "cpf.channel", "cpf.tenant",
            "cpf.segment", "cpf.attempt", "cpf.correlation");
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "(?i)[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
    private static final Pattern LONG_NUMBER = Pattern.compile("(?<![A-Za-z])\\d{4,}(?![A-Za-z])");
    private static final Pattern SAFE_NAME = Pattern.compile("[^a-z0-9._:/{}-]");
    private static final Pattern SENSITIVE_VALUE = Pattern.compile(
            "(?i).*(authorization|bearer\\s+|password|passwd|secret|api[-_]?key|credential|session[-_]?id|cookie).*" );

    public CpfTraceContext {
        traceId = requireHex(traceId, 32, "traceId");
        spanId = requireHex(spanId, 16, "spanId");
        parentSpanId = optionalHex(parentSpanId, 16, "parentSpanId");
        transactionId = requiredText(transactionId, "transactionId", 200);
        segmentId = optionalText(segmentId, 100);
        if (attempt < 0 || attempt > 1_000_000) throw new IllegalArgumentException("attempt is out of range");
        kind = Objects.requireNonNull(kind, "kind");
        spanName = canonicalSpanName(kind, spanName);
        baggage = sanitizeBaggage(baggage);
        if (policyVersion < 1) throw new IllegalArgumentException("policyVersion must be positive");
    }

    /** root 작업을 CPF 표준 계약에 따라 수행한다. */
    public static CpfTraceContext root(
            String transactionId,
            SpanKind kind,
            String operation,
            Map<String, String> baggage) {
        String transaction = requiredText(transactionId, "transactionId", 200);
        String trace = digest(transaction).substring(0, 32);
        String span = digest(trace + "|ROOT|" + kind + '|' + operation).substring(0, 16);
        return new CpfTraceContext(trace, span, null, transaction, null, 0, kind,
                operation, baggage, CURRENT_POLICY_VERSION);
    }

    /** child 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfTraceContext child(
            SpanKind childKind,
            String operation,
            String childSegmentId,
            int childAttempt,
            Map<String, String> childBaggage) {
        String canonicalSegment = optionalText(childSegmentId, 100);
        String childSpan = digest(traceId + '|' + spanId + '|' + childKind + '|'
                + operation + '|' + text(canonicalSegment) + '|' + childAttempt).substring(0, 16);
        LinkedHashMap<String, String> merged = new LinkedHashMap<>(baggage);
        if (childBaggage != null) merged.putAll(childBaggage);
        if (canonicalSegment != null) merged.put("cpf.segment", canonicalSegment);
        merged.put("cpf.attempt", Integer.toString(childAttempt));
        return new CpfTraceContext(traceId, childSpan, spanId, transactionId,
                canonicalSegment, childAttempt, childKind, operation, merged, policyVersion);
    }

    /** attributes 작업을 CPF 표준 계약에 따라 수행한다. */
    public Map<String, String> attributes() {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        result.put("cpf.trace_id", traceId);
        result.put("cpf.span_id", spanId);
        if (parentSpanId != null) result.put("cpf.parent_span_id", parentSpanId);
        result.put("cpf.transaction_id", opaqueIdentifier(transactionId));
        if (segmentId != null) result.put("cpf.segment_id", boundedIdentifier(segmentId));
        result.put("cpf.attempt", Integer.toString(attempt));
        result.put("cpf.span_kind", kind.name());
        result.put("cpf.trace_policy_version", Long.toString(policyVersion));
        baggage.forEach((key, value) -> result.put("baggage." + key, value));
        return Map.copyOf(result);
    }

    /** SpanKind 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public enum SpanKind { LOCAL, REMOTE, MESSAGE, BATCH, FILE }

    public static String canonicalSpanName(SpanKind kind, String operation) {
        Objects.requireNonNull(kind, "kind");
        String candidate = requiredText(operation, "operation", 500).toLowerCase(Locale.ROOT);
        candidate = UUID_PATTERN.matcher(candidate).replaceAll("{id}");
        candidate = LONG_NUMBER.matcher(candidate).replaceAll("{id}");
        candidate = SAFE_NAME.matcher(candidate).replaceAll("_");
        candidate = candidate.replaceAll("_{2,}", "_").replaceAll("/{2,}", "/");
        String prefix = kind.name().toLowerCase(Locale.ROOT) + '.';
        String named = candidate.startsWith(prefix) ? candidate : prefix + candidate;
        return named.length() <= MAX_SPAN_NAME ? named : named.substring(0, MAX_SPAN_NAME);
    }

    private static Map<String, String> sanitizeBaggage(Map<String, String> source) {
        if (source == null || source.isEmpty()) return Map.of();
        if (source.size() > MAX_BAGGAGE_ITEMS) throw new IllegalArgumentException("too many baggage items");
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        source.forEach((rawKey, rawValue) -> {
            String key = rawKey == null ? null : rawKey.trim().toLowerCase(Locale.ROOT);
            if (!BAGGAGE_ALLOWLIST.contains(key)) {
                throw new IllegalArgumentException("baggage key is not allowed: " + rawKey);
            }
            String value = requiredText(rawValue, key, MAX_BAGGAGE_VALUE);
            if (SENSITIVE_VALUE.matcher(value).matches()) {
                throw new IllegalArgumentException("sensitive baggage value is forbidden: " + key);
            }
            result.put(key, boundedIdentifier(value));
        });
        return Map.copyOf(result);
    }


    private static String opaqueIdentifier(String value) {
        return "sha256:" + digest(value);
    }

    private static String boundedIdentifier(String value) {
        if (value.length() <= 64) return value;
        return value.substring(0, 32) + "#" + digest(value).substring(0, 16);
    }

    private static String requiredText(String value, String name, int max) {
        String normalized = optionalText(value, max);
        if (normalized == null) throw new IllegalArgumentException(name + " is required");
        return normalized;
    }

    private static String optionalText(String value, int max) {
        if (value == null) return null;
        String normalized = value.trim();
        if (normalized.isEmpty()) return null;
        if (normalized.length() > max) throw new IllegalArgumentException("value exceeds " + max);
        if (normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("control character is forbidden");
        }
        return normalized;
    }

    private static String requireHex(String value, int length, String name) {
        String normalized = optionalHex(value, length, name);
        if (normalized == null) throw new IllegalArgumentException(name + " is required");
        return normalized;
    }

    private static String optionalHex(String value, int length, String name) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() != length || !normalized.matches("[0-9a-f]+")) {
            throw new IllegalArgumentException(name + " must be " + length + " lowercase hex characters");
        }
        if (normalized.chars().allMatch(ch -> ch == '0')) {
            throw new IllegalArgumentException(name + " must not be all zero");
        }
        return normalized;
    }

    private static String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private static String text(String value) { return value == null ? "" : value; }
}
