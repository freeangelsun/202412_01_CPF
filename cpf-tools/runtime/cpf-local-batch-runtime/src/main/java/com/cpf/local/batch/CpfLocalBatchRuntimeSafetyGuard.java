package com.cpf.local.batch;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

/** 개발용 통합 Batch Runtime의 Production 오사용과 외부 Bind를 fail-closed로 차단합니다. */
public final class CpfLocalBatchRuntimeSafetyGuard
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Set<String> FORBIDDEN =
            Set.of("prod", "production", "prd", "stg", "stage");

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        validate(applicationContext.getEnvironment());
    }

    static void validate(Environment environment) {
        if (!environment.getProperty("cpf.local.batch.enabled", Boolean.class, false)) {
            throw new IllegalStateException(
                    "cpf-local-batch-runtime은 cpf.local.batch.enabled=true일 때만 실행할 수 있습니다.");
        }
        String configuredProfiles = environment.getProperty("spring.profiles.active", "");
        String[] activeProfiles = java.util.stream.Stream.concat(
                        Arrays.stream(environment.getActiveProfiles()),
                        Arrays.stream(configuredProfiles.split(",")))
                .map(String::trim)
                .filter(profile -> !profile.isBlank())
                .distinct()
                .toArray(String[]::new);
        boolean localProfile = Arrays.stream(activeProfiles)
                .map(profile -> profile.toLowerCase(Locale.ROOT))
                .anyMatch(profile -> profile.equals("local") || profile.startsWith("local-"));
        boolean forbiddenProfile = Arrays.stream(activeProfiles)
                .map(profile -> profile.toLowerCase(Locale.ROOT))
                .anyMatch(FORBIDDEN::contains);
        String environmentName = environment.getProperty("cpf.environment", "local")
                .trim().toLowerCase(Locale.ROOT);
        if (!localProfile || forbiddenProfile || FORBIDDEN.contains(environmentName)) {
            throw new IllegalStateException(
                    "개발용 Batch Runtime은 local Profile에서만 실행하며 Production/Stage 환경을 허용하지 않습니다.");
        }
        String address = environment.getProperty("server.address", "127.0.0.1").trim();
        boolean remote = environment.getProperty("cpf.local.batch.allow-remote-bind", Boolean.class, false);
        if (!remote && !Set.of("127.0.0.1", "localhost", "::1").contains(address)) {
            throw new IllegalStateException("개발용 Batch Runtime의 원격 Bind는 기본 차단됩니다.");
        }
    }
}
