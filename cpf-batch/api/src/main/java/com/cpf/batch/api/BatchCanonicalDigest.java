package com.cpf.batch.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Batch 승인·멱등성·원격전송에서 공동으로 사용하는 Canonical 직렬화와 SHA-256 계약입니다.
 *
 * <p>Java Object 직렬화나 Map iteration 순서에 의존하지 않으며, 허용 타입과 중첩/개수/크기를
 * 제한해 승인 시점과 실행 시점의 Digest가 항상 동일하도록 합니다.</p>
 */
public final class BatchCanonicalDigest {
    public static final int MAX_DEPTH = 8;
    public static final int MAX_VALUE_COUNT = 512;
    public static final int MAX_SERIALIZED_BYTES = 65_536;

    private BatchCanonicalDigest() {}

    /** Parameter를 검증하고 중첩 Collection까지 불변 구조로 복사합니다. */
    public static Map<String, Object> immutableParameters(Map<String, ?> source) {
        if (source == null || source.isEmpty()) return Map.of();
        Counter counter = new Counter();
        Object normalized = normalize(source, 0, counter);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) normalized;
        ensureSize(result);
        return result;
    }

    /** 허용 값 구조를 정규화한 Canonical Text입니다. 운영 로그에는 출력하지 않습니다. */
    public static String canonicalText(Object value) {
        StringBuilder canonical = new StringBuilder();
        append(canonical, normalize(value, 0, new Counter()), 0, new Counter());
        byte[] encoded = canonical.toString().getBytes(StandardCharsets.UTF_8);
        if (encoded.length > MAX_SERIALIZED_BYTES) {
            throw new IllegalArgumentException("Canonical value exceeds " + MAX_SERIALIZED_BYTES + " bytes.");
        }
        return canonical.toString();
    }

    /** 임의의 허용 값 구조를 Canonical SHA-256으로 계산합니다. */
    public static String sha256(Object value) {
        byte[] encoded = canonicalText(value).getBytes(StandardCharsets.UTF_8);
        if (encoded.length > MAX_SERIALIZED_BYTES) {
            throw new IllegalArgumentException("Canonical value exceeds " + MAX_SERIALIZED_BYTES + " bytes.");
        }
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(encoded));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable.", impossible);
        }
    }

    public static String planHash(
            String planId,
            long planVersion,
            BatchExecutionTopology topology,
            List<BatchStepDefinition> steps) {
        List<Object> canonicalSteps = new ArrayList<>(steps.size());
        for (BatchStepDefinition step : steps) {
            canonicalSteps.add(Map.of(
                    "stepId", step.stepId(),
                    "executorType", step.executorType().name(),
                    "executorReference", step.executorReference(),
                    "parameters", step.parameters(),
                    "partitionCount", step.partitionCount(),
                    "nextOnSuccess", step.nextOnSuccess(),
                    "nextOnFailure", step.nextOnFailure(),
                    "restartable", step.restartable()));
        }
        return sha256(Map.of(
                "planId", planId,
                "planVersion", planVersion,
                "topology", topology.name(),
                "steps", canonicalSteps));
    }

    public static String requestHash(BatchApprovedLaunchRequest request) {
        // 운영자·사유·fencing token은 감사/동시성 정보이며 승인된 업무 요청의 불변 payload가 아닙니다.
        return sha256(Map.of(
                "jobId", request.definition().jobId(),
                "definitionVersion", request.definition().definitionVersion(),
                "definitionChecksum", request.definition().checksum().toLowerCase(java.util.Locale.ROOT),
                "planChecksum", request.plan().checksum().toLowerCase(java.util.Locale.ROOT),
                "parameters", request.parameters(),
                "approvalId", request.approvalId()));
    }

    private static Object normalize(Object value, int depth, Counter counter) {
        if (depth > MAX_DEPTH) throw new IllegalArgumentException("Canonical value nesting exceeds " + MAX_DEPTH + ".");
        counter.increment();
        if (value == null || value instanceof String || value instanceof Boolean || value instanceof Scalar) return value;
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long
                || value instanceof BigInteger) return new Scalar('I', value.toString());
        if (value instanceof BigDecimal decimal) return new Scalar('D', decimal.stripTrailingZeros().toPlainString());
        if (value instanceof Float number) {
            if (!Float.isFinite(number)) throw new IllegalArgumentException("Non-finite numeric value is prohibited.");
            return new Scalar('D', BigDecimal.valueOf(number.doubleValue()).stripTrailingZeros().toPlainString());
        }
        if (value instanceof Double number) {
            if (!Double.isFinite(number)) throw new IllegalArgumentException("Non-finite numeric value is prohibited.");
            return new Scalar('D', BigDecimal.valueOf(number).stripTrailingZeros().toPlainString());
        }
        if (value instanceof UUID) return new Scalar('U', value.toString());
        if (value instanceof Instant || value instanceof LocalDate
                || value instanceof LocalDateTime || value instanceof OffsetDateTime) {
            return new Scalar('T', value.toString());
        }
        if (value instanceof Enum<?> enumeration) {
            return new Scalar('E', enumeration.getDeclaringClass().getName() + "#" + enumeration.name());
        }
        if (value instanceof Map<?, ?> map) {
            List<Map.Entry<String, Object>> entries = new ArrayList<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String key) || key.isBlank()) {
                    throw new IllegalArgumentException("Canonical map keys must be non-blank strings.");
                }
                entries.add(Map.entry(key, normalize(entry.getValue(), depth + 1, counter)));
            }
            entries.sort(Comparator.comparing(Map.Entry::getKey));
            Map<String, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : entries) {
                if (copy.put(entry.getKey(), entry.getValue()) != null) {
                    throw new IllegalArgumentException("Duplicate canonical map key: " + entry.getKey());
                }
            }
            return java.util.Collections.unmodifiableMap(copy);
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> copy = new ArrayList<>();
            for (Object item : iterable) copy.add(normalize(item, depth + 1, counter));
            return List.copyOf(copy);
        }
        if (value.getClass().isArray()) {
            if (!(value instanceof Object[] array)) {
                throw new IllegalArgumentException("Primitive arrays are prohibited in Batch parameters.");
            }
            List<Object> copy = new ArrayList<>(array.length);
            for (Object item : array) copy.add(normalize(item, depth + 1, counter));
            return List.copyOf(copy);
        }
        throw new IllegalArgumentException("Unsupported canonical value type: " + value.getClass().getName());
    }

    private static void append(StringBuilder out, Object value, int depth, Counter counter) {
        if (depth > MAX_DEPTH) throw new IllegalArgumentException("Canonical value nesting exceeds " + MAX_DEPTH + ".");
        counter.increment();
        if (value == null) {
            out.append('N');
        } else if (value instanceof Boolean bool) {
            out.append(bool ? "B1" : "B0");
        } else if (value instanceof String text) {
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            out.append('S').append(bytes.length).append(':').append(text);
        } else if (value instanceof Scalar scalar) {
            byte[] bytes = scalar.value().getBytes(StandardCharsets.UTF_8);
            out.append(scalar.kind()).append(bytes.length).append(':').append(scalar.value());
        } else if (value instanceof Map<?, ?> map) {
            out.append('M').append(map.size()).append('{');
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                append(out, entry.getKey(), depth + 1, counter);
                append(out, entry.getValue(), depth + 1, counter);
            }
            out.append('}');
        } else if (value instanceof Iterable<?> iterable) {
            List<?> items = iterable instanceof List<?> list ? list : toList(iterable);
            out.append('L').append(items.size()).append('[');
            for (Object item : items) append(out, item, depth + 1, counter);
            out.append(']');
        } else {
            throw new IllegalArgumentException("Value was not normalized: " + value.getClass().getName());
        }
        if (out.length() > MAX_SERIALIZED_BYTES) {
            throw new IllegalArgumentException("Canonical value exceeds " + MAX_SERIALIZED_BYTES + " characters.");
        }
    }

    private static List<Object> toList(Iterable<?> iterable) {
        List<Object> result = new ArrayList<>();
        for (Object value : iterable) result.add(value);
        return result;
    }

    private static void ensureSize(Object normalizedValue) {
        StringBuilder canonical = new StringBuilder();
        append(canonical, normalizedValue, 0, new Counter());
        if (canonical.toString().getBytes(StandardCharsets.UTF_8).length > MAX_SERIALIZED_BYTES) {
            throw new IllegalArgumentException("Canonical value exceeds " + MAX_SERIALIZED_BYTES + " bytes.");
        }
    }

    private record Scalar(char kind, String value) {}

    private static final class Counter {
        private int count;
        void increment() {
            if (++count > MAX_VALUE_COUNT) {
                throw new IllegalArgumentException("Canonical value count exceeds " + MAX_VALUE_COUNT + ".");
            }
        }
    }
}
