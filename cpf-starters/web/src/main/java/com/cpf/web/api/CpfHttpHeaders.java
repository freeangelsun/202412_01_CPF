package com.cpf.web.api;

import com.cpf.core.api.error.CpfFrameworkErrorCode;
import com.cpf.web.context.CpfHeaderValidationException;
import com.cpf.web.context.CpfHttpHeaderCatalog;
import com.cpf.web.context.CpfHttpHeaderNames;
import com.cpf.web.context.CpfHttpHeadersContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Public, case-insensitive CPF HTTP header API.
 *
 * <p>Unknown/custom headers are readable without catalog registration. Registration is only needed
 * for validation/propagation/masking policy. Generic mutation cannot overwrite CPF canonical or
 * trust-boundary headers; dedicated framework/runtime methods own those values.</p>
 */
/** 수신 HTTP Header를 대소문자 비구분으로 조회하고 Canonical 거래 Header 6개의 위조성 변경을 차단하는 Public API입니다. */
public final class CpfHttpHeaders {
    private static final int MAX_VALUE_LENGTH = 4096;
    private final LinkedHashMap<String, HeaderValues> values;

    private CpfHttpHeaders(LinkedHashMap<String, HeaderValues> values) {
        this.values = values;
    }

    /** 비어 있는 읽기 전용 CPF HTTP Header 집합을 생성합니다. */
    public static CpfHttpHeaders empty() { return new CpfHttpHeaders(new LinkedHashMap<>()); }

    /** Returns the actual headers captured at the current HTTP ingress, or {@code null} outside a web request. */
    /** 현재 요청 경계의 CPF HTTP Header 또는 Runtime Identity를 조회합니다. */
    public static CpfHttpHeaders current() { return CpfHttpHeadersContext.current(); }

    /** Returns the current request headers and fails clearly when called outside an HTTP request scope. */
    /** 현재 요청의 CPF HTTP Header를 필수로 조회하고 요청 경계가 없으면 명확히 실패합니다. */
    public static CpfHttpHeaders requireCurrent() {
        CpfHttpHeaders current = current();
        if (current == null) throw new IllegalStateException("CPF HTTP request context is not active");
        return current;
    }

    /** Captures all actual received values without requiring catalog registration. */
    /** 실제 수신된 Header의 다중 값을 보존하여 대소문자 비구분 CPF Header 뷰로 캡처합니다. */
    public static CpfHttpHeaders capture(Map<String, ? extends Collection<String>> source) {
        LinkedHashMap<String, HeaderValues> result = new LinkedHashMap<>();
        if (source != null) {
            source.forEach((name, list) -> {
                if (name == null || name.isBlank() || list == null) return;
                ArrayList<String> safe = new ArrayList<>();
                for (String value : list) if (value != null) safe.add(safeValue(value, name));
                if (!safe.isEmpty()) putCaseInsensitive(result, name, List.copyOf(safe));
            });
        }
        return new CpfHttpHeaders(result);
    }

    /** Captures a single-value map, mainly for adapters/tests. */
    /** 단일 값 Header Map을 CPF HTTP Header 뷰로 안전하게 캡처합니다. */
    public static CpfHttpHeaders captureSingle(Map<String,String> source) {
        LinkedHashMap<String, Collection<String>> converted = new LinkedHashMap<>();
        if (source != null) source.forEach((k,v) -> { if (k != null && v != null) converted.put(k, List.of(v)); });
        return capture(converted);
    }

    public String get(String name) {
        List<String> all = getAll(name);
        return all.isEmpty() ? null : all.getFirst();
    }

    public String getRequired(String name) {
        String value = get(name);
        if (value == null || value.isBlank()) throw missing(name);
        return value;
    }

    public <T> T get(String name, Class<T> type) {
        String value = get(name);
        return value == null ? null : convert(name, value, type);
    }

    public <T> T getRequired(String name, Class<T> type) {
        return convert(name, getRequired(name), type);
    }

