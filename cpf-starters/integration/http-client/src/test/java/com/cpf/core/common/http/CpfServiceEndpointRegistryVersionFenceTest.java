package com.cpf.core.common.http;

import com.cpf.core.api.security.network.CpfNetworkEndpointPolicy;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Regression protection for same-version stale-writer overwrite. */
class CpfServiceEndpointRegistryVersionFenceTest {
    @Test
    void sameVersionIsIdempotentOnlyForTheSameNormalizedSnapshot() throws Exception {
        CpfServiceEndpointRegistry registry = new CpfServiceEndpointRegistry(
                new CpfServiceEndpointProperties(),
                CpfNetworkEndpointPolicy.secureDefault(),
                host -> new InetAddress[]{InetAddress.getByAddress(new byte[]{8, 8, 8, 8})});
        CpfServiceEndpointRegistry.RuntimeEndpoint first = endpoint("https://one.example");
        CpfServiceEndpointRegistry.RuntimeEndpoint conflicting = endpoint("https://two.example");

        registry.replaceRuntime(7, Map.of("PAYMENT", first));
        assertEquals("https://one.example", registry.replaceRuntime(7, Map.of("payment", first))
                .endpoints().get("payment").baseUrl());
        assertThrows(IllegalArgumentException.class,
                () -> registry.replaceRuntime(7, Map.of("payment", conflicting)));
        assertEquals("https://one.example", registry.runtimeEndpoint("payment").baseUrl());
    }

    private CpfServiceEndpointRegistry.RuntimeEndpoint endpoint(String url) {
        return new CpfServiceEndpointRegistry.RuntimeEndpoint(
                "payment", "HTTP", url, "", "", "", 3000, true, false,
                Map.of("allowDns", "true", "allowPublic", "true", "allowedPorts", "443"));
    }
}
