package com.cpf.core.spi.featureflag;

import com.cpf.core.api.featureflag.CpfFeatureFlagContext;
import com.cpf.core.api.featureflag.CpfFeatureFlagResult;
import com.cpf.core.api.featureflag.CpfFeatureFlagValue;

/** Customer/OpenFeature adapter SPI without SDK types. */
public interface CpfFeatureFlagProvider {
    CpfFeatureFlagResult<CpfFeatureFlagValue> evaluate(
            String flagKey, CpfFeatureFlagValue fallback, CpfFeatureFlagContext context);
    long revision();
}