    /** Header 이름을 대소문자와 무관하게 포함하는지 확인합니다. */
    public boolean contains(String name) { return find(name) != null; }
    /** Map 호환 방식으로 Header 존재 여부를 확인합니다. */
    public boolean containsKey(String name) { return contains(name); }
    public String get(String name, String defaultValue) { String value = get(name); return value == null ? defaultValue : value; }
    public String getFirst(String name) { return get(name); }
    public List<String> getValues(String name) { return getAll(name); }

    public List<String> getAll(String name) {
        HeaderValues found = find(name);
        return found == null ? List.of() : found.values();
    }

    /** First-value read-only view for common business code. */
    /** 업무 코드가 읽기 쉽게 첫 번째 값 기준의 변경 불가 Map을 반환합니다. */
    public Map<String,String> asMap() {
        LinkedHashMap<String,String> result = new LinkedHashMap<>();
        values.values().forEach(v -> result.put(v.name(), v.values().getFirst()));
        return Collections.unmodifiableMap(result);
    }

    /** 현재 캡처된 Header 이름의 변경 불가 집합을 반환합니다. */
    public Set<String> names() {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        values.values().forEach(v -> result.add(v.name()));
        return Collections.unmodifiableSet(result);
    }

    // Canonical transaction header names.
    /** Canonical X-Transaction-Id Header 이름을 반환합니다. */
    public static String transactionId() { return CpfHttpHeaderNames.TRANSACTION_ID; }
    /** Canonical X-Original-System-Code Header 이름을 반환합니다. */
    public static String originalSystemCode() { return CpfHttpHeaderNames.ORIGINAL_SYSTEM_CODE; }
    /** Canonical X-System-Code Header 이름을 반환합니다. */
    public static String systemCode() { return CpfHttpHeaderNames.SYSTEM_CODE; }
    /** Canonical X-Caller-System-Code Header 이름을 반환합니다. */
    public static String callerSystemCode() { return CpfHttpHeaderNames.CALLER_SYSTEM_CODE; }
    /** Canonical X-Target-System-Code Header 이름을 반환합니다. */
    public static String targetSystemCode() { return CpfHttpHeaderNames.TARGET_SYSTEM_CODE; }
    /** Canonical X-Target-Operation-Id Header 이름을 반환합니다. */
    public static String targetOperationId() { return CpfHttpHeaderNames.TARGET_OPERATION_ID; }
    /** Optional X-Original-Channel Header 이름을 반환합니다. */
    public static String originalChannel() { return CpfHttpHeaderNames.ORIGINAL_CHANNEL; }
    /** Optional X-Current-Channel Header 이름을 반환합니다. */
    public static String currentChannel() { return CpfHttpHeaderNames.CURRENT_CHANNEL; }
    /** Optional X-Caller-Channel Header 이름을 반환합니다. */
    public static String callerChannel() { return CpfHttpHeaderNames.CALLER_CHANNEL; }
    /** Optional X-Target-Channel Header 이름을 반환합니다. */
    public static String targetChannel() { return CpfHttpHeaderNames.TARGET_CHANNEL; }

    /** 선택 Client Context의 Country Code Header 이름을 반환합니다. */
    public static String countryCode() { return CpfHttpHeaderNames.COUNTRY_CODE; }
    /** 선택 Client Context의 Client ID Header 이름을 반환합니다. */
    public static String clientId() { return CpfHttpHeaderNames.CLIENT_ID; }
    /** 선택 Client Context의 Client Instance ID Header 이름을 반환합니다. */
    public static String clientInstanceId() { return CpfHttpHeaderNames.CLIENT_INSTANCE_ID; }
    /** 선택 Client Context의 Client Version Header 이름을 반환합니다. */
    public static String clientVersion() { return CpfHttpHeaderNames.CLIENT_VERSION; }
    /** 선택 Client Context의 Device ID Header 이름을 반환합니다. */
    public static String deviceId() { return CpfHttpHeaderNames.DEVICE_ID; }
    /** 선택 Client Context의 Idempotency Key Header 이름을 반환합니다. */
    public static String idempotencyKey() { return CpfHttpHeaderNames.IDEMPOTENCY_KEY; }
    /** 운영자 식별 Header 이름을 반환합니다. */
    public static String operatorId() { return CpfHttpHeaderNames.OPERATOR_ID; }
    /** W3C traceparent Header 이름을 반환합니다. */
    public static String traceparent() { return CpfHttpHeaderNames.TRACEPARENT; }
    /** W3C tracestate Header 이름을 반환합니다. */
    public static String tracestate() { return CpfHttpHeaderNames.TRACESTATE; }

