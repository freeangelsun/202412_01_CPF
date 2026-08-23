package com.cpf.gateway.control;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetAddress;
import org.apache.catalina.connector.Connector;
import org.junit.jupiter.api.Test;

class CpfGatewayControlListenerConfigurationTest {
    @Test
    void connectorUsesDedicatedAddressPortAndTlsProperties() {
        CpfGatewayControlSecurityProperties properties = new CpfGatewayControlSecurityProperties();
        properties.setEnabled(true);
        properties.setListenerPort(9070);
        properties.setBindAddress("127.0.0.1");
        properties.setSharedSecret("0123456789abcdef0123456789abcdef");
        properties.setTlsEnabled(true);
        properties.setKeyStore("/run/secrets/gateway-control.p12");
        properties.setKeyStorePassword("secret-reference-value");
        properties.validate();

        Connector connector = CpfGatewayControlListenerConfiguration.connector(properties);

        assertThat(connector.getPort()).isEqualTo(9070);
        assertThat(connector.getScheme()).isEqualTo("https");
        assertThat(connector.getSecure()).isTrue();
        assertThat(connector.getProperty("address")).isInstanceOf(InetAddress.class);
        assertThat(((InetAddress) connector.getProperty("address")).getHostAddress())
                .isEqualTo("127.0.0.1");
        assertThat(connector.getProperty("SSLEnabled")).isEqualTo(Boolean.TRUE);
    }
}
