package com.cpf.core.service.feature;

import com.cpf.core.api.feature.CpfFeatureFlagContext;
import com.cpf.core.api.feature.CpfFeatureFlagResult;
import com.cpf.core.spi.feature.CpfFeatureFlagProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CPF 기본 Flag Provider입니다.
 *
 * <p>운영 Vendor Provider가 없을 때도 safe default와 deterministic percentage rollout을 제공합니다.
 * 설정이 없거나 해석 실패 시 반드시 호출자가 지정한 safeDefault를 사용합니다.</p>
 */
final class CpfPropertyFeatureFlagProvider implements CpfFeatureFlagProvider {
    private final Environment environment;
    private final ObjectMapper objectMapper;

    CpfPropertyFeatureFlagProvider(Environment environment, ObjectMapper objectMapper) {
        this.environment = environment;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> CpfFeatureFlagResult<T> evaluate(
            String flagKey, Class<T> valueType, CpfFeatureFlagContext context, T safeDefault) {
        String prefix = "cpf.feature-flags." + flagKey + ".";
        String enabledText = environment.getProperty(prefix + "enabled");
        if (enabledText == null) return new CpfFeatureFlagResult<>(safeDefault, "FLAG_UNDEFINED", "property-v1", true);
        if (!Boolean.parseBoolean(enabledText)) {
            return new CpfFeatureFlagResult<>(safeDefault, "KILL_SWITCH_OFF", "property-v1", true);
        }
        CpfFeatureFlagContext effectiveContext = context == null
                ? new CpfFeatureFlagContext(null, null, null, null, null, java.util.Map.of())
                : context;
        if (!allowed(prefix + "environments", effectiveContext.environment())
                || !allowed(prefix + "domains", effectiveContext.domain())
                || !allowed(prefix + "tenants", effectiveContext.tenant())
                || !allowed(prefix + "channels", effectiveContext.channel())
                || !allowed(prefix + "members", effectiveContext.memberKey())) {
            return new CpfFeatureFlagResult<>(safeDefault, "TARGET_EXCLUDED", "property-v1", true);
        }
        if (denied(prefix + "deny-environments", effectiveContext.environment())
                || denied(prefix + "deny-domains", effectiveContext.domain())
                || denied(prefix + "deny-tenants", effectiveContext.tenant())
                || denied(prefix + "deny-channels", effectiveContext.channel())
                || denied(prefix + "deny-members", effectiveContext.memberKey())) {
            return new CpfFeatureFlagResult<>(safeDefault, "TARGET_DENIED", "property-v1", true);
        }
        int percentage = clamp(environment.getProperty(prefix + "percentage", Integer.class, 100));
        if (bucket(flagKey, effectiveContext.stableTargetKey()) >= percentage) {
            return new CpfFeatureFlagResult<>(safeDefault, "PERCENTAGE_EXCLUDED", "property-v1", true);
        }
        String raw = environment.getProperty(prefix + "value");
        if (raw == null) return new CpfFeatureFlagResult<>(safeDefault, "VALUE_UNDEFINED", "property-v1", true);
        try {
            T parsed = parse(raw, valueType);
            return new CpfFeatureFlagResult<>(parsed, "TARGET_MATCH", "property-v1", false);
        } catch (RuntimeException ex) {
            return new CpfFeatureFlagResult<>(safeDefault, "PARSE_FAILED", "property-v1", true);
        }
    }

    private <T> T parse(String raw, Class<T> valueType) {
        if (valueType == String.class) return valueType.cast(raw);
        if (valueType == Boolean.class) return valueType.cast(Boolean.valueOf(raw));
        if (valueType == Double.class) return valueType.cast(Double.valueOf(raw));
        try {
            return objectMapper.readValue(raw, valueType);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Feature flag object value cannot be parsed", ex);
        }
    }

    private int bucket(String flagKey, String targetKey) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest((flagKey + ":" + targetKey).getBytes(StandardCharsets.UTF_8));
            int value = ((hash[0] & 0xff) << 8) | (hash[1] & 0xff);
            return value % 100;
        } catch (Exception ex) {
            return 99; // 해시 실패 시 rollout에서 제외하여 safe default로 닫습니다.
        }
    }

    private boolean allowed(String property, String actual) {
        Set<String> configured = csvSet(environment.getProperty(property));
        return configured.isEmpty() || (actual != null && configured.contains(actual));
    }

    private boolean denied(String property, String actual) {
        Set<String> configured = csvSet(environment.getProperty(property));
        return actual != null && configured.contains(actual);
    }

    private Set<String> csvSet(String value) {
        if (value == null || value.isBlank()) return Set.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    private int clamp(Integer value) {
        if (value == null) return 100;
        return Math.max(0, Math.min(100, value));
    }
}
