package com.cpf.starter.featureflag;

import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.EvaluationContext;

/** OpenFeature SDK를 Primary 평가 엔진으로 사용하는 CPF 경계입니다. */
public final class CpfFeatureFlagService {
    private final Client client;
    public CpfFeatureFlagService(Client client) { this.client = client; }
    public boolean booleanValue(String flagKey, boolean defaultValue, EvaluationContext context) {
        if (flagKey == null || !flagKey.matches("[A-Za-z0-9._-]{1,160}")) throw new IllegalArgumentException("Invalid feature flag key.");
        return client.getBooleanValue(flagKey, defaultValue, context);
    }
}
