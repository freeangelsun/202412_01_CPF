package com.cpf.integration.http.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cpf.security.api.network.CpfNetworkEndpointPolicy;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfServiceEndpointRegistryTest {
    @Test
    void dnsAddressIsValidatedAndReturnedAsTheActualConnectionPin() throws Exception {
        CpfServiceEndpointProperties properties = properties("https://api.example.com", List.of("8.8.8.8"));
        CpfNetworkEndpointPolicy policy = new CpfNetworkEndpointPolicy(
                List.of("8.8.8.0/24"), List.of(443), false, true, true, true);
        CpfServiceEndpointRegistry registry = new CpfServiceEndpointRegistry(
                properties, policy, host -> new InetAddress[]{InetAddress.getByName("8.8.8.8")});
        var resolved = registry.resolvedEndpoint("bank");
        assertEquals("8.8.8.8", resolved.pinnedAddress().getHostAddress());
        assertEquals("api.example.com", resolved.authority());
    }

    @Test
    void dnsRebindingToPrivateOrMixedAddressFailsBeforeConnectorCreation() throws Exception {
        CpfServiceEndpointProperties properties = properties("https://api.example.com", List.of());
        CpfNetworkEndpointPolicy policy = new CpfNetworkEndpointPolicy(
                List.of(), List.of(443), false, true, true, true);
        CpfServiceEndpointRegistry registry = new CpfServiceEndpointRegistry(
                properties, policy, host -> new InetAddress[]{
                        InetAddress.getByName("8.8.8.8"), InetAddress.getByName("10.0.0.3")});
        assertThrows(IllegalArgumentException.class, () -> registry.resolvedEndpoint("bank"));
    }

    @Test
    void configuredPinMismatchFailsClosed() throws Exception {
        CpfServiceEndpointProperties properties = properties("https://api.example.com", List.of("8.8.8.8"));
        CpfNetworkEndpointPolicy policy = new CpfNetworkEndpointPolicy(
                List.of(), List.of(443), false, true, true, true);
        CpfServiceEndpointRegistry registry = new CpfServiceEndpointRegistry(
                properties, policy, host -> new InetAddress[]{InetAddress.getByName("1.1.1.1")});
        assertThrows(IllegalArgumentException.class, () -> registry.resolvedEndpoint("bank"));
    }

    @Test
    void sameVersionReplayIsIdempotentButDifferentSnapshotIsRejected() {
        CpfServiceEndpointRegistry registry = new CpfServiceEndpointRegistry(null);
        var first = new CpfServiceEndpointRegistry.RuntimeEndpoint(
                "partner-a", "HTTP", "https://partner.example", "", "", "", 3_000,
                true, false, Map.of("allowDns", "true", "allowPublic", "true", "allowedPorts", "443"));
        var changed = new CpfServiceEndpointRegistry.RuntimeEndpoint(
                "partner-a", "HTTP", "https://other.example", "", "", "", 3_000,
                true, false, Map.of("allowDns", "true", "allowPublic", "true", "allowedPorts", "443"));

        var applied = registry.replaceRuntime(7L, Map.of("partner-a", first));
        var replayed = registry.replaceRuntime(7L, Map.of("partner-a", first));

        assertEquals(applied, replayed);
        assertThrows(IllegalArgumentException.class,
                () -> registry.replaceRuntime(7L, Map.of("partner-a", changed)));
        assertEquals("https://partner.example", registry.runtimeEndpoint("partner-a").baseUrl());
    }

    private static CpfServiceEndpointProperties properties(String url, List<String> pins) {
        CpfServiceEndpointProperties.ServiceEndpoint endpoint = new CpfServiceEndpointProperties.ServiceEndpoint();
        endpoint.setBaseUrl(url);
        endpoint.setAllowDns(true);
        endpoint.setAllowPublic(true);
        endpoint.setAllowedPorts(List.of(443));
        endpoint.setPinnedAddresses(pins);
        CpfServiceEndpointProperties properties = new CpfServiceEndpointProperties();
        properties.setServices(Map.of("bank", endpoint));
        return properties;
    }
}
