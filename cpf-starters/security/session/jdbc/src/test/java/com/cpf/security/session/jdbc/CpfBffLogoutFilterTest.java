package com.cpf.security.session.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CpfBffLogoutFilterTest {
    @Test
    void invalidatesSessionEvenWhenVaultRevokeFails() {
        CpfBffCredentialVault vault = new FailingRevokeVault();
        CpfBffLogoutFilter filter = new CpfBffLogoutFilter(vault);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/adm/api/auth/logout");
        request.getSession(true).setAttribute(CpfBffSessionBridgeFilter.CREDENTIAL_HANDLE, "handle-1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilter(request, response, new MockFilterChain()))
                .hasMessageContaining("CPF_BFF_LOGOUT_VAULT_REVOKE_FAILED");
        assertThat(request.getSession(false)).isNull();
    }

    private static final class FailingRevokeVault implements CpfBffCredentialVault {
        @Override public String create(String accessToken, String refreshToken,
                Instant accessExpiresAt, Instant refreshExpiresAt) { return "handle"; }
        @Override public CpfBffCredential rotate(String handle, String accessToken, String refreshToken,
                Instant accessExpiresAt, Instant refreshExpiresAt, long expectedVersion) {
            return new CpfBffCredential(handle, accessToken, refreshToken,
                    accessExpiresAt, refreshExpiresAt, expectedVersion + 1);
        }
        @Override public Optional<CpfBffCredential> find(String handle) { return Optional.empty(); }
        @Override public void revoke(String handle) { throw new IllegalStateException("database unavailable"); }
        @Override public int purgeExpired(Instant now) { return 0; }
    }
}
