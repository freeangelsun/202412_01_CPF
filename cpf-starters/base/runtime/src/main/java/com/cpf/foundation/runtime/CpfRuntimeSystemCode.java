package com.cpf.foundation.runtime;

import java.util.Locale;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * CPF Runtime이 사용하는 Canonical systemCode를 한 곳에서 결정합니다.
 * Profile/application 이름은 systemCode 대체값이 아니며 업무 Domain/System 정본과 혼합하지 않습니다.
 */
public final class CpfRuntimeSystemCode {
    private CpfRuntimeSystemCode() {}

    /** Architecture Role 선언 property. 정본은 cpf-tools/governance/cpf-product-surface-policy.json 이다. */
    public static final String ARCHITECTURE_ROLE_PROPERTY = "cpf.architecture-role";

    /** SystemCode 를 가지지 않는 Architecture Role (Harness 30.16 표). */
    private static final java.util.Set<String> ROLES_WITHOUT_SYSTEM_CODE = java.util.Set.of(
            "PLATFORM_CONTROL_PLANE", "GATEWAY", "CHANNEL_FRONT",
            "BATCH_CONTROL_PLANE", "FRAMEWORK_INTERNAL", "TOPOLOGY");

    /**
     * Runtime 의 canonical systemCode 를 결정합니다. 없을 수 있습니다.
     *
     * <p>SystemCode 는 정본이 소유하는 고정값이며 Runtime 이 고르는 설정값이 아니다(Harness 30.20).
     * 따라서 Module ID / DB Prefix / Application 이름 / Package / Profile 을 fallback 으로 쓰지 않는다.
     * 이전 구현은 {@code cpf.framework.module-id} 와 {@code CPF_SYSTEM_CODE} 환경변수를 source 로
     * 사용했는데, 이는 Module Namespace 를 System Namespace 로 승격하는 위반이다.</p>
     *
     * <p>ADM(Platform Control Plane) / Gateway / Channel Front / Batch Control Plane / 1-WAS
     * topology 처럼 SystemCode 가 없는 Role 은 {@code null} 을 정상 반환한다. 가상 값을 만들지 않는다.
     * SystemCode 를 가져야 하는 Role 인데 정본 값이 없으면 fail-closed 한다.</p>
     *
     * @param environment Runtime Environment
     * @return canonical systemCode. SystemCode 를 가지지 않는 Role 이면 {@code null}
     */
    public static String resolve(Environment environment) {
        String canonical = canonicalValue(environment, "cpf.system-code", "cpf.generated-domain.system-code");
        String effective = first(environment, "cpf.system-code", "cpf.generated-domain.system-code");
        if (!hasText(canonical)) {
            if (hasText(effective)) {
                throw new IllegalStateException(
                        "CPF runtime systemCode must be declared by a canonical configuration source; "
                                + "an external override alone is not an identity source.");
            }
            if (roleWithoutSystemCode(environment)) return null;
            throw new IllegalStateException(
                    "CPF runtime systemCode is required for this architecture role."
                            + " Declare the canonical value in cpf.system-code (or"
                            + " cpf.generated-domain.system-code for a generated domain).");
        }
        String normalized = normalized(canonical);
        if (hasText(effective) && !normalized.equals(normalized(effective))) {
            throw new IllegalStateException(
                    "CPF runtime systemCode override does not match the canonical identity: canonical="
                            + normalized + " effective=" + normalized(effective));
        }
        if (!normalized.matches("[A-Z0-9][A-Z0-9_-]{1,31}")) {
            throw new IllegalStateException("Invalid CPF runtime systemCode: " + normalized);
        }
        if (roleWithoutSystemCode(environment)) {
            throw new IllegalStateException(
                    "This architecture role must not declare a systemCode: role="
                            + environment.getProperty(ARCHITECTURE_ROLE_PROPERTY) + " value=" + normalized);
        }
        return normalized;
    }

    /**
     * 정본 source만에서 SystemCode를 읽습니다. Spring Environment의 최상위 값은 OS 환경변수나
     * command-line property로 덮일 수 있으므로 identity source로 신뢰하지 않습니다.
     *
     * <p>외부 값은 {@link #resolve(Environment)}에서 이 값과 같을 때만 허용됩니다. PropertySource 이름은
     * Spring의 표준 external source 이름을 사용하며, 나머지 application/profile configuration source는
     * 정본 선언 후보입니다. 이 방식은 동일 literal의 profile별 정본 선언은 허용하지만 external source만으로
     * SystemCode를 만들어내는 동작은 fail-closed 합니다.</p>
     */
    private static String canonicalValue(Environment environment, String... keys) {
        if (!(environment instanceof ConfigurableEnvironment configurable)) {
            return first(environment, keys);
        }
        for (PropertySource<?> source : configurable.getPropertySources()) {
            if (externalSource(source)) continue;
            for (String key : keys) {
                Object value = source.getProperty(key);
                if (value != null && hasText(String.valueOf(value))) {
                    return String.valueOf(value).trim();
                }
            }
        }
        return null;
    }

    private static boolean externalSource(PropertySource<?> source) {
        String name = source == null ? "" : source.getName();
        return "systemProperties".equals(name)
                || "systemEnvironment".equals(name)
                || "commandLineArgs".equals(name)
                || "configurationProperties".equals(name);
    }

    private static String normalized(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean roleWithoutSystemCode(Environment environment) {
        String role = environment == null ? null : environment.getProperty(ARCHITECTURE_ROLE_PROPERTY);
        // hasText 를 거쳤다는 사실을 정적 분석이 알 수 없어 null 경고가 남는다. 판정을 직접 쓴다.
        if (role == null || role.isBlank()) return false;
        return ROLES_WITHOUT_SYSTEM_CODE.contains(role.trim().toUpperCase(Locale.ROOT));
    }

    private static String first(Environment environment, String... keys) {
        if (environment == null) return null;
        for (String key : keys) {
            String value = environment.getProperty(key);
            if (hasText(value)) return value;
        }
        return null;
    }

    private static boolean hasText(String value) { return value != null && !value.isBlank(); }
}
