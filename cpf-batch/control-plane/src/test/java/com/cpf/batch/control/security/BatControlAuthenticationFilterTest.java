package com.cpf.batch.control.security;

import com.cpf.batch.api.BatControlHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BatControlAuthenticationFilterTest {
    @Test
    void localLoopbackCreatesVerifiedPrincipalFromStandardOperatorHeader() throws Exception {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("local");
        BatControlAuthenticationFilter filter = new BatControlAuthenticationFilter(environment);
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", "/bat/internal/operations/requestRun");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader(BatControlHeaders.CALLER_SERVICE, "ADM");
        request.addHeader(BatControlHeaders.CALLER_INSTANCE_ID, "adm-local-01");
        request.addHeader(BatControlHeaders.OPERATOR_ID, "operator-a");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<Authentication> captured = new AtomicReference<>();

        filter.doFilter(
                request,
                response,
                (filteredRequest, filteredResponse) ->
                        captured.set(SecurityContextHolder.getContext().getAuthentication()));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(captured.get()).isNotNull();
        assertThat(captured.get().getName()).isEqualTo("operator-a");
        assertThat(captured.get().getDetails())
                .isInstanceOf(BatAuthenticatedIdentity.class);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void localInternalOwnerApiRejectsNonAdmCaller() throws Exception {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("test");
        BatControlAuthenticationFilter filter = new BatControlAuthenticationFilter(environment);
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", "/bat/internal/operations/findJobs");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader(BatControlHeaders.CALLER_SERVICE, "REF");
        request.addHeader(BatControlHeaders.CALLER_INSTANCE_ID, "ref-test-01");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> {
            throw new AssertionError("Rejected request must not reach the controller");
        });

        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void admCallerWithoutVerifiedOperatorIsRejected() throws Exception {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("local");
        BatControlAuthenticationFilter filter = new BatControlAuthenticationFilter(environment);
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", "/bat/internal/center-cut/findJobs");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader(BatControlHeaders.CALLER_SERVICE, "ADM");
        request.addHeader(BatControlHeaders.CALLER_INSTANCE_ID, "adm-local-01");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> {
            throw new AssertionError("ADM request without an operator must not reach the controller");
        });

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void productProfileFailsStartupWithoutCertificateIdentityMapping() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");

        assertThatThrownBy(() -> new BatControlAuthenticationFilter(environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("trusted-client-identities");
    }

    @Test
    void productProfileFailsStartupWhenMutualTlsIsNotRequired() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty(
                        "cpf.batch.security.trusted-client-identities",
                        "ADM=CN=cpf-admin");
        environment.setActiveProfiles("prod");

        assertThatThrownBy(() -> new BatControlAuthenticationFilter(environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("client-auth=need");
    }
}
