package com.cpf.gateway.control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CpfGatewayControlSecurityPropertiesListenerTest {
    @Test
    void disabledControlDoesNotRequireListenerOrSecret() {
        CpfGatewayControlSecurityProperties properties = new CpfGatewayControlSecurityProperties();
        properties.validate();
    }

    @Test
    void enabledControlRequiresDedicatedListenerAndStrongSecret() {
        CpfGatewayControlSecurityProperties properties = enabled();
        properties.setListenerPort(0);
        assertThatThrownBy(properties::validate).hasMessageContaining("listener-port");

        properties = enabled();
        properties.validate();
        assertThat(properties.getBindAddress()).isEqualTo("127.0.0.1");
    }

    @Test
    void tlsControlRequiresCompleteKeyStoreConfiguration() {
        CpfGatewayControlSecurityProperties properties = enabled();
        properties.setTlsEnabled(true);
        assertThatThrownBy(properties::validate).hasMessageContaining("key-store");

        properties.setKeyStore("/run/secrets/gateway-control.p12");
        properties.setKeyStorePassword("secret-reference-value");
        properties.validate();
    }

    private static CpfGatewayControlSecurityProperties enabled() {
        CpfGatewayControlSecurityProperties properties = new CpfGatewayControlSecurityProperties();
        properties.setEnabled(true);
        properties.setListenerPort(9070);
        properties.setSharedSecret("0123456789abcdef0123456789abcdef");
        return properties;
    }
}
