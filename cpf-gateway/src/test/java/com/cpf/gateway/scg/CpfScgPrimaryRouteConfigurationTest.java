package com.cpf.gateway.scg;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.function.RequestPredicate;
import org.springframework.web.servlet.function.ServerRequest;

@SuppressWarnings("deprecation")
class CpfScgPrimaryRouteConfigurationTest {
    @Test
    void platformOpenApiAndSwaggerRequestsNeverEnterBusinessGatewayRouting() {
        RequestPredicate business = CpfScgPrimaryRouteConfiguration.businessRequestPredicate(
                new MockEnvironment());

        assertThat(business.test(request("/orders/123"))).isTrue();
        assertThat(business.test(request("/v3/api-docs"))).isFalse();
        assertThat(business.test(request("/v3/api-docs/groups/internal"))).isFalse();
        assertThat(business.test(request("/swagger-ui/index.html"))).isFalse();
        assertThat(business.test(request("/swagger-ui.html"))).isFalse();
        assertThat(business.test(request("/actuator/health"))).isFalse();
        assertThat(business.test(request("/api/gateway/control/routes"))).isFalse();
    }

    @Test
    void canonicalAndExplicitEffectiveOpenApiPathsStayOutsideBusinessRouting() {
        MockEnvironment canonical = new MockEnvironment()
                .withProperty("cpf.openapi.webmvc.api-docs-path", "/platform/openapi");
        assertThat(CpfScgPrimaryRouteConfiguration.businessRequestPredicate(canonical)
                .test(request("/platform/openapi"))).isFalse();

        MockEnvironment explicitConsumerOverride = new MockEnvironment()
                .withProperty("cpf.openapi.webmvc.api-docs-path", "/platform/openapi")
                .withProperty("springdoc.api-docs.path", "/effective/openapi")
                .withProperty("springdoc.swagger-ui.path", "/documentation");
        RequestPredicate business = CpfScgPrimaryRouteConfiguration.businessRequestPredicate(
                explicitConsumerOverride);
        assertThat(business.test(request("/effective/openapi"))).isFalse();
        assertThat(business.test(request("/documentation/index.html"))).isFalse();
        assertThat(business.test(request("/customer/contract"))).isTrue();
    }

    @Test
    void publicGatewayDataPlaneOpenApiContractUsesTheActualRouterEntryPaths() {
        OpenAPI openApi = new OpenAPI();

        CpfScgPrimaryRouteConfiguration.applyGatewayDataPlaneOpenApiContract(openApi);

        assertThat(openApi.getTags()).extracting(tag -> tag.getName())
                .contains(CpfScgPrimaryRouteConfiguration.GATEWAY_OPENAPI_TAG);
        PathItem byHeader = openApi.getPaths().get("/cpf/execute");
        PathItem byPath = openApi.getPaths().get("/cpf/execute/{executionId}");
        assertThat(byHeader).isNotNull();
        assertThat(byPath).isNotNull();
        assertThat(byHeader.getPost().getTags())
                .contains(CpfScgPrimaryRouteConfiguration.GATEWAY_OPENAPI_TAG);
        assertThat(byHeader.getPost().getParameters()).extracting(parameter -> parameter.getName())
                .contains("X-Cpf-Gateway-Execution-Id");
        assertThat(byPath.getPost().getParameters()).extracting(parameter -> parameter.getName())
                .contains("executionId");
        assertThat(byHeader.readOperations()).hasSize(7);
        assertThat(byPath.readOperations()).hasSize(7);
    }

    private static ServerRequest request(String path) {
        MockHttpServletRequest servlet = new MockHttpServletRequest("GET", path);
        return ServerRequest.create(servlet, List.of(new ByteArrayHttpMessageConverter()));
    }
}
