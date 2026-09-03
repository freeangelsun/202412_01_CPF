package com.cpf.starter.observability.runtimecontrol;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.security.api.CpfMaskingRuntime;
import com.cpf.platform.operations.runtimecontrol.spi.CpfRuntimePayloadReader;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** 로그/감사 출력에 사용되는 실제 민감정보 마스킹 정책을 원자 교체합니다. */
public final class CpfMaskingPolicyRuntimeApplier implements CpfRuntimeChangeApplier {
    public static final String CHANGE_TYPE = "MASKING_POLICY";

    @Override public String changeType() { return CHANGE_TYPE; }
    @Override public boolean supportsIdempotentReplay() { return true; }
    @Override public boolean snapshotCapable() { return true; }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        try {
            Object rawKeys = CpfRuntimePayloadReader.value(delivery.payload(), "sensitiveKeys");
            Set<String> keys = new LinkedHashSet<>();
            if (rawKeys instanceof List<?> list) {
                for (Object item : list) {
                    if (item != null && !String.valueOf(item).isBlank()) keys.add(String.valueOf(item));
                }
            }
            int maxLength = number(CpfRuntimePayloadReader.value(delivery.payload(), "maxLength"), 4000);
            boolean maskBearer = bool(
                    CpfRuntimePayloadReader.value(delivery.payload(), "maskBearerToken"),
                    true);
            // 운영자가 고른 값 규칙을 그대로 배포한다. 항목이 오지 않으면 현재 선택을 유지한다
            // (null 전달 = 현재 선택 유지). 규칙을 임의로 되살리지 않는다.
            Object rawRules = CpfRuntimePayloadReader.value(delivery.payload(), "valueRules");
            java.util.Set<com.cpf.security.api.CpfMaskingValueRule> valueRules = null;
            if (rawRules instanceof List<?> ruleList) {
                LinkedHashSet<com.cpf.security.api.CpfMaskingValueRule> selected = new LinkedHashSet<>();
                for (Object item : ruleList) {
                    if (item != null && !String.valueOf(item).isBlank()) {
                        selected.add(com.cpf.security.api.CpfMaskingValueRule.of(String.valueOf(item)));
                    }
                }
                valueRules = selected;
            }
            CpfMaskingRuntime.MaskingPolicy policy =
                    CpfMaskingRuntime.replacePolicy(keys, maxLength, maskBearer, valueRules);
            if (policy.maxLength() < 256 || !policy.sensitiveKeys().contains("password")) {
                return CpfRuntimeApplyResult.failure("MASKING_POLICY_NOT_CONFIRMED", "마스킹 정책 fail-safe 기본값이 적용되지 않았습니다.");
            }
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure("MASKING_POLICY_INVALID", "마스킹 정책 payload가 유효하지 않습니다.");
        }
    }

    private int number(Object value, int fallback) {
        if (value instanceof Number number) return number.intValue();
        if (value == null) return fallback;
        return Integer.parseInt(String.valueOf(value));
    }

    private boolean bool(Object value, boolean fallback) {
        if (value instanceof Boolean bool) return bool;
        if (value == null) return fallback;
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
