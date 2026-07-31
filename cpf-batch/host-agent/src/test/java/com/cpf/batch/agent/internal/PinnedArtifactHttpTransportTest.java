package com.cpf.batch.agent.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.batch.agent.AgentProperties;
import java.net.InetAddress;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PinnedArtifactHttpTransportTest {
    @Test
    void publicHostnameRequiresExplicitAddressPins() throws Exception {
        AgentProperties properties = base();
        PinnedArtifactHttpTransport transport = new PinnedArtifactHttpTransport(properties,
                host -> List.of(InetAddress.getByName("203.0.113.10")));
        assertThatThrownBy(() -> transport.resolveAndValidate(URI.create("https://repo.example.test/repository/")))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("PIN_REQUIRED");
    }

    @Test
    void literalLoopbackHttpRequiresExplicitDevelopmentSwitch() throws Exception {
        AgentProperties properties = base();
        properties.setArtifactAllowedHosts(List.of("127.0.0.1"));
        properties.setArtifactAllowedPorts(Set.of(80));
        properties.setAllowPrivateRepositoryAddresses(true);
        PinnedArtifactHttpTransport transport = new PinnedArtifactHttpTransport(properties,
                host -> List.of(InetAddress.getByName("127.0.0.1")));
        assertThatThrownBy(() -> transport.resolveAndValidate(URI.create("http://127.0.0.1/repository/")))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("TLS_REQUIRED");

        properties.setAllowHttpLoopback(true);
        assertThat(transport.resolveAndValidate(URI.create("http://127.0.0.1/repository/"))
                .address().isLoopbackAddress()).isTrue();
    }

    @Test
    void mismatchingPinAndMetadataAddressFailClosed() throws Exception {
        AgentProperties properties = base();
        properties.setArtifactPinnedAddresses(List.of("203.0.113.10"));
        PinnedArtifactHttpTransport mismatch = new PinnedArtifactHttpTransport(properties,
                host -> List.of(InetAddress.getByName("203.0.113.11")));
        assertThatThrownBy(() -> mismatch.resolveAndValidate(URI.create("https://repo.example.test/repository/")))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("PIN_MISMATCH");

        properties.setArtifactPinnedAddresses(List.of("169.254.169.254"));
        properties.setAllowPrivateRepositoryAddresses(true);
        PinnedArtifactHttpTransport metadata = new PinnedArtifactHttpTransport(properties,
                host -> List.of(InetAddress.getByName("169.254.169.254")));
        assertThatThrownBy(() -> metadata.resolveAndValidate(URI.create("https://repo.example.test/latest/meta-data")))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("METADATA_ADDRESS_DENIED");
    }

    @Test
    void mixedDnsCidrAndPortPoliciesFailClosed() throws Exception {
        AgentProperties properties = base();
        properties.setAllowPrivateRepositoryAddresses(true);
        properties.setArtifactPinnedAddresses(List.of("10.0.0.10", "203.0.113.10"));
        PinnedArtifactHttpTransport mixed = new PinnedArtifactHttpTransport(properties,
                host -> List.of(InetAddress.getByName("10.0.0.10"), InetAddress.getByName("203.0.113.10")));
        assertThatThrownBy(() -> mixed.resolveAndValidate(URI.create("https://repo.example.test/repository/")))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("MIXED_DNS_RESPONSE_DENIED");

        properties.setArtifactPinnedAddresses(List.of("203.0.113.10"));
        properties.setArtifactAllowedCidrs(List.of("198.51.100.0/24"));
        PinnedArtifactHttpTransport cidr = new PinnedArtifactHttpTransport(properties,
                host -> List.of(InetAddress.getByName("203.0.113.10")));
        assertThatThrownBy(() -> cidr.resolveAndValidate(URI.create("https://repo.example.test/repository/")))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("CIDR_DENIED");

        properties.setArtifactAllowedCidrs(List.of());
        assertThatThrownBy(() -> cidr.resolveAndValidate(URI.create("https://repo.example.test:8443/repository/")))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("PORT_DENIED");
    }

    @Test
    void cidrMatcherSupportsIpv4AndIpv6() throws Exception {
        assertThat(PinnedArtifactHttpTransport.inCidr(
                InetAddress.getByName("203.0.113.10"), "203.0.113.0/24")).isTrue();
        assertThat(PinnedArtifactHttpTransport.inCidr(
                InetAddress.getByName("203.0.114.10"), "203.0.113.0/24")).isFalse();
        assertThat(PinnedArtifactHttpTransport.inCidr(
                InetAddress.getByName("2001:db8::10"), "2001:db8::/32")).isTrue();
    }

    private static AgentProperties base() {
        AgentProperties properties = new AgentProperties();
        properties.setArtifactAllowedHosts(List.of("repo.example.test"));
        return properties;
    }
}
