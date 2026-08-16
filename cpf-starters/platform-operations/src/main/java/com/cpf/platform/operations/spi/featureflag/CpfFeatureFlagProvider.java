package com.cpf.platform.operations.spi.featureflag;

import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagContext;
import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagResult;
import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagValue;

/** Customer/OpenFeature adapter SPI without SDK types. */
/** CpfFeatureFlagProvider 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfFeatureFlagProvider {
    CpfFeatureFlagResult<CpfFeatureFlagValue> evaluate(
            String flagKey, CpfFeatureFlagValue fallback, CpfFeatureFlagContext context);
    long revision();
}
