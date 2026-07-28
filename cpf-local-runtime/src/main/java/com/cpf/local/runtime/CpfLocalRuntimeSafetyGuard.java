package com.cpf.local.runtime;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

/** 개발 전용 통합 Runtime의 Production 오사용을 fail-closed로 차단합니다. */
public final class CpfLocalRuntimeSafetyGuard
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Set<String> FORBIDDEN_PROFILES =
            Set.of("prod", "production", "prd", "stg", "stage");

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        if (!environment.getProperty("cpf.local.runtime.enabled", Boolean.class, false)) {
            throw new IllegalStateException(
                    "cpf-local-runtime은 cpf.local.runtime.enabled=true일 때만 실행할 수 있습니다.");
        }

        String[] activeProfiles = environment.getActiveProfiles();
        boolean localProfile = Arrays.stream(activeProfiles)
                .map(profile -> profile.toLowerCase(Locale.ROOT))
                .anyMatch(profile -> profile.equals("local") || profile.startsWith("local-"));
        boolean forbiddenProfile = Arrays.stream(activeProfiles)
                .map(profile -> profile.toLowerCase(Locale.ROOT))
                .anyMatch(FORBIDDEN_PROFILES::contains);
        String environmentName = environment.getProperty("cpf.environment", "local")
                .trim().toLowerCase(Locale.ROOT);
        if (!localProfile || forbiddenProfile || FORBIDDEN_PROFILES.contains(environmentName)) {
            throw new IllegalStateException(
                    "cpf-local-runtime은 local Profile에서만 실행하며 Production/Stage 환경을 허용하지 않습니다.");
        }

        boolean allowRemoteBind =
                environment.getProperty("cpf.local.runtime.allow-remote-bind", Boolean.class, false);
        String serverAddress = environment.getProperty("server.address", "127.0.0.1").trim();
        if (!allowRemoteBind && !Set.of("127.0.0.1", "localhost", "::1").contains(serverAddress)) {
            throw new IllegalStateException(
                    "개발 Runtime의 원격 Bind는 기본 차단됩니다. server.address=" + serverAddress);
        }
    }
}
