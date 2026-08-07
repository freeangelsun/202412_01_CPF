package com.cpf.admin.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.Set;

/** Prevents local/ephemeral operational providers from being activated by an omitted or unsafe profile. */
@Component
public final class AdmIntegrationClosureProfileGuard implements ApplicationRunner {
    private final Environment environment;
    public AdmIntegrationClosureProfileGuard(Environment environment) { this.environment = environment; }
    @Override public void run(ApplicationArguments args) {
        boolean enabled = environment.getProperty("cpf.adm.integration-closure.enabled", Boolean.class, false);
        boolean ephemeral = environment.getProperty("cpf.adm.integration-closure.ephemeral-providers-enabled", Boolean.class, false);
        Set<String> profiles = Set.of(environment.getActiveProfiles());
        if (enabled && profiles.isEmpty()) throw new IllegalStateException("integration-closure requires an explicit active profile");
        if (ephemeral && profiles.stream().noneMatch(Set.of("local", "dev")::contains))
            throw new IllegalStateException("ephemeral integration-closure providers are allowed only in local/dev profiles");
        boolean protectedProfile = profiles.stream().anyMatch(Set.of("prod", "stg")::contains);
        if (ephemeral && protectedProfile)
            throw new IllegalStateException("ephemeral integration-closure providers are forbidden in prod/stg");
        if (enabled && protectedProfile) {
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
