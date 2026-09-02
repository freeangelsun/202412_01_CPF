package com.cpf.gateway.scg;

import static org.springframework.web.servlet.function.RequestPredicates.path;

import com.cpf.gateway.context.CpfGatewayHeaderNames;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.function.RequestPredicate;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

/** Gateway 업무 Data Plane만 SCG에 위임하며 Actuator/Control Plane은 별도 보안 경계에 둡니다. */
@Configuration(proxyBeanMethods = false)
public class CpfScgPrimaryRouteConfiguration {
    static final String GATEWAY_OPENAPI_TAG = "CPF Gateway";

    @Bean
    RouterFunction<ServerResponse> cpfPrimaryGatewayRoutes(CpfScgPrimaryHandler handler, Environment environment) {
        return RouterFunctions.route(businessRequestPredicate(environment), handler);
    }

    /**
     * Documents the two stable public entry paths of the dynamic Gateway Data Plane.
     *
     * <p>The actual target, authorization and allowed method remain the approved runtime binding;
     * this customizer neither creates a route nor makes a default-deny installation routable. It
     * supplies the metadata that Springdoc cannot infer from the catch-all RouterFunction.</p>
     */
    @Bean("cpfGatewayPublicDataPlaneOpenApiCustomizer")
    OpenApiCustomizer cpfGatewayPublicDataPlaneOpenApiCustomizer() {
        return CpfScgPrimaryRouteConfiguration::applyGatewayDataPlaneOpenApiContract;
    }

    static void applyGatewayDataPlaneOpenApiContract(OpenAPI openApi) {
        List<Tag> tags = openApi.getTags() == null ? new ArrayList<>() : new ArrayList<>(openApi.getTags());
        if (tags.stream().noneMatch(tag -> GATEWAY_OPENAPI_TAG.equals(tag.getName()))) {
            tags.add(new Tag().name(GATEWAY_OPENAPI_TAG)
                    .description("CPF Gateway approved-binding Data Plane"));
        }
        openApi.setTags(tags);

        Paths paths = openApi.getPaths() == null ? new Paths() : openApi.getPaths();
        openApi.setPaths(paths);
        addGatewayPath(paths, "/cpf/execute", "cpfGatewayExecuteByHeader", true);
        addGatewayPath(paths, "/cpf/execute/{executionId}", "cpfGatewayExecuteByPath", false);
    }

    private static void addGatewayPath(Paths paths, String path, String operationIdPrefix, boolean executionIdInHeader) {
        PathItem item = paths.get(path);
        if (item == null) {
            item = new PathItem();
            paths.addPathItem(path, item);
        }
        if (item.getGet() == null) item.setGet(gatewayOperation(operationIdPrefix, "Get", executionIdInHeader));
        if (item.getPost() == null) item.setPost(gatewayOperation(operationIdPrefix, "Post", executionIdInHeader));
        if (item.getPut() == null) item.setPut(gatewayOperation(operationIdPrefix, "Put", executionIdInHeader));
        if (item.getPatch() == null) item.setPatch(gatewayOperation(operationIdPrefix, "Patch", executionIdInHeader));
        if (item.getDelete() == null) item.setDelete(gatewayOperation(operationIdPrefix, "Delete", executionIdInHeader));
        if (item.getHead() == null) item.setHead(gatewayOperation(operationIdPrefix, "Head", executionIdInHeader));
        if (item.getOptions() == null) item.setOptions(gatewayOperation(operationIdPrefix, "Options", executionIdInHeader));
    }

    private static Operation gatewayOperation(String operationIdPrefix, String method, boolean executionIdInHeader) {
        Operation operation = new Operation()
                .operationId(operationIdPrefix + method)
                .summary("Execute an approved Gateway binding")
                .description("The approved Gateway binding selects the target and permits the HTTP method. "
                        + "No binding is implied by this OpenAPI entry.")
                .addTagsItem(GATEWAY_OPENAPI_TAG)
                .responses(gatewayResponses());
        operation.addExtension("x-cpf-gateway-binding-method", "GW_BINDING.http_method");
        if (executionIdInHeader) {
            operation.addParametersItem(new Parameter()
                    .name(CpfGatewayHeaderNames.EXECUTION_ROUTE_ID)
                    .in("header")
                    .required(true)
                    .description("Approved Gateway binding execution identifier")
                    .schema(new StringSchema()));
        } else {
            operation.addParametersItem(new Parameter()
                    .name("executionId")
                    .in("path")
                    .required(true)
                    .description("Approved Gateway binding execution identifier")
                    .schema(new StringSchema()));
        }
        return operation;
    }

    private static ApiResponses gatewayResponses() {
        return new ApiResponses()
                .addApiResponse("200", new ApiResponse().description("Configured target response"))
                .addApiResponse("400", new ApiResponse().description("Invalid execution identifier, request, or method"))
                .addApiResponse("401", new ApiResponse().description("Authentication required by the approved binding"))
                .addApiResponse("403", new ApiResponse().description("Gateway binding authorization denied"))
                .addApiResponse("404", new ApiResponse().description("No approved Gateway binding"))
                .addApiResponse("409", new ApiResponse().description("Binding version or state conflict"))
                .addApiResponse("429", new ApiResponse().description("Gateway rate limit exceeded"))
                .addApiResponse("503", new ApiResponse().description("Target or Gateway temporarily unavailable"));
    }

    /**
     * Keeps CPF Platform management surfaces outside the Gateway business-route resolver.
     *
     * <p>The OpenAPI document is owned by the web starter's canonical
     * {@code cpf.openapi.webmvc.api-docs-path}. Springdoc may explicitly override its effective
     * path, so the predicate resolves that already-supported consumer override first. Neither
     * document nor Swagger UI requests represent an approved Gateway business route.</p>
     */
    static RequestPredicate businessRequestPredicate(Environment environment) {
        String canonicalApiDocsPath = safePath(
                environment.getProperty("cpf.openapi.webmvc.api-docs-path"), "/v3/api-docs");
        String apiDocsPath = safePath(
                environment.getProperty("springdoc.api-docs.path"), canonicalApiDocsPath);
        String swaggerUiPath = safePath(
                environment.getProperty("springdoc.swagger-ui.path"), "/swagger-ui");
        RequestPredicate business = path("/**")
                .and(path("/actuator/**").negate())
                .and(path("/internal/**").negate())
                .and(path("/api/gateway/control/**").negate())
                .and(platformSurface(apiDocsPath).negate())
                .and(platformSurface(swaggerUiPath).negate())
                .and(path("/swagger-ui.html").negate());
        return business;
    }

    private static RequestPredicate platformSurface(String rootPath) {
        return path(rootPath).or(path(rootPath + "/**"));
    }

    private static String safePath(String candidate, String fallback) {
        if (candidate == null) return fallback;
        String value = candidate.trim();
        if (value.isEmpty() || !value.startsWith("/") || value.contains("..")) return fallback;
        return value.length() > 1 && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
