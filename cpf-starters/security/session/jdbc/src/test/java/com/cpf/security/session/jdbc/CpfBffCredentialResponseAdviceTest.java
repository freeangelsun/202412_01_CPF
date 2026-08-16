package com.cpf.security.session.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CpfBffCredentialResponseAdviceTest {
    private final MemoryVault vault = new MemoryVault();
    private final CpfServerSessionProperties properties = new CpfServerSessionProperties(
            true,
            "CPFSESSION",
            Duration.ofMinutes(30),
            true,
            "Strict",
            "/",
            List.of("https://admin.example"),
            null,
            "test",
            Duration.ofMinutes(5),
            Duration.ofMinutes(30),
            1,
            null);
    private final CpfBffCredentialResponseAdvice advice =
            new CpfBffCredentialResponseAdvice(vault, properties, new ObjectMapper(), concurrentSessions());

    @Test
    void stripsMapCredentialsRotatesSessionAndStoresOnlyOpaqueHandle() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/adm/api/auth/login");
        request.getSession(true).setAttribute("before", "value");
        String before = request.getSession(false).getId();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("operatorId", "ADM001");
        body.put("accessToken", "access-secret");
        body.put("refreshToken", "refresh-secret");

        Object result = invoke(body, request);

        assertThat(result).isEqualTo(Map.of("operatorId", "ADM001"));
        assertThat(request.getSession(false).getId()).isNotEqualTo(before);
        Object handle = request.getSession(false)
                .getAttribute(CpfBffSessionBridgeFilter.CREDENTIAL_HANDLE);
        assertThat(handle).isInstanceOf(String.class).asString().doesNotContain("secret");
        assertThat(Collections.list(request.getSession(false).getAttributeNames()))
                .doesNotContain("CPF_BFF_ACCESS_TOKEN", "CPF_BFF_REFRESH_TOKEN");
        assertThat(vault.find((String) handle))
                .get()
                .extracting(CpfBffCredential::accessToken)
                .isEqualTo("access-secret");
    }

    @Test
    void stripsCredentialsFromRecordResponseAndFindsNestedPrincipal() {
        record Operator(String operatorId, String name) {}
        record LoginResult(
                String accessToken,
                String refreshToken,
                String tokenType,
                long expiresIn,
                Operator operator) {}

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/bza/auth/login");
        Object result = invoke(
                new LoginResult("access-secret", "refresh-secret", "Bearer", 300,
                        new Operator("BZA001", "관리자")),
                request);

        Map<?, ?> response = (Map<?, ?>) result;
        assertThat(response.containsKey("accessToken")).isFalse();
        assertThat(response.containsKey("refreshToken")).isFalse();
        assertThat(response.containsKey("sessionId")).isFalse();
        assertThat(response.get("tokenType")).isEqualTo("Bearer");
        String handle = (String) request.getSession(false)
                .getAttribute(CpfBffSessionBridgeFilter.CREDENTIAL_HANDLE);
        assertThat(vault.find(handle)).get().extracting(CpfBffCredential::accessToken)
                .isEqualTo("access-secret");
        assertThat(request.getSession(false).getAttribute(CpfBffSessionBridgeFilter.PRINCIPAL_ID))
                .isEqualTo("BZA001");
    }

    @Test
    void refreshRotatesExistingVaultRowWithoutExposingSessionId() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/adm/api/auth/login");
        invoke(Map.of(
                "operatorId", "ADM001",
                "accessToken", "first",
                "refreshToken", "refresh-1"), request);
        String handle = (String) request.getSession(false)
                .getAttribute(CpfBffSessionBridgeFilter.CREDENTIAL_HANDLE);
        request.setRequestURI("/adm/api/auth/refresh");

        Object result = invoke(Map.of(
                "operatorId", "ADM001",
                "accessToken", "second",
                "refreshToken", "refresh-2",
                "sessionId", "leak"), request);

        Map<?, ?> response = (Map<?, ?>) result;
        assertThat(response.containsKey("accessToken")).isFalse();
        assertThat(response.containsKey("refreshToken")).isFalse();
        assertThat(response.containsKey("sessionId")).isFalse();
        assertThat(request.getSession(false).getAttribute(CpfBffSessionBridgeFilter.CREDENTIAL_HANDLE))
                .isEqualTo(handle);
        assertThat(vault.find(handle)).get().satisfies(credential -> {
            assertThat(credential.accessToken()).isEqualTo("second");
            assertThat(credential.refreshToken()).isEqualTo("refresh-2");
            assertThat(credential.version()).isEqualTo(2);
        });
    }

    @Test
    void refreshWithoutNewRefreshTokenPreservesExistingRefreshCredentialAndExpiry() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/bza/auth/login");
        invoke(Map.of(
                "operatorId", "BZA001",
                "accessToken", "first",
                "refreshToken", "refresh-stable"), request);
        String handle = (String) request.getSession(false)
                .getAttribute(CpfBffSessionBridgeFilter.CREDENTIAL_HANDLE);
        CpfBffCredential before = vault.find(handle).orElseThrow();
        request.setRequestURI("/api/bza/auth/refresh");

        invoke(Map.of(
                "operatorId", "BZA001",
                "accessToken", "second"), request);

        CpfBffCredential after = vault.find(handle).orElseThrow();
        assertThat(after.accessToken()).isEqualTo("second");
        assertThat(after.refreshToken()).isEqualTo("refresh-stable");
        assertThat(after.refreshExpiresAt()).isEqualTo(before.refreshExpiresAt());
        assertThat(after.version()).isEqualTo(2);
    }

    @Test
    void rejectsNestedUnknownCredentialShape() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/adm/api/auth/login");
        assertThatThrownBy(() -> invoke(Map.of("data", Map.of("accessToken", "secret")), request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("invalid accessToken");
    }

    @Test
    void leavesErrorBodyWithoutCredentialsUntouched() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/adm/api/auth/login");
        Map<String, Object> body = Map.of("code", "AUTH_FAILED");
        assertThat(invoke(body, request)).isSameAs(body);
        assertThat(request.getSession(false)).isNull();
    }

    private Object invoke(Object body, MockHttpServletRequest request) {
        return advice.beforeBodyWrite(
                body,
                null,
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                new ServletServerHttpRequest(request),
                new ServletServerHttpResponse(new MockHttpServletResponse()));
    }

    private static CpfBffConcurrentSessionController concurrentSessions() {
        @SuppressWarnings("unchecked")
        org.springframework.session.FindByIndexNameSessionRepository<org.springframework.session.MapSession> repository =
                org.mockito.Mockito.mock(org.springframework.session.FindByIndexNameSessionRepository.class);
        org.mockito.Mockito.when(repository.findByPrincipalName(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(java.util.Map.of());
        return new CpfBffConcurrentSessionController(repository);
    }

    private static final class MemoryVault implements CpfBffCredentialVault {
        private final Map<String, CpfBffCredential> values = new ConcurrentHashMap<>();

        @Override
        public String create(String accessToken, String refreshToken,
                Instant accessExpiresAt, Instant refreshExpiresAt) {
            String handle = "handle-" + values.size();
            values.put(handle, new CpfBffCredential(
                    handle, accessToken, refreshToken, accessExpiresAt, refreshExpiresAt, 1));
            return handle;
        }

        @Override
        public CpfBffCredential rotate(String handle, String accessToken, String refreshToken,
                Instant accessExpiresAt, Instant refreshExpiresAt, long expectedVersion) {
            CpfBffCredential current = values.get(handle);
            if (current == null || current.version() != expectedVersion) {
                throw new IllegalStateException("conflict");
            }
            CpfBffCredential next = new CpfBffCredential(
                    handle, accessToken, refreshToken, accessExpiresAt, refreshExpiresAt,
                    expectedVersion + 1);
            values.put(handle, next);
            return next;
        }

        @Override
        public Optional<CpfBffCredential> find(String handle) {
            return Optional.ofNullable(values.get(handle));
        }

        @Override
        public void revoke(String handle) {
            values.remove(handle);
        }

        @Override
        public int purgeExpired(Instant now) {
            return 0;
        }
    }
}