    /** CPF Canonical 거래 Header 6개의 이름을 불변 목록으로 반환합니다. */
    public static List<String> standardNames() { return List.copyOf(CpfHttpHeaderCatalog.CANONICAL_TRANSACTION); }

    /** Backward-compatible map lookup helper. */
    public static String get(Map<String,String> headers, String name) { return captureSingle(headers).get(name); }
    /** 지정 Header 값을 필수로 조회하고 누락 시 표준 오류로 실패합니다. */
    public static String require(Map<String,String> headers, String name) { return captureSingle(headers).getRequired(name); }

    /** 원격 내부 Domain 호출에 필요한 Canonical 거래 Header 6개를 검증합니다. */
    public static void validateInternal(Map<String,String> headers) {
        CpfHttpHeaders captured = captureSingle(headers);
        for (String name : CpfHttpHeaderCatalog.REQUIRED_INTERNAL) captured.getRequired(name);
    }

    /** 보호 Header 위조를 차단하는 Custom Header Builder를 생성합니다. */
    public static Builder builder() { return new Builder(); }
    /** 기존 단일 값 Header Map을 기반으로 안전한 Builder를 생성합니다. */
    public static Builder from(Map<String,String> headers) { return new Builder(headers); }

    /** 업무 Custom Header만 안전하게 추가·삭제하고 CPF 보호 Header는 변경하지 못하게 하는 Builder입니다. */
    public static final class Builder {
        private final LinkedHashMap<String,List<String>> values = new LinkedHashMap<>();
        private Builder() {}
        private Builder(Map<String,String> source) {
            if (source != null) source.forEach((k,v) -> { if (k != null && v != null) setInternal(k, v); });
        }


        /** Generic custom-header mutation. Canonical/trust-boundary headers cannot be forged here. */
        public Builder set(String name, String value) {
            assertMutableCustom(name);
            removeIgnoreCase(name);
            if (value != null && !value.isBlank()) setInternal(name, value);
            return this;
        }

        /** 허용된 Custom Header에 값을 추가하며 Canonical 보호 Header 변경은 차단합니다. */
        public Builder add(String name, String value) {
            assertMutableCustom(name);
            if (value == null || value.isBlank()) return this;
            String existing = matchingKey(name);
            String key = existing == null ? name : existing;
            ArrayList<String> merged = new ArrayList<>(values.getOrDefault(key, List.of()));
            merged.add(safeValue(value, name));
            values.put(key, List.copyOf(merged));
            return this;
        }

        /** 허용된 Custom Header를 제거하며 Canonical 보호 Header 삭제는 차단합니다. */
        public Builder remove(String name) {
            requireName(name);
            if (CpfHttpHeaderCatalog.isProtected(name)) {
                throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA, name,
                        "CPF 보호 Header는 일반 Custom Header API로 제거할 수 없습니다: " + name,
                        403, "PROTECTED_HEADER_MUTATION");
            }
            removeIgnoreCase(name);
            return this;
        }

        /** 설정된 값으로 변경 불가 호출 옵션을 생성합니다. */
        public Map<String,String> build() {
            LinkedHashMap<String,String> result = new LinkedHashMap<>();
            values.forEach((name, list) -> { if (!list.isEmpty()) result.put(name, list.getFirst()); });
            return Collections.unmodifiableMap(result);
        }

