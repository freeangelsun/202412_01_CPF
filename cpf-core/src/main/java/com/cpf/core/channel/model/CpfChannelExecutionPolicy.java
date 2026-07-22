package cpf.pfw.channel.model;

import cpf.pfw.common.execution.CpfStandardExecutionId;

import java.time.Instant;
import java.util.Locale;

/** 표준 실행별 최초 채널과 호출 채널의 허용 조건입니다. */
public record CpfChannelExecutionPolicy(
        String policyKey,
        String standardExecutionId,
        String originalChannelCode,
        String callerChannelCode,
        String requestType,
        boolean allowed,
        boolean authenticationRequired,
        boolean signatureRequired,
        int maxTps,
        Instant effectiveFrom,
        Instant effectiveTo,
        boolean active,
        long version) {

    public CpfChannelExecutionPolicy {
        policyKey = normalize(policyKey, "정책 키", "[A-Z][A-Z0-9_.-]{2,99}");
        standardExecutionId = normalizeExecutionId(standardExecutionId);
        originalChannelCode = normalizeChannel(originalChannelCode);
        callerChannelCode = normalizeChannel(callerChannelCode);
        requestType = normalize(requestType, "요청 유형", "[A-Z*][A-Z0-9_*]{0,29}");
        if (maxTps < 0) {
            throw new IllegalArgumentException("최대 TPS는 0 이상이어야 합니다.");
        }
        if (effectiveFrom != null && effectiveTo != null && effectiveFrom.isAfter(effectiveTo)) {
            throw new IllegalArgumentException("정책 적용 시작일시는 종료일시보다 늦을 수 없습니다.");
        }
        if (version < 0) {
            throw new IllegalArgumentException("정책 버전은 0 이상이어야 합니다.");
        }
    }

    public boolean isEffectiveAt(Instant instant) {
        Instant target = instant == null ? Instant.now() : instant;
        return active
                && (effectiveFrom == null || !target.isBefore(effectiveFrom))
                && (effectiveTo == null || !target.isAfter(effectiveTo));
    }

    private static String normalizeExecutionId(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!"*".equals(normalized) && !CpfStandardExecutionId.isValid(normalized)) {
            throw new IllegalArgumentException("표준 실행 ID 또는 * 형식이 올바르지 않습니다. value=" + value);
        }
        return normalized;
    }

    private static String normalizeChannel(String value) {
        return normalize(value, "채널 코드", "[A-Z][A-Z0-9_]{1,29}");
    }

    private static String normalize(String value, String fieldName, String pattern) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches(pattern)) {
            throw new IllegalArgumentException(fieldName + " 형식이 올바르지 않습니다. value=" + value);
        }
        return normalized;
    }
}
