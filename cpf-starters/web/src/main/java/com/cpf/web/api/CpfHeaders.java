package com.cpf.web.api;

import com.cpf.web.context.CpfHttpHeaderCatalog;
import com.cpf.web.context.CpfHttpHeaderNames;
import com.cpf.web.context.CpfHeaderValidationException;
import com.cpf.core.api.error.CpfFrameworkErrorCode;
import com.cpf.core.api.transaction.CpfTransactionIds;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CPF Web Profile의 표준 HTTP Header 생성·조회·수정 Public API입니다.
 *
 * <p>업무 코드는 Header literal을 직접 만들지 않고 이 API를 사용합니다. 외부 최초 진입은
 * Framework가 Tx/Exec를 생성할 수 있지만, 내부 거래 경계에서는 Catalog의 필수 Header가
 * 빠지면 fail-fast합니다. 인증/Secret Header는 이 API에서 생성하지 않습니다.</p>
 */
public final class CpfHeaders {
    private static final int MAX_HEADER_LENGTH = 256;

    private CpfHeaders() {}

    public static String transactionId() { return CpfHttpHeaderNames.TRANSACTION_ID; }
    /** executionId는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
    public static String executionId() { return CpfHttpHeaderNames.EXECUTION_ID; }
    /** parentExecutionId는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
    public static String parentExecutionId() { return CpfHttpHeaderNames.PARENT_EXECUTION_ID; }
    /** segmentId는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
    public static String segmentId() { return CpfHttpHeaderNames.SEGMENT_ID; }
    /** parentSegmentId는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
    public static String parentSegmentId() { return CpfHttpHeaderNames.PARENT_SEGMENT_ID; }
    /** standardExecutionId는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
    public static String standardExecutionId() { return CpfHttpHeaderNames.STANDARD_EXECUTION_ID; }
    /** idempotencyKey는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
    public static String idempotencyKey() { return CpfHttpHeaderNames.IDEMPOTENCY_KEY; }
    /** correlationId는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
    public static String correlationId() { return CpfHttpHeaderNames.CORRELATION_ID; }
    /** caller는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
    public static String caller() { return CpfHttpHeaderNames.CALLER; }
    /** target는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
    public static String target() { return CpfHttpHeaderNames.TARGET; }
    /** channelCode는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
    public static String channelCode() { return CpfHttpHeaderNames.CHANNEL_CODE; }
    /** originalChannelCode는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
    public static String originalChannelCode() { return CpfHttpHeaderNames.ORIGINAL_CHANNEL_CODE; }
    /** userId는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
    public static String userId() { return CpfHttpHeaderNames.USER_ID; }
    /** operatorId는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
    public static String operatorId() { return CpfHttpHeaderNames.OPERATOR_ID; }
    /** tenantId는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
    public static String tenantId() { return CpfHttpHeaderNames.TENANT_ID; }
    /** traceparent는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
    public static String traceparent() { return CpfHttpHeaderNames.TRACEPARENT; }
    /** tracestate는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
    public static String tracestate() { return CpfHttpHeaderNames.TRACESTATE; }

    /** 개발자가 자주 확인하는 표준 Header 이름을 짧은 canonical 이름으로 반환합니다. */
    public static List<String> standardNames() {
        return List.of(transactionId(), executionId(), parentExecutionId(), segmentId(), parentSegmentId(),
                standardExecutionId(), idempotencyKey(), correlationId(), caller(), target(),
                originalChannelCode(), channelCode(), userId(), operatorId(), tenantId(), traceparent(), tracestate());
    }

    /** 새로운 immutable Header Map을 직관적으로 구성합니다. */
    public static Builder builder() { return new Builder(); }

    /** 기존 Map을 복사하여 안전하게 일부 표준 Header를 수정할 Builder를 생성합니다. */
    public static Builder from(Map<String,String> headers) { return new Builder(headers); }

