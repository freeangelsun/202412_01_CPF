package com.cpf.core.common.http;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CpfPinnedHttpConnectorFactoryContractTest {
    @Test
    void createsConnectorFromValidatedPinnedAddress() throws Exception {
        CpfServiceEndpointRegistry.ResolvedEndpoint endpoint = new CpfServiceEndpointRegistry.ResolvedEndpoint(
                "payment", "https://service.example", URI.create("https://service.example"),
                InetAddress.getByAddress(new byte[]{8, 8, 8, 8}), 443, "service.example", List.of("8.8.8.8"));
        assertNotNull(new CpfPinnedHttpConnectorFactory(1000, 2000).connector(endpoint));
    }
}
