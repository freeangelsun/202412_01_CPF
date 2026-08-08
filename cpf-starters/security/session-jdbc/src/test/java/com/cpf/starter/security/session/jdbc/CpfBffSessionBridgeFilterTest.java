package com.cpf.starter.security.session.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CpfBffSessionBridgeFilterTest {
    @Test
    void resolvesOpaqueHandleAndInjectsBearerOnlyDownstream() throws Exception {
        CpfBffCredentialVault vault = fixed(new CpfBffCredential(
                "h1", "token", "refresh",
                Instant.now().plusSeconds(60), Instant.now().plusSeconds(120), 1));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/adm/api/operators/me");
        request.getSession(true).setAttribute(CpfBffSessionBridgeFilter.CREDENTIAL_HANDLE, "h1");
        request.getSession(false).setAttribute(CpfBffSessionBridgeFilter.PRINCIPAL_ID, "ADM001");
        AtomicBoolean called = new AtomicBoolean();

        new CpfBffSessionBridgeFilter(vault).doFilter(
                request, new MockHttpServletResponse(), (req, res) -> {
                    called.set(true);
                    HttpServletRequest wrapped = (HttpServletRequest) req;
                    assertThat(wrapped.getHeader("Authorization")).isEqualTo("Bearer token");
                    assertThat(java.util.Collections.list(wrapped.getHeaderNames()))
                            .contains("Authorization");
                });

        assertThat(called).isTrue();
        assertThat(request.getHeader("Authorization")).isNull();
    }

    @Test
    void refreshUsesVaultRefreshTokenWithoutInjectingAuthorization() throws Exception {
        CpfBffCredentialVault vault = fixed(new CpfBffCredential(
                "h1", "expired-access", "refresh-token",
                Instant.now().minusSeconds(1), Instant.now().plusSeconds(120), 1));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/bza/auth/refresh");
        request.getSession(true).setAttribute(CpfBffSessionBridgeFilter.CREDENTIAL_HANDLE, "h1");
        request.getSession(false).setAttribute(CpfBffSessionBridgeFilter.PRINCIPAL_ID, "BZA001");
        AtomicBoolean called = new AtomicBoolean();

        new CpfBffSessionBridgeFilter(vault).doFilter(
                request, new MockHttpServletResponse(), (req, res) -> {
                    called.set(true);
                    HttpServletRequest http = (HttpServletRequest) req;
                    assertThat(CpfBffSessionBridgeFilter.internalRefreshToken(http))
                            .isEqualTo("refresh-token");
                    assertThat(http.getHeader("Authorization")).isNull();
                });

        assertThat(called).isTrue();
    }

    @Test
    void rejectsBrowserSuppliedAuthorization() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/adm/api/operators/me");
        request.addHeader("Authorization", "Bearer attacker");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new CpfBffSessionBridgeFilter(fixed(null))
                .doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void expiredAccessFailsClosedButKeepsRefreshCapableVaultHandle() throws Exception {
        AtomicBoolean revoked = new AtomicBoolean();
        CpfBffCredential expired = new CpfBffCredential(
                "h1", "token", "refresh",
                Instant.now().minusSeconds(1), Instant.now().plusSeconds(60), 1);
        CpfBffCredentialVault vault = new CpfBffCredentialVault() {
            @Override public String create(String a, String r, Instant ae, Instant re) { return null; }
            @Override public CpfBffCredential rotate(String h, String a, String r,
                    Instant ae, Instant re, long v) { return null; }
            @Override public Optional<CpfBffCredential> find(String h) { return Optional.of(expired); }
            @Override public void revoke(String h) { revoked.set(true); }
            @Override public int purgeExpired(Instant n) { return 0; }
        };
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/adm/api/operators/me");
        request.getSession(true).setAttribute(CpfBffSessionBridgeFilter.CREDENTIAL_HANDLE, "h1");
        request.getSession(false).setAttribute(CpfBffSessionBridgeFilter.PRINCIPAL_ID, "ADM001");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new CpfBffSessionBridgeFilter(vault)
                .doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(revoked).isFalse();
        assertThat(request.getSession(false)
                .getAttribute(CpfBffSessionBridgeFilter.CREDENTIAL_HANDLE)).isEqualTo("h1");
    }

    @Test
    void expiredRefreshRevokesHandle() throws Exception {
        AtomicBoolean revoked = new AtomicBoolean();
        CpfBffCredential expired = new CpfBffCredential(
                "h1", "token", "refresh",
                Instant.now().minusSeconds(1), Instant.now().minusSeconds(1), 1);
        CpfBffCredentialVault vault = new CpfBffCredentialVault() {
            @Override public String create(String a, String r, Instant ae, Instant re) { return null; }
            @Override public CpfBffCredential rotate(String h, String a, String r,
                    Instant ae, Instant re, long v) { return null; }
            @Override public Optional<CpfBffCredential> find(String h) { return Optional.of(expired); }
            @Override public void revoke(String h) { revoked.set(true); }
            @Override public int purgeExpired(Instant n) { return 0; }
        };
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/bza/auth/refresh");
        request.getSession(true).setAttribute(CpfBffSessionBridgeFilter.CREDENTIAL_HANDLE, "h1");
        request.getSession(false).setAttribute(CpfBffSessionBridgeFilter.PRINCIPAL_ID, "BZA001");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new CpfBffSessionBridgeFilter(vault)
                .doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(revoked).isTrue();
        assertThat(request.getSession(false)
                .getAttribute(CpfBffSessionBridgeFilter.CREDENTIAL_HANDLE)).isNull();
    }

    private static CpfBffCredentialVault fixed(CpfBffCredential value) {
        return new CpfBffCredentialVault() {
            @Override public String create(String a, String r, Instant ae, Instant re) { return null; }
            @Override public CpfBffCredential rotate(String h, String a, String r,
                    Instant ae, Instant re, long v) { return null; }
            @Override public Optional<CpfBffCredential> find(String h) {
                return Optional.ofNullable(value);
            }
            @Override public void revoke(String h) {}
            @Override public int purgeExpired(Instant n) { return 0; }
        };
    }
}
