package com.cpf.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;

class CpfGatewaySafetyPropertiesEntryTest {
    @Test
    void protocolAliasesAreNormalized() {
        CpfGatewaySafetyProperties properties = new CpfGatewaySafetyProperties();
        properties.setAllowedIngressProtocols(Set.of("http/1.1", "HTTP/2"));
        properties.validate();
        assertThat(properties.getAllowedIngressProtocols())
                .containsExactlyInAnyOrder("HTTP/1.1", "HTTP/2.0");
    }

    @Test
    void productionRequiresExplicitTlsDataPlaneAndDistributedCounter() {
        CpfGatewaySafetyProperties properties = new CpfGatewaySafetyProperties();
        properties.setEnvironmentCode("production");
        properties.setRequireDistributedRateLimitCounter(true);
        properties.setRateLimitCounterMode("JDBC");

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TLS Data Plane");

        properties.setDataPlanePort(8443);
        properties.setRequireTlsIngress(true);
        properties.validate();
    }

    @Test
    void unsupportedProtocolAndInvalidPortFailFast() {
        CpfGatewaySafetyProperties properties = new CpfGatewaySafetyProperties();
        properties.setAllowedIngressProtocols(Set.of("HTTP/3"));
        assertThatThrownBy(properties::validate)
                .hasMessageContaining("Unsupported ingress protocol");

        properties = new CpfGatewaySafetyProperties();
        properties.setDataPlanePort(70_000);
        CpfGatewaySafetyProperties invalid = properties;
        assertThatThrownBy(invalid::validate).hasMessageContaining("dataPlanePort");
    }
}
