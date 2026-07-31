package com.cpf.gateway.scg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
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
