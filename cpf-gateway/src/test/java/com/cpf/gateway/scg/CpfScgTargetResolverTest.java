package com.cpf.gateway.scg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.security.api.network.CpfNetworkEndpointPolicy;
import java.net.InetAddress;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CpfScgTargetResolverTest {
    @Test
    void resolvesOnlyBelowRegistryBasePath() {
        URI resolved = CpfScgTargetResolver.resolveCanonical(
                URI.create("https://service.internal:8443/base"),
                "/orders/100",
                "view=summary");
        assertThat(resolved).isEqualTo(URI.create(
                "https://service.internal:8443/base/orders/100?view=summary"));
    }

    @Test
    void canonicalUriPreservesTlsHostnameWhilePinnedContextOwnsConnectionAddress() throws Exception {
        URI canonical = URI.create("https://service.internal:8443/base/orders");
        InetAddress pinned = InetAddress.getByName("10.20.30.40");
        String result = CpfGatewayPinnedAddressContext.call(
                canonical.getHost(), pinned,
                () -> CpfGatewayPinnedAddressContext.resolve(canonical.getHost())[0].getHostAddress());
        assertThat(canonical.getHost()).isEqualTo("service.internal");
        assertThat(result).isEqualTo("10.20.30.40");
        assertThat(CpfGatewayPinnedAddressContext.active()).isFalse();
    }

    @Test
    void rejectsMixedPrivatePublicAndMetadataResponses() throws Exception {
        URI base = URI.create("https://service.internal/base");
        CpfNetworkEndpointPolicy allowPrivateAndPublic = new CpfNetworkEndpointPolicy(
                List.of(), Set.of(443), true, true, true, true);
        assertThatThrownBy(() -> CpfScgTargetResolver.validateResolvedAddresses(
                base, allowPrivateAndPublic, ignored -> new InetAddress[] {
                    InetAddress.getByName("10.0.0.10"),
                    InetAddress.getByName("8.8.8.8")
                })).isInstanceOf(SecurityException.class)
                .hasMessageContaining("mixed private/public");
        assertThatThrownBy(() -> CpfScgTargetResolver.validateResolvedAddresses(
                base, allowPrivateAndPublic, ignored -> new InetAddress[] {
                    InetAddress.getByName("169.254.169.254")
                })).isInstanceOf(SecurityException.class)
                .hasMessageContaining("network policy denied");
    }

    @Test
    void dnsChangeCannotAlterActivePinnedConnectionIdentity() throws Exception {
        URI base = URI.create("https://service.internal/base");
        CpfNetworkEndpointPolicy privateOnly = new CpfNetworkEndpointPolicy(
                List.of(), Set.of(443), true, false, true, true);
        var approved = CpfScgTargetResolver.validateResolvedAddresses(
                base, privateOnly, ignored -> new InetAddress[] {InetAddress.getByName("10.0.0.7")});
        URI canonical = CpfScgTargetResolver.resolveCanonical(base, "/orders", null);
        String actual = CpfGatewayPinnedAddressContext.call(
                canonical.getHost(), approved.getFirst(),
                () -> CpfGatewayPinnedAddressContext.resolve("service.internal")[0].getHostAddress());
        assertThat(canonical.getHost()).isEqualTo("service.internal");
        assertThat(actual).isEqualTo("10.0.0.7");
        assertThatThrownBy(() -> CpfGatewayPinnedAddressContext.resolve("attacker.internal"))
                .isInstanceOf(java.net.UnknownHostException.class);
    }

    @Test
    void rejectsTraversalAuthorityAndControlCharacters() {
        URI base = URI.create("https://service.internal/base/");
        assertThatThrownBy(() -> CpfScgTargetResolver.resolveCanonical(base, "../admin", null))
                .isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> CpfScgTargetResolver.resolveCanonical(base, "//evil.example/path", null))
                .isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> CpfScgTargetResolver.resolveCanonical(base, "/ok\r\nInjected: yes", null))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void rejectsRegistryUriWithCredentialsOrQuery() {
        assertThatThrownBy(() -> CpfScgTargetResolver.validateBaseUri(
                URI.create("https://user:password@service.internal/base")))
                .isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> CpfScgTargetResolver.validateBaseUri(
                URI.create("https://service.internal/base?target=other")))
                .isInstanceOf(SecurityException.class);
    }
}
