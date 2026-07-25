package com.cpf.core.api.feature;

import java.util.Map;

/** Vendor-neutral feature flag evaluation context. OpenFeature targeting context로 손실 없이 매핑 가능한 최소 계약입니다. */
public record CpfFeatureFlagContext(
        String environment,
        String domain,
        String tenant,
        String channel,
        String memberKey,
        Map<String, String> attributes) {

    public CpfFeatureFlagContext {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public String stableTargetKey() {
        if (memberKey != null && !memberKey.isBlank()) return memberKey;
        if (tenant != null && !tenant.isBlank()) return tenant;
        if (domain != null && !domain.isBlank()) return domain;
        return "anonymous";
    }
}
