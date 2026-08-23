package com.cpf.platform.operations.runtimecontrol;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.foundation.runtime.CpfRuntimeMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class CpfRuntimeControlAgentIdentityAutoConfigurationTest {
    private final CpfRuntimeControlAgentAutoConfiguration configuration =
            new CpfRuntimeControlAgentAutoConfiguration();

    @Test
    void defaultsToCanonicalSystemServiceAndApiEndpoint() {
        CpfRuntimeInstanceRegistration registration = configuration.cpfRuntimeInstanceRegistration(
                new CpfRuntimeMetadata(
                        "BAT", "cpf-batch-worker", "bat-worker-01", "localhost", "127.0.0.1"),
                new MockEnvironment().withProperty("spring.profiles.active", "local"));

        assertThat(registration.serviceId()).isEqualTo("BAT");
        assertThat(registration.endpointCode()).isEqualTo("BAT_API");
        assertThat(registration.applicationName()).isEqualTo("cpf-batch-worker");
        assertThat(registration.baseUrl()).isEqualTo("http://localhost:8080");
    }

    @Test
    void explicitRegistryIdentityOverridesRemainAuthoritative() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.profiles.active", "local")
                .withProperty("cpf.runtime.control.agent.service-id", "BAT-WORKER-DEDICATED")
                .withProperty("cpf.runtime.control.agent.endpoint-code", "BAT-WORKER-CONTROL")
                .withProperty("cpf.runtime.control.agent.runtime-base-url", "https://runtime.example.test/batch");

        CpfRuntimeInstanceRegistration registration = configuration.cpfRuntimeInstanceRegistration(
                new CpfRuntimeMetadata(
                        "BAT", "cpf-batch-worker", "bat-worker-01", "localhost", "127.0.0.1"),
                environment);

        assertThat(registration.serviceId()).isEqualTo("BAT-WORKER-DEDICATED");
        assertThat(registration.endpointCode()).isEqualTo("BAT-WORKER-CONTROL");
        assertThat(registration.baseUrl()).isEqualTo("https://runtime.example.test/batch");
    }

    @Test
    void wildcardBindAddressUsesRuntimeHostnameAndConfiguredPort() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("server.address", "0.0.0.0")
                .withProperty("server.port", "8282");

        String baseUrl = CpfRuntimeControlAgentAutoConfiguration.resolveRuntimeBaseUrl(
                new CpfRuntimeMetadata("BAT", "cpf-batch-worker", "bat-worker-01", "HOST-A", "10.0.0.8"),
                environment);

        assertThat(baseUrl).isEqualTo("http://HOST-A:8282");
    }
}
