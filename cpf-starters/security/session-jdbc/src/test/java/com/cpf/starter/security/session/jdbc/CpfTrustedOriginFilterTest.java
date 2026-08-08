package com.cpf.starter.security.session.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CpfTrustedOriginFilterTest {
    private final CpfTrustedOriginFilter filter =
            new CpfTrustedOriginFilter(List.of("https://console.example.com"));

    @Test
    void rejectsMalformedRefererWithForbiddenInsteadOfServerError() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/adm/api/operators");
        request.addHeader("Referer", "http://[invalid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void acceptsConfiguredOriginForMutationRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/bza/users");
        request.addHeader("Origin", "https://console.example.com");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void canonicalizesDefaultHttpsPortInConfiguredOrigin() throws Exception {
        CpfTrustedOriginFilter portFilter =
                new CpfTrustedOriginFilter(List.of("https://console.example.com:443"));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/adm/api/operators");
        request.addHeader("Origin", "https://console.example.com");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        portFilter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void rejectsMissingOriginAndRefererForMutationRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/bza/users/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(403);
    }
}
