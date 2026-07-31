package com.cpf.batch.worker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.InetAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BatchOutboundHttpPolicyTest {
    @Test
    void externalProtocolIsDisabledByDefault() {
        WorkerOperationalProperties.OutboundHttp properties = new WorkerOperationalProperties.OutboundHttp();
        BatchOutboundHttpPolicy policy = new BatchOutboundHttpPolicy(properties,
                host -> List.of(InetAddress.getByName("203.0.113.10")));
        assertThrows(SecurityException.class, () -> policy.approve(URI.create("https://api.example.test/run"), 1));
    }

    @Test
    void approvedHostPortAndPinProduceConnectionIdentity() throws Exception {
        WorkerOperationalProperties.OutboundHttp properties = enabled();
        properties.setHostPins(Map.of("api.example.test", List.of("203.0.113.10")));
        BatchOutboundHttpPolicy policy = new BatchOutboundHttpPolicy(properties,
                host -> List.of(InetAddress.getByName("203.0.113.10")));
        BatchOutboundHttpPolicy.ApprovedTarget target =
                policy.approve(URI.create("https://api.example.test/run"), 12);
        assertEquals("203.0.113.10", target.address().getHostAddress());
        assertEquals("api.example.test", target.host());
    }

    @Test
    void dnsPinMismatchAndMetadataAddressFailClosed() throws Exception {
        WorkerOperationalProperties.OutboundHttp properties = enabled();
        properties.setHostPins(Map.of("api.example.test", List.of("203.0.113.10")));
        BatchOutboundHttpPolicy mismatch = new BatchOutboundHttpPolicy(properties,
                host -> List.of(InetAddress.getByName("203.0.113.11")));
        assertThrows(SecurityException.class,
                () -> mismatch.approve(URI.create("https://api.example.test/run"), 0));

        properties.setHostPins(Map.of("api.example.test", List.of("169.254.169.254")));
        BatchOutboundHttpPolicy metadata = new BatchOutboundHttpPolicy(properties,
                host -> List.of(InetAddress.getByName("169.254.169.254")));
        assertThrows(SecurityException.class,
                () -> metadata.approve(URI.create("https://api.example.test/latest/meta-data"), 0));
    }

    @Test
    void requestBudgetAndPlainHttpFailClosed() throws Exception {
        WorkerOperationalProperties.OutboundHttp properties = enabled();
        properties.setMaxRequestBytes(4);
        properties.setHostPins(Map.of("api.example.test", List.of("203.0.113.10")));
        BatchOutboundHttpPolicy policy = new BatchOutboundHttpPolicy(properties,
                host -> List.of(InetAddress.getByName("203.0.113.10")));
        assertThrows(SecurityException.class,
                () -> policy.approve(URI.create("https://api.example.test/run"), 5));
        assertThrows(SecurityException.class,
                () -> policy.approve(URI.create("http://api.example.test/run"), 0));
    }


    @Test
    void mixedDnsAndCidrMismatchFailClosed() throws Exception {
        WorkerOperationalProperties.OutboundHttp properties = enabled();
        properties.setAllowPrivateAddresses(true);
        properties.setHostPins(Map.of("api.example.test", List.of("10.0.0.10", "203.0.113.10")));
        BatchOutboundHttpPolicy mixed = new BatchOutboundHttpPolicy(properties, host -> List.of(
                InetAddress.getByName("10.0.0.10"), InetAddress.getByName("203.0.113.10")));
        assertThrows(SecurityException.class,
                () -> mixed.approve(URI.create("https://api.example.test/run"), 0));

        properties.setHostPins(Map.of("api.example.test", List.of("203.0.113.10")));
        properties.setAllowedCidrs(List.of("198.51.100.0/24"));
        BatchOutboundHttpPolicy cidr = new BatchOutboundHttpPolicy(properties,
                host -> List.of(InetAddress.getByName("203.0.113.10")));
        assertThrows(SecurityException.class,
                () -> cidr.approve(URI.create("https://api.example.test/run"), 0));
    }

    @Test
    void canonicalCidrMatcherSupportsIpv4AndIpv6() throws Exception {
        assertEquals(true, BatchOutboundHttpPolicy.inCidr(
                InetAddress.getByName("203.0.113.10"), "203.0.113.0/24"));
        assertEquals(false, BatchOutboundHttpPolicy.inCidr(
                InetAddress.getByName("203.0.114.10"), "203.0.113.0/24"));
        assertEquals(true, BatchOutboundHttpPolicy.inCidr(
                InetAddress.getByName("2001:db8::10"), "2001:db8::/32"));
    }

    @Test
    void requestHeaderAllowlistAndInjectionFailClosed() {
        WorkerOperationalProperties.OutboundHttp properties = enabled();
        assertEquals(true, properties.getAllowedRequestHeaders().contains("content-type"));
        assertEquals(false, properties.getAllowedRequestHeaders().contains("authorization"));
    }

    private static WorkerOperationalProperties.OutboundHttp enabled() {
        WorkerOperationalProperties.OutboundHttp properties = new WorkerOperationalProperties.OutboundHttp();
        properties.setEnabled(true);
        properties.setAllowedHosts(List.of("api.example.test"));
        properties.setAllowedPorts(Set.of(443));
        return properties;
    }
}
