package com.cpf.gateway.scg;

import com.cpf.core.api.gateway.CpfGatewayRateLimitPort;
import com.cpf.gateway.logging.CpfGatewayCaptureService;
import com.cpf.gateway.runtime.CpfGatewayRuntimePolicy;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Constructor;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CpfGatewayRateLimitResponseHeaderPolicyTest {

    @Test
    void gatewayGeneratedRateLimitHeadersSurviveResponseAllowlist() throws Exception {
        CpfGatewayRuntimePolicy policy = new CpfGatewayRuntimePolicy();
        policy.replaceHeaders(1L, Set.of(), Set.of("content-type"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(CpfScgPrimaryHandler.TX_ATTR, "tx");
        request.setAttribute(CpfScgPrimaryHandler.RATE_LIMIT_DECISION_ATTR, denied());
        MockHttpServletResponse response = new MockHttpServletResponse();
        Object wrapper = countingResponse(request, response, policy);

        setHeader(wrapper, "Retry-After", "10");
        setHeader(wrapper, "X-Cpf-RateLimit-Scope", "API");
        setHeader(wrapper, "X-Cpf-RateLimit-Degraded", "false");
        setHeader(wrapper, "X-Cpf-RateLimit-Reason", "QUOTA_EXCEEDED");
        setHeader(wrapper, "X-Untrusted-Upstream", "must-drop");

        assertThat(response.getHeader("Retry-After")).isEqualTo("10");
        assertThat(response.getHeader("X-Cpf-RateLimit-Scope")).isEqualTo("API");
        assertThat(response.getHeader("X-Cpf-RateLimit-Degraded")).isEqualTo("false");
        assertThat(response.getHeader("X-Cpf-RateLimit-Reason")).isEqualTo("QUOTA_EXCEEDED");
        assertThat(response.getHeader("X-Untrusted-Upstream")).isNull();
    }

    @Test
    void upstreamCannotSpoofRateLimitHeadersForAllowedRequest() throws Exception {
        CpfGatewayRuntimePolicy policy = new CpfGatewayRuntimePolicy();
        policy.replaceHeaders(1L, Set.of(), Set.of("content-type"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(CpfScgPrimaryHandler.TX_ATTR, "tx");
        request.setAttribute(CpfScgPrimaryHandler.RATE_LIMIT_DECISION_ATTR, allowed());
        MockHttpServletResponse response = new MockHttpServletResponse();
        Object wrapper = countingResponse(request, response, policy);

        setHeader(wrapper, "Retry-After", "999");
        assertThat(response.getHeader("Retry-After")).isNull();
    }

    private static void setHeader(Object wrapper, String name, String value) throws Exception {
        var method = wrapper.getClass().getDeclaredMethod("setHeader", String.class, String.class);
        method.setAccessible(true);
        method.invoke(wrapper, name, value);
    }

    private static Object countingResponse(
            MockHttpServletRequest request,
            MockHttpServletResponse response,
            CpfGatewayRuntimePolicy policy) throws Exception {
        Class<?> type = Class.forName(
                "com.cpf.gateway.scg.CpfGatewayLedgerCompletionFilter$CountingResponse");
        Constructor<?> constructor = type.getDeclaredConstructor(
                jakarta.servlet.http.HttpServletRequest.class,
                jakarta.servlet.http.HttpServletResponse.class,
                AtomicLong.class,
                long.class,
                String.class,
                CpfGatewayRuntimePolicy.class,
                CpfGatewayCaptureService.class);
        constructor.setAccessible(true);
        return constructor.newInstance(
                request, response, new AtomicLong(), 1024L, "gateway-1", policy,
                mock(CpfGatewayCaptureService.class));
    }

    private static CpfGatewayRateLimitPort.Decision denied() {
        return new CpfGatewayRateLimitPort.Decision(
                false, "API:opaque", CpfGatewayRateLimitPort.Scope.API, 0L,
                Instant.now().plusSeconds(10), Duration.ofSeconds(10),
                false, false, "QUOTA_EXCEEDED");
    }

    private static CpfGatewayRateLimitPort.Decision allowed() {
        return new CpfGatewayRateLimitPort.Decision(
                true, "API:opaque", null, 10L,
                Instant.now().plusSeconds(10), Duration.ZERO,
                false, false, "ALLOWED");
    }
}
