package com.cpf.starter.security.session.jdbc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

/** BFF matcher가 UI 숨김이 아니라 401/403과 Method Security를 실제로 소유하는지 검증한다. */
@WebMvcTest(
        controllers = CpfServerSessionSecurityFilterChainTest.TestController.class,
        properties = {
            "cpf.security.session.enabled=true",
            "cpf.security.session.secure=false",
            "cpf.security.session.allowed-origins=http://localhost",
            "cpf.security.session.fail-closed=false"
        })
@ContextConfiguration(classes = {
    CpfServerSessionSecurityAutoConfiguration.class,
    CpfServerSessionSecurityFilterChainTest.TestController.class,
    CpfServerSessionSecurityFilterChainTest.MethodSecurityConfiguration.class
})
@ImportAutoConfiguration({
    org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration.class,
    org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration.class
})
class CpfServerSessionSecurityFilterChainTest {
    @Autowired MockMvc mvc;
    @Autowired SecurityFilterChain cpfBffSecurityFilterChain;

    @MockitoBean CpfBffCredentialVault vault;
    @MockitoBean org.springframework.session.FindByIndexNameSessionRepository<?> sessionRepository;
    @MockitoBean javax.sql.DataSource dataSource;

    @Test
    void staticShellAndLoginArePublicButPrivilegedApisAreNot() throws Exception {
        mvc.perform(get("/adm/index.html")).andExpect(status().isOk());
        mvc.perform(post("/api/bza/auth/login").header("Origin", "http://localhost").with(csrf()))
                .andExpect(status().isOk());
        mvc.perform(get("/adm/api/runtime/control")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/bza/customers")).andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedPrincipalWithoutOperationAuthorityIsForbidden() throws Exception {
        mvc.perform(get("/adm/api/runtime/control").with(user("viewer")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/adm/api/runtime/control").with(user("operator")
                        .authorities(new SimpleGrantedAuthority("runtime:control"))))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedWriteRequiresBothAuthorityAndCsrf() throws Exception {
        var operator = user("operator").authorities(new SimpleGrantedAuthority("runtime:control"));
        mvc.perform(post("/adm/api/runtime/control").header("Origin", "http://localhost").with(operator))
                .andExpect(status().isForbidden());
        mvc.perform(post("/adm/api/runtime/control").header("Origin", "http://localhost").with(operator).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void validVaultSessionAuthenticatesWithoutInventedAuthoritiesAndBridgesAccessToken() throws Exception {
        MockHttpSession session = session("handle-1", "BZA001");
        when(vault.find("handle-1")).thenReturn(Optional.of(credential(false)));

        mvc.perform(get("/api/bza/session").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("BZA001:0:Bearer vault-access"));
        verify(vault).find("handle-1");

        mvc.perform(get("/adm/api/runtime/control").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void refreshUsesVaultCredentialAndSessionPrincipal() throws Exception {
        MockHttpSession session = session("handle-1", "BZA001");
        when(vault.find("handle-1")).thenReturn(Optional.of(credential(true)));

        mvc.perform(post("/api/bza/auth/refresh").header("Origin", "http://localhost").session(session).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("BZA001:vault-refresh"));
    }

    @Test
    void logoutAllowsExpiredAccessThenRevokesVaultAndInvalidatesSession() throws Exception {
        MockHttpSession session = session("handle-1", "BZA001");
        when(vault.find("handle-1")).thenReturn(Optional.of(credential(true)));

        mvc.perform(post("/api/bza/auth/logout").header("Origin", "http://localhost").session(session).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("BZA001"));

        verify(vault).revoke("handle-1");
        org.assertj.core.api.Assertions.assertThat(session.isInvalid()).isTrue();
    }

    private static MockHttpSession session(String handle, String principal) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CpfBffSessionBridgeFilter.CREDENTIAL_HANDLE, handle);
        session.setAttribute(CpfBffSessionBridgeFilter.PRINCIPAL_ID, principal);
        return session;
    }

    private static CpfBffCredential credential(boolean expiredAccess) {
        Instant now = Instant.now();
        return new CpfBffCredential(
                "handle-1",
                "vault-access",
                "vault-refresh",
                expiredAccess ? now.minusSeconds(1) : now.plusSeconds(60),
                now.plusSeconds(120),
                1);
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableMethodSecurity
    static class MethodSecurityConfiguration {}

    @org.springframework.web.bind.annotation.RestController
    static class TestController {
        @org.springframework.web.bind.annotation.GetMapping("/adm/index.html")
        String shell() {
            return "ok";
        }

        @org.springframework.web.bind.annotation.PostMapping("/api/bza/auth/login")
        String login() {
            return "ok";
        }

        @org.springframework.web.bind.annotation.GetMapping("/api/bza/customers")
        String customers() {
            return "ok";
        }

        @PreAuthorize("hasAuthority('runtime:control')")
        @org.springframework.web.bind.annotation.GetMapping("/adm/api/runtime/control")
        String controlRead() {
            return "ok";
        }

        @PreAuthorize("hasAuthority('runtime:control')")
        @org.springframework.web.bind.annotation.PostMapping("/adm/api/runtime/control")
        String controlWrite() {
            return "ok";
        }

        @org.springframework.web.bind.annotation.GetMapping("/api/bza/session")
        String session(
                Authentication authentication,
                @org.springframework.web.bind.annotation.RequestHeader("Authorization") String authorization) {
            return authentication.getName() + ":" + authentication.getAuthorities().size() + ":" + authorization;
        }

        @org.springframework.web.bind.annotation.PostMapping("/api/bza/auth/refresh")
        String refresh(HttpServletRequest request, Authentication authentication) {
            return authentication.getName() + ":" + CpfBffSessionBridgeFilter.internalRefreshToken(request);
        }

        @org.springframework.web.bind.annotation.PostMapping("/api/bza/auth/logout")
        String logout(Authentication authentication) {
            return authentication.getName();
        }
    }
}
