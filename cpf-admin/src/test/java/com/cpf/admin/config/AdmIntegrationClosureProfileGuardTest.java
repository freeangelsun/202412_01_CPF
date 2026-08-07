package com.cpf.admin.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

class AdmIntegrationClosureProfileGuardTest {
    private static void run(MockEnvironment environment) throws Exception {
        new AdmIntegrationClosureProfileGuard(environment).run(new DefaultApplicationArguments(new String[0]));
    }
    @Test void enabledFeatureRequiresExplicitProfile() {
        MockEnvironment environment=new MockEnvironment().withProperty("cpf.adm.integration-closure.enabled","true");
        assertThrows(IllegalStateException.class,()->run(environment));
    }
    @Test void ephemeralProviderIsForbiddenInProduction() {
        MockEnvironment environment=new MockEnvironment().withProperty("cpf.adm.integration-closure.enabled","true")
                .withProperty("cpf.adm.integration-closure.ephemeral-providers-enabled","true");
        environment.setActiveProfiles("prod");
        assertThrows(IllegalStateException.class,()->run(environment));
    }
    @Test void localExplicitProfileAllowsEphemeralProvider() {
        MockEnvironment environment=new MockEnvironment().withProperty("cpf.adm.integration-closure.enabled","true")
                .withProperty("cpf.adm.integration-closure.ephemeral-providers-enabled","true");
        environment.setActiveProfiles("local"); assertDoesNotThrow(()->run(environment));
    }
}
