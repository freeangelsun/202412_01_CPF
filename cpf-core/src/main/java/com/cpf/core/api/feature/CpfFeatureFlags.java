package com.cpf.core.api.feature;

import com.cpf.core.spi.feature.CpfFeatureFlagProvider;

/** 업무 코드가 특정 Feature Flag Vendor SDK에 직접 의존하지 않도록 하는 CPF 공개 Facade입니다. */
public class CpfFeatureFlags {
    private final CpfFeatureFlagProvider provider;

    public CpfFeatureFlags(CpfFeatureFlagProvider provider) {
        this.provider = provider;
    }

    public CpfFeatureFlagResult<Boolean> bool(
            String flagKey, CpfFeatureFlagContext context, boolean safeDefault) {
        return evaluateSafely(flagKey, Boolean.class, context, safeDefault);
    }

    public CpfFeatureFlagResult<String> string(
            String flagKey, CpfFeatureFlagContext context, String safeDefault) {
        return evaluateSafely(flagKey, String.class, context, safeDefault);
    }

    public CpfFeatureFlagResult<Double> number(
            String flagKey, CpfFeatureFlagContext context, double safeDefault) {
        return evaluateSafely(flagKey, Double.class, context, safeDefault);
    }

    public <T> CpfFeatureFlagResult<T> object(
            String flagKey, CpfFeatureFlagContext context, Class<T> valueType, T safeDefault) {
        return evaluateSafely(flagKey, valueType, context, safeDefault);
    }

    private <T> CpfFeatureFlagResult<T> evaluateSafely(
            String flagKey, Class<T> valueType, CpfFeatureFlagContext context, T safeDefault) {
        try {
            CpfFeatureFlagResult<T> result = provider.evaluate(flagKey, valueType, context, safeDefault);
            return result == null
                    ? new CpfFeatureFlagResult<>(safeDefault, "PROVIDER_NULL_RESULT", "cpf-facade", true)
                    : result;
        } catch (RuntimeException ex) {
            // Feature Flag control-plane/provider 장애가 원 업무를 오염시키지 않도록 caller safe default로 닫습니다.
            return new CpfFeatureFlagResult<>(safeDefault, "PROVIDER_FAILURE", "cpf-facade", true);
        }
    }
}
