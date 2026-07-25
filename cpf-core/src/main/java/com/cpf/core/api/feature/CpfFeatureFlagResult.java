package com.cpf.core.api.feature;

/** Flag 값과 평가 근거/Provider Version을 함께 전달합니다. */
public record CpfFeatureFlagResult<T>(T value, String reason, String providerVersion, boolean defaulted) {
}
