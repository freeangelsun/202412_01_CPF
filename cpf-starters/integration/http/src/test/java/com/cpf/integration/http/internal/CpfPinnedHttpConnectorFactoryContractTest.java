package com.cpf.integration.http.internal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.InetAddress;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

class CpfPinnedHttpConnectorFactoryContractTest {
    @Test
    void createsConnectorFromValidatedPinnedAddress() throws Exception {
        var endpoint = new CpfServiceEndpointRegistry.ResolvedEndpoint(
                "bank", "https://api.example.com", URI.create("https://api.example.com"),
                InetAddress.getByName("8.8.8.8"), 443, "api.example.com", List.of("8.8.8.8"));
        assertNotNull(CpfPinnedHttpConnectorFactory.secureDefault().connector(endpoint));
    }
}
