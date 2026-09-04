package com.cpf.admin.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.Set;

/** Prevents mandatory ADM integration-closure composition from using local providers in an unsafe profile. */
@Component
/** AdmIntegrationClosureProfileGuard 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class AdmIntegrationClosureProfileGuard implements ApplicationRunner {
    private final Environment environment;
    public AdmIntegrationClosureProfileGuard(Environment environment) { this.environment = environment; }
    @Override public void run(ApplicationArguments args) {
        boolean ephemeral = environment.getProperty("cpf.adm.integration-closure.ephemeral-providers-enabled", Boolean.class, true);
        Set<String> profiles = Set.of(environment.getActiveProfiles());
        if (profiles.isEmpty()) throw new IllegalStateException("mandatory integration-closure requires an explicit active profile");
        boolean protectedProfile = profiles.stream().anyMatch(Set.of("prod", "stg")::contains);
        if (ephemeral && protectedProfile)
            throw new IllegalStateException("ephemeral integration-closure providers are forbidden in prod/stg");
        if (ephemeral && profiles.stream().noneMatch(Set.of("local", "dev")::contains))
            throw new IllegalStateException("ephemeral integration-closure providers are allowed only in local/dev profiles");
        if (protectedProfile) {
            rejectRawSecret("cpf.adm.integration-closure.approval-proof-key-base64");
            rejectRawSecret("cpf.adm.integration-closure.crypto.active-key-base64");
        }
    }
    private void rejectRawSecret(String propertyName) {
        String value = environment.getProperty(propertyName);
        if (value != null && !value.isBlank()) {
            throw new IllegalStateException(propertyName + " raw secret property is forbidden in prod/stg; configure a secret-ref provider");
        }
    }
}
