package com.cpf.local.runtime;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 개발 전용 통합 Runtime의 Production 오사용, 원격 Bind,
 * 과도한 모듈 조립을 fail-closed로 차단합니다.
 */
public final class CpfLocalRuntimeSafetyGuard
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Set<String> FORBIDDEN =
            Set.of("prod", "production", "prd", "stg", "stage");

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        if (!environment.getProperty("cpf.local.runtime.enabled", Boolean.class, false)) {
            throw new IllegalStateException(
                    "cpf.local.runtime.enabled=true가 필요합니다.");
        }

        List<String> profiles = Arrays.stream(environment.getActiveProfiles())
                .map(profile -> profile.toLowerCase(Locale.ROOT))
                .toList();
        String environmentName = environment.getProperty("cpf.environment", "local")
                .toLowerCase(Locale.ROOT);
        boolean localProfile = profiles.stream()
                .anyMatch(profile -> profile.equals("local") || profile.startsWith("local-"));
        boolean forbiddenProfile = profiles.stream().anyMatch(FORBIDDEN::contains);
        if (!localProfile || forbiddenProfile || FORBIDDEN.contains(environmentName)) {
            throw new IllegalStateException(
                    "개발용 Web Runtime은 local Profile에서만 실행하며 "
                            + "Production/Stage 환경을 허용하지 않습니다.");
        }

        String address = environment.getProperty("server.address", "127.0.0.1").trim();
        boolean allowRemoteBind = environment.getProperty(
                "cpf.local.runtime.allow-remote-bind",
                Boolean.class,
                false);
        if (!allowRemoteBind && !Set.of("127.0.0.1", "localhost", "::1").contains(address)) {
            throw new IllegalStateException("원격 Bind는 기본 차단됩니다: " + address);
        }

        int port = environment.getProperty("server.port", Integer.class, 8080);
        if (port < 1 || port > 65_535) {
            throw new IllegalStateException("유효하지 않은 단일 Web Port입니다.");
        }

        long enabledModules = java.util.stream.Stream
                .of("core", "common", "gateway", "admin", "backoffice", "domains")
                .filter(module -> environment.getProperty(
                        "cpf.local.modules."
                                + module
                                + ("domains".equals(module) ? ".enabled" : ""),
                        Boolean.class,
                        !Set.of("backoffice", "domains").contains(module)))
                .count();
        int maxEnabledModules = environment.getProperty(
                "cpf.local.runtime.max-enabled-modules",
                Integer.class,
                6);
        if (enabledModules > maxEnabledModules) {
            throw new IllegalStateException("활성 모듈 수가 안전 상한을 초과했습니다.");
        }

        long minimumMaxMemoryMb = environment.getProperty(
                "cpf.local.runtime.minimum-max-memory-mb",
                Long.class,
                512L);
        long actualMaxMemoryMb = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        if (actualMaxMemoryMb < minimumMaxMemoryMb) {
            throw new IllegalStateException(
                    "Local Runtime max heap이 부족합니다. required="
                            + minimumMaxMemoryMb
                            + "MB, actual="
                            + actualMaxMemoryMb
                            + "MB");
        }
    }
}
