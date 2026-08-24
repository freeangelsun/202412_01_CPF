package com.cpf.integration.http.internal.domaincall;

import com.cpf.web.context.CpfHttpIngressTrust;
import com.cpf.web.context.CpfHttpIngressTrustResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CpfDomainCallSecurityAutoConfigurationTest.TestController.class)
@ContextConfiguration(classes = {
        CpfDomainCallSecurityAutoConfiguration.class,
        CpfDomainCallSecurityAutoConfigurationTest.TestController.class,
        CpfDomainCallSecurityAutoConfigurationTest.SecurityFixture.class
})
@ImportAutoConfiguration({
        org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration.class,
        org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration.class
})
class CpfDomainCallSecurityAutoConfigurationTest {
    @Autowired MockMvc mvc;

    @Test
    void trustedInternalDomainCallDoesNotRequireBrowserCsrfToken() throws Exception {
        mvc.perform(post("/_cpf/domain/ping").with(request -> {
                    request.setRemoteAddr("10.20.30.40");
                    return request;
                }))
                .andExpect(status().isOk());
    }

    @Test
    void untrustedDomainCallIsRejectedBeforeController() throws Exception {
        mvc.perform(post("/_cpf/domain/ping").with(request -> {
                    request.setRemoteAddr("203.0.113.9");
                    return request;
                }))
                .andExpect(status().isForbidden());
    }

    @Test
    void unrelatedUserApiRetainsItsCsrfBoundary() throws Exception {
        mvc.perform(post("/user/ping"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/user/ping").with(csrf()))
                .andExpect(status().isOk());
    }

    @RestController
    static class TestController {
        @PostMapping("/_cpf/domain/ping") String domain() { return "domain"; }
        @PostMapping("/user/ping") String user() { return "user"; }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class SecurityFixture {
        @Bean
        CpfHttpIngressTrustResolver ingressTrustResolver() {
            return request -> "10.20.30.40".equals(request.getRemoteAddr())
                    ? new CpfHttpIngressTrustResolver.Decision(CpfHttpIngressTrust.TRUSTED_INTERNAL, "BAT")
                    : new CpfHttpIngressTrustResolver.Decision(CpfHttpIngressTrust.UNTRUSTED_EXTERNAL, null);
        }

        @Bean
        @Order(100)
        SecurityFilterChain userApiSecurityFilterChain(HttpSecurity http) throws Exception {
            http.securityMatcher("/**")
                    .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
            return http.build();
        }
    }
}
