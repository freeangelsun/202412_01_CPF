package com.cpf.core.common.runtimecontrol.applier;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.common.logging.SensitiveDataMasker;
import com.cpf.core.common.runtimecontrol.CpfRuntimePayloadJson;

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
            Object rawKeys = CpfRuntimePayloadJson.value(delivery.payload(), "sensitiveKeys");
            Set<String> keys = new LinkedHashSet<>();
            if (rawKeys instanceof List<?> list) {
                for (Object item : list) {
                    if (item != null && !String.valueOf(item).isBlank()) keys.add(String.valueOf(item));
                }
            }
            int maxLength = number(CpfRuntimePayloadJson.value(delivery.payload(), "maxLength"), 4000);
            boolean maskBearer = bool(
                    CpfRuntimePayloadJson.value(delivery.payload(), "maskBearerToken"),
                    true);
            SensitiveDataMasker.MaskingPolicy policy = SensitiveDataMasker.replacePolicy(keys, maxLength, maskBearer);
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
