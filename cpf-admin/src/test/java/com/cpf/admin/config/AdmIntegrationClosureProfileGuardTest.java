package com.cpf.admin.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

class AdmIntegrationClosureProfileGuardTest {
    private static void run(MockEnvironment environment) throws Exception {
        new AdmIntegrationClosureProfileGuard(environment).run(new DefaultApplicationArguments(new String[0]));
    }
    @Test void mandatoryFeatureRequiresExplicitProfile() {
        MockEnvironment environment=new MockEnvironment();
        assertThrows(IllegalStateException.class,()->run(environment));
    }
    @Test void ephemeralProviderIsForbiddenInProduction() {
        MockEnvironment environment=new MockEnvironment()
                .withProperty("cpf.adm.integration-closure.ephemeral-providers-enabled","true");
        environment.setActiveProfiles("prod");
        assertThrows(IllegalStateException.class,()->run(environment));
    }
    @Test void protectedProfileAllowsOnlyExplicitlyDisabledEphemeralProvider() {
        MockEnvironment environment=new MockEnvironment()
                .withProperty("cpf.adm.integration-closure.ephemeral-providers-enabled","false");
        environment.setActiveProfiles("prod");
        assertDoesNotThrow(()->run(environment));
    }
    @Test void localProfileAllowsDefaultEphemeralProvider() {
        MockEnvironment environment=new MockEnvironment();
        environment.setActiveProfiles("local"); assertDoesNotThrow(()->run(environment));
    }
    @Test void rawApprovalProofSecretIsForbiddenInProduction() {
        MockEnvironment environment=new MockEnvironment()
                .withProperty("cpf.adm.integration-closure.ephemeral-providers-enabled","false")
                .withProperty("cpf.adm.integration-closure.approval-proof-key-base64","secret");
        environment.setActiveProfiles("prod");
        assertThrows(IllegalStateException.class,()->run(environment));
    }
    @Test void rawCryptoSecretIsForbiddenInStaging() {
        MockEnvironment environment=new MockEnvironment()
                .withProperty("cpf.adm.integration-closure.ephemeral-providers-enabled","false")
                .withProperty("cpf.adm.integration-closure.crypto.active-key-base64","secret");
        environment.setActiveProfiles("stg");
        assertThrows(IllegalStateException.class,()->run(environment));
    }
}
