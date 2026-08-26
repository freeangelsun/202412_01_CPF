package com.cpf.gateway.control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CpfGatewayPlaneBoundaryFilterTest {
    @Test
    void controlApiOnDataPlanePortIsHidden() throws Exception {
        CpfGatewayPlaneBoundaryFilter filter = new CpfGatewayPlaneBoundaryFilter(properties(false));
        MockHttpServletRequest request = request(8070, false);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(404);
        verifyNoInteractions(chain);
    }

    @Test
    void insecureControlRequestIsRejectedWhenTlsListenerIsRequired() throws Exception {
        CpfGatewayPlaneBoundaryFilter filter = new CpfGatewayPlaneBoundaryFilter(properties(true));
        MockHttpServletRequest request = request(9070, false);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
        verifyNoInteractions(chain);
    }

    @Test
    void secureRequestOnDedicatedControlPortContinuesToSignatureFilter() throws Exception {
        CpfGatewayPlaneBoundaryFilter filter = new CpfGatewayPlaneBoundaryFilter(properties(true));
        MockHttpServletRequest request = request(9070, true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    private static MockHttpServletRequest request(int port, boolean secure) {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", "/internal/v1/gateway/registry/bindings");
        request.setLocalPort(port);
        request.setSecure(secure);
        return request;
    }

    private static CpfGatewayControlSecurityProperties properties(boolean tls) {
        CpfGatewayControlSecurityProperties properties = new CpfGatewayControlSecurityProperties();
        properties.setEnabled(true);
        properties.setListenerPort(9070);
        properties.setSharedSecret("0123456789abcdef0123456789abcdef");
        properties.setTlsEnabled(tls);
        if (tls) {
            properties.setKeyStore("/run/secrets/gateway-control.p12");
            properties.setKeyStorePassword("secret-reference-value");
        }
        return properties;
    }
}