    /** 대소문자를 무시하고 Header 값을 조회합니다. */
    public static String get(Map<String,String> headers, String name) {
        if (headers == null || name == null) return null;
        for (Map.Entry<String,String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) return entry.getValue();
        }
        return null;
    }

    /** 필수 Header 값을 조회하고 누락 시 표준 Header 오류로 fail-fast합니다. */
    public static String require(Map<String,String> headers, String name) {
        String value = get(headers, name);
        if (value == null || value.isBlank()) {
            throw new CpfHeaderValidationException(
                    CpfFrameworkErrorCode.MISSING_TRANSACTION_HEADER,
                    name,
                    "필수 CPF Header가 없습니다: " + name);
        }
        return safe(value, name);
    }

    /** 신뢰된 내부 서비스 호출에서 필요한 필수 Header 집합을 검증합니다. */
    public static void validateInternal(Map<String,String> headers) {
        for (String name : CpfHttpHeaderCatalog.REQUIRED_INTERNAL) require(headers, name);
    }

    /** transactionId 하나만 필요한 테스트/Adapter용 immutable Map을 만듭니다. */
    public static Map<String,String> transaction(String transactionId) {
        return builder().txId(transactionId).build();
    }

    /** transactionId와 선택 segment 계층을 immutable Header Map으로 만듭니다. */
    public static Map<String,String> transaction(String transactionId, String segmentId, String parentSegmentId) {
        return builder().txId(transactionId).segmentId(segmentId).parentSegmentId(parentSegmentId).build();
    }

    /**
     * 표준 Header 생성기입니다. 메소드명은 wire 이름보다 짧게 유지하되 의미를 잃지 않습니다.
     * {@code set}은 CPF 표준 Header의 비-Secret 값에만 사용합니다.
     */
    public static final class Builder {
        private final LinkedHashMap<String,String> values = new LinkedHashMap<>();

        private Builder() {}
        private Builder(Map<String,String> source) {
            if (source != null) source.forEach((k,v) -> { if (k != null && v != null) values.put(k, v); });
        }

        /** txId는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
        public Builder txId(String value) { return set(transactionId(), value); }
        /** execId는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
        public Builder execId(String value) { return set(executionId(), value); }
        /** parentExecId는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
        public Builder parentExecId(String value) { return set(parentExecutionId(), value); }
        /** segmentId는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
        public Builder segmentId(String value) { return set(CpfHeaders.segmentId(), value); }
        /** parentSegmentId는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
        public Builder parentSegmentId(String value) { return set(CpfHeaders.parentSegmentId(), value); }
        /** idemKey는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
        public Builder idemKey(String value) { return set(idempotencyKey(), value); }
        /** corrId는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
        public Builder corrId(String value) { return set(correlationId(), value); }
        /** caller는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
        public Builder caller(String value) { return set(CpfHeaders.caller(), value); }
        /** target는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
        public Builder target(String value) { return set(CpfHeaders.target(), value); }
        /** channel는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
        public Builder channel(String value) { return set(channelCode(), value); }
        /** tenant는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
        public Builder tenant(String value) { return set(tenantId(), value); }

        /** 값이 null/blank이면 해당 Header를 제거하고, 아니면 안전한 값으로 교체합니다. */
        public Builder set(String name, String value) {
            if (name == null || name.isBlank()) throw new IllegalArgumentException("header name은 필수입니다.");
            removeIgnoreCase(name);
            if (value != null && !value.isBlank()) values.put(name, safe(value, name));
            return this;
        }

        /** remove는 변경 가능한 확장 Header만 제거하고 신뢰 경계의 불변 Header는 보호합니다. */
        public Builder remove(String name) {
            removeIgnoreCase(name);
            return this;
        }

        /** 내부 서비스 호출용 필수값을 검증한 뒤 immutable Map을 반환합니다. */
        public Map<String,String> buildInternal() {
            Map<String,String> result = build();
            validateInternal(result);
            CpfTransactionIds.requireCanonical(require(result, transactionId()));
            return result;
        }

        /** 현재 Builder 값을 변경 불가능한 Header Map으로 확정합니다. */
        public Map<String,String> build() { return Collections.unmodifiableMap(new LinkedHashMap<>(values)); }

        private void removeIgnoreCase(String name) {
            String lower = name.toLowerCase(Locale.ROOT);
            values.keySet().removeIf(key -> key != null && key.toLowerCase(Locale.ROOT).equals(lower));
        }
    }

    private static String safe(String value, String name) {
        String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        if (normalized.length() > MAX_HEADER_LENGTH || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new CpfHeaderValidationException(
                    CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    name,
                    "안전하지 않은 CPF Header 값입니다: " + name);
        }
        return normalized;
    }
}
