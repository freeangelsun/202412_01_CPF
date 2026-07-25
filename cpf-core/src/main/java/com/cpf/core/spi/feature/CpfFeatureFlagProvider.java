package com.cpf.core.spi.feature;

import com.cpf.core.api.feature.CpfFeatureFlagContext;
import com.cpf.core.api.feature.CpfFeatureFlagResult;

/** OpenFeature Provider 등 외부 Flag Engine을 연결하는 CPF SPI입니다. */
public interface CpfFeatureFlagProvider {
    <T> CpfFeatureFlagResult<T> evaluate(
            String flagKey, Class<T> valueType, CpfFeatureFlagContext context, T safeDefault);
}
