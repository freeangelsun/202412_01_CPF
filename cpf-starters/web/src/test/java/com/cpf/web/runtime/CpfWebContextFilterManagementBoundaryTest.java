package com.cpf.web.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import com.cpf.foundation.time.spi.CpfBusinessDateProvider;
import com.cpf.web.context.CpfHeaderFailureRecorder;
import com.cpf.web.context.CpfHeaderPolicyRegistry;
import com.cpf.web.context.CpfHttpInboundContextAdapter;
import com.cpf.web.context.CpfHttpIngressTrustResolver;
import com.cpf.web.context.CpfRuntimeIdentity;
import com.cpf.web.context.CpfTrustedProxyClientIpResolver;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class CpfWebContextFilterManagementBoundaryTest {
    @Test
    void excludesExactDefaultManagementBoundaryButNotAdjacentBusinessPaths() {
        CpfWebContextFilter filter = filter("/actuator");

        assertThat(filter.shouldNotFilter(request("", "/actuator"))).isTrue();
        assertThat(filter.shouldNotFilter(request("", "/actuator/health"))).isTrue();
        assertThat(filter.shouldNotFilter(request("", "/actuatorx/health"))).isFalse();
        assertThat(filter.shouldNotFilter(request("", "/api/member"))).isFalse();
    }

    @Test
    void honorsConfiguredManagementBasePathAndServletContextPath() {
        CpfWebContextFilter filter = filter("manage/");

        assertThat(filter.shouldNotFilter(request("/app", "/app/manage/health"))).isTrue();
        assertThat(filter.shouldNotFilter(request("/app", "/app/managex/health"))).isFalse();
    }

    @Test
    void rootManagementBaseExcludesOnlyDiscoveredEndpointRoots() {
        CpfWebContextFilter filter = filter("/", java.util.List.of("/health", "/info"));

        assertThat(filter.shouldNotFilter(request("", "/health"))).isTrue();
        assertThat(filter.shouldNotFilter(request("", "/health/liveness"))).isTrue();
        assertThat(filter.shouldNotFilter(request("", "/info"))).isTrue();
        assertThat(filter.shouldNotFilter(request("", "/member"))).isFalse();
    }

    @Test
    void excludesOwnerDeclaredManagementRootWithoutMatchingAdjacentBusinessPath() {
        CpfWebContextFilter filter = filter("/actuator", java.util.List.of("/api/v1/batch", "/bat"));

        assertThat(filter.shouldNotFilter(request("", "/api/v1/batch/runtime/registrations"))).isTrue();
        assertThat(filter.shouldNotFilter(request("", "/bat/internal/executions"))).isTrue();
        assertThat(filter.shouldNotFilter(request("", "/api/v1/batches/member"))).isFalse();
        assertThat(filter.shouldNotFilter(request("", "/batch/member"))).isFalse();
    }

    private static MockHttpServletRequest request(String contextPath, String requestUri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", requestUri);
        request.setContextPath(contextPath);
        return request;
    }

    private static CpfWebContextFilter filter(String managementBasePath) {
        return filter(managementBasePath, java.util.List.of());
    }

    private static CpfWebContextFilter filter(String managementBasePath, java.util.List<String> roots) {
        return new CpfWebContextFilter(
                mock(CpfHttpInboundContextAdapter.class),
                mock(CpfBusinessDateProvider.class),
                mock(CpfTransactionIdGenerator.class),
                mock(CpfHttpIngressTrustResolver.class),
                mock(CpfTrustedProxyClientIpResolver.class),
                mock(CpfHeaderPolicyRegistry.class),
                mock(CpfHeaderFailureRecorder.class),
                mock(CpfRuntimeIdentity.class),
                null,
                managementBasePath,
                roots);
    }
}