        /** 다중 값을 보존하는 변경 불가 Header Map을 생성합니다. */
        public Map<String,List<String>> buildMultiValue() {
            LinkedHashMap<String,List<String>> result = new LinkedHashMap<>();
            values.forEach((name, list) -> result.put(name, List.copyOf(list)));
            return Collections.unmodifiableMap(result);
        }

        private void setInternal(String name, String value) { values.put(name, List.of(safeValue(value, name))); }
        private void removeIgnoreCase(String name) {
            String lower = name.toLowerCase(Locale.ROOT);
            values.keySet().removeIf(k -> k != null && k.toLowerCase(Locale.ROOT).equals(lower));
        }
        private String matchingKey(String name) {
            String lower = name.toLowerCase(Locale.ROOT);
            for (String key : values.keySet()) if (key.toLowerCase(Locale.ROOT).equals(lower)) return key;
            return null;
        }
        private void assertMutableCustom(String name) {
            requireName(name);
            if (CpfHttpHeaderCatalog.isProtected(name)) {
                throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA, name,
                        "CPF 보호 Header는 일반 Custom Header API로 변경할 수 없습니다: " + name,
                        403, "PROTECTED_HEADER_MUTATION");
            }
        }
    }

    private HeaderValues find(String name) {
        if (name == null) return null;
        return values.get(name.toLowerCase(Locale.ROOT));
    }

    private static void putCaseInsensitive(Map<String,HeaderValues> target, String name, List<String> values) {
        String key = name.toLowerCase(Locale.ROOT);
        HeaderValues previous = target.get(key);
        if (previous == null) target.put(key, new HeaderValues(name, values));
        else {
            ArrayList<String> merged = new ArrayList<>(previous.values());
            merged.addAll(values);
            target.put(key, new HeaderValues(previous.name(), List.copyOf(merged)));
        }
    }

    private static void requireName(String name) {
        if (name == null || name.isBlank() || name.length() > 256 || name.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("invalid header name");
        }
    }

    private static String safeValue(String value, String name) {
        if (value == null) throw new IllegalArgumentException(name + " must not be null");
        String normalized = value.trim();
        if (normalized.length() > MAX_VALUE_LENGTH || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA, name,
                    "안전하지 않은 Header 값입니다: " + name, 400, "HEADER_INVALID");
        }
        return normalized;
    }

    @SuppressWarnings("unchecked")
    private static <T> T convert(String name, String value, Class<T> type) {
        if (type == null) throw new IllegalArgumentException("type is required");
        try {
            Object converted;
            if (type == String.class) converted = value;
            else if (type == UUID.class) converted = UUID.fromString(value);
            else if (type == Integer.class || type == int.class) converted = Integer.valueOf(value);
            else if (type == Long.class || type == long.class) converted = Long.valueOf(value);
            else if (type == LocalDate.class) converted = LocalDate.parse(value);
            else if (type == LocalDateTime.class) converted = LocalDateTime.parse(value);
            else if (type == OffsetDateTime.class) converted = OffsetDateTime.parse(value);
            else if (type == Instant.class) converted = Instant.parse(value);
            else if (type == Boolean.class || type == boolean.class) {
                if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) throw new IllegalArgumentException();
                converted = Boolean.valueOf(value);
            } else if (Enum.class.isAssignableFrom(type)) {
                converted = Enum.valueOf((Class<? extends Enum>) type.asSubclass(Enum.class), value);
            } else throw new IllegalArgumentException("지원하지 않는 Header 변환 타입입니다: " + type.getName());
            return (T) converted;
        // typed Header 변환 실패는 원문 값을 묵살하지 않고 표준 Header 검증 오류로 변환합니다.
        } catch (RuntimeException ex) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA, name,
                    "Header 타입 변환에 실패했습니다: " + name, 400, "HEADER_TYPE_INVALID");
        }
    }

    private static CpfHeaderValidationException missing(String name) {
        return new CpfHeaderValidationException(CpfFrameworkErrorCode.MISSING_TRANSACTION_HEADER, name,
                "필수 Header가 없습니다: " + name, 400, "HEADER_REQUIRED");
    }

    private record HeaderValues(String name, List<String> values) {}
}
