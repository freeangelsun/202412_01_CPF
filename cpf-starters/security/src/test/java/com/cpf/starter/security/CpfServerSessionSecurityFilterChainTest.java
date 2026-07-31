package com.cpf.starter.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
@Import({
    CpfServerSessionSecurityAutoConfiguration.class,
    CpfServerSessionSecurityFilterChainTest.TestController.class,
    CpfServerSessionSecurityFilterChainTest.MethodSecurityConfiguration.class
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
        mvc.perform(post("/api/bza/auth/login").with(csrf())).andExpect(status().isOk());
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
        mvc.perform(post("/adm/api/runtime/control").with(operator))
                .andExpect(status().isForbidden());
        mvc.perform(post("/adm/api/runtime/control").with(operator).with(csrf()))
                .andExpect(status().isOk());
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
    }
}
