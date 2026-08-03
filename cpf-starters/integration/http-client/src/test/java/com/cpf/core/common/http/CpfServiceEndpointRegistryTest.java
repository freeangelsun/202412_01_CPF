package com.cpf.core.common.http;

import com.cpf.core.api.security.network.CpfNetworkEndpointPolicy;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfServiceEndpointRegistryTest {
    @Test
    void dnsAddressIsValidatedAndReturnedAsTheActualConnectionPin() throws Exception {
        CpfServiceEndpointProperties properties = properties("https://service.example:8443", true, false, true,
                List.of("8.8.8.0/24"), List.of(8443), List.of("8.8.8.8"));
        CpfServiceEndpointRegistry registry = new CpfServiceEndpointRegistry(
                properties,
                CpfNetworkEndpointPolicy.secureDefault(),
                host -> new InetAddress[]{InetAddress.getByAddress(new byte[]{8, 8, 8, 8})});

        CpfServiceEndpointRegistry.ResolvedEndpoint endpoint = registry.resolvedEndpoint("PAYMENT");

        assertEquals("8.8.8.8", endpoint.pinnedAddress().getHostAddress());
        assertEquals(List.of("8.8.8.8"), endpoint.validatedAddresses());
        assertEquals("service.example:8443", endpoint.authority());
        assertEquals("https://service.example:8443", endpoint.baseUrl());
    }

    @Test
    void dnsRebindingToPrivateOrMixedAddressFailsBeforeConnectorCreation() throws Exception {
        CpfServiceEndpointProperties properties = properties("https://service.example", true, false, true,
                List.of(), List.of(443), List.of());
        CpfServiceEndpointRegistry privateRebind = new CpfServiceEndpointRegistry(
                properties, CpfNetworkEndpointPolicy.secureDefault(),
                host -> new InetAddress[]{InetAddress.getByAddress(new byte[]{10, 1, 2, 3})});
        assertThrows(IllegalArgumentException.class, () -> privateRebind.resolvedEndpoint("payment"));

        CpfServiceEndpointProperties mixedProperties = properties("https://service.example", true, true, true,
                List.of(), List.of(443), List.of());
        CpfServiceEndpointRegistry mixed = new CpfServiceEndpointRegistry(
                mixedProperties, CpfNetworkEndpointPolicy.secureDefault(),
                host -> new InetAddress[]{
                        InetAddress.getByAddress(new byte[]{10, 1, 2, 3}),
                        InetAddress.getByAddress(new byte[]{8, 8, 8, 8})});
        assertThrows(IllegalArgumentException.class, () -> mixed.resolvedEndpoint("payment"));
    }

    @Test
    void configuredPinMismatchFailsClosed() throws Exception {
        CpfServiceEndpointProperties properties = properties("https://service.example", true, false, true,
                List.of(), List.of(443), List.of("1.1.1.1"));
        CpfServiceEndpointRegistry registry = new CpfServiceEndpointRegistry(
                properties, CpfNetworkEndpointPolicy.secureDefault(),
                host -> new InetAddress[]{InetAddress.getByAddress(new byte[]{8, 8, 8, 8})});
        assertThrows(IllegalArgumentException.class, () -> registry.resolvedEndpoint("payment"));
    }

    @Test
    void runtimeEndpointIsReResolvedForEverySnapshotUseAndCannotReturnHostnameOnly() throws Exception {
        CpfServiceEndpointRegistry registry = new CpfServiceEndpointRegistry(
                new CpfServiceEndpointProperties(),
                CpfNetworkEndpointPolicy.secureDefault(),
                host -> new InetAddress[]{InetAddress.getByAddress(new byte[]{8, 8, 4, 4})});
        registry.replaceRuntime(1, Map.of("PAYMENT", new CpfServiceEndpointRegistry.RuntimeEndpoint(
                "PAYMENT", "HTTP", "https://service.example", "", "", "", 3000, true, false,
                Map.of("allowDns", "true", "allowedPorts", "443", "pinnedAddresses", "8.8.4.4"))));
        assertEquals("8.8.4.4", registry.resolvedEndpoint("payment").pinnedAddress().getHostAddress());
    }

    @Test
    void configuredServiceKeysAreNormalizedAndNormalizationDuplicatesFailClosed() throws Exception {
        CpfServiceEndpointProperties properties = properties("https://service.example", true, false, true,
                List.of(), List.of(443), List.of("8.8.8.8"));
        CpfServiceEndpointProperties.ServiceEndpoint endpoint = properties.getServices().remove("payment");
        properties.getServices().put("  PAYMENT  ", endpoint);
        CpfServiceEndpointRegistry registry = new CpfServiceEndpointRegistry(
                properties, CpfNetworkEndpointPolicy.secureDefault(),
                host -> new InetAddress[]{InetAddress.getByAddress(new byte[]{8, 8, 8, 8})});
        assertEquals("8.8.8.8", registry.resolvedEndpoint("payment").pinnedAddress().getHostAddress());

        properties.getServices().put("payment", endpoint);
        IllegalArgumentException duplicate = assertThrows(IllegalArgumentException.class,
                () -> new CpfServiceEndpointRegistry(properties, CpfNetworkEndpointPolicy.secureDefault()));
        assertTrue(duplicate.getMessage().contains("정규화 중복"));
    }

    private CpfServiceEndpointProperties properties(
            String url, boolean allowDns, boolean allowPrivate, boolean allowPublic,
            List<String> cidrs, List<Integer> ports, List<String> pins) {
        CpfServiceEndpointProperties.ServiceEndpoint endpoint = new CpfServiceEndpointProperties.ServiceEndpoint();
        endpoint.setBaseUrl(url);
        endpoint.setAllowDns(allowDns);
        endpoint.setAllowPrivate(allowPrivate);
        endpoint.setAllowPublic(allowPublic);
        endpoint.setAllowedCidrs(cidrs);
        endpoint.setAllowedPorts(ports);
        endpoint.setPinnedAddresses(pins);
        CpfServiceEndpointProperties properties = new CpfServiceEndpointProperties();
        Map<String, CpfServiceEndpointProperties.ServiceEndpoint> services = new LinkedHashMap<>();
        services.put("payment", endpoint);
        properties.setServices(services);
        return properties;
    }
}
