package cpf.pfw.gateway.service;

import cpf.pfw.channel.application.CpfChannelPolicyService;
import cpf.pfw.channel.model.CpfChannelPolicyDecision;
import cpf.pfw.common.gateway.CpfGatewayAuthorizationPort;
import cpf.pfw.common.gateway.CpfGatewayRoute;
import cpf.pfw.common.header.CpfHeaderNames;
import cpf.pfw.common.logging.ServerInstanceIdentity;
import cpf.pfw.common.servicecall.CpfEndpointResolver;
import cpf.pfw.common.servicecall.ServiceCallRequest;
import cpf.pfw.common.servicecall.ServiceCallResolvedTarget;
import cpf.pfw.gateway.route.PfwGatewayRouteSnapshot;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** route·registry·header 정책을 적용해 대상 서비스로 요청을 전달합니다. */
@Service
public class PfwGatewayProxyService {
    private static final Set<String> HOP_BY_HOP = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailer", "transfer-encoding", "upgrade", "host", "content-length");
    private static final Set<String> REGENERATED_INTERNAL = Set.of(
            CpfHeaderNames.GATEWAY_INSTANCE_ID.toLowerCase(Locale.ROOT),
            CpfHeaderNames.GATEWAY_ROUTE_ID.toLowerCase(Locale.ROOT),
            CpfHeaderNames.GATEWAY_ROUTE_VERSION.toLowerCase(Locale.ROOT),
            CpfHeaderNames.INGRESS_TYPE.toLowerCase(Locale.ROOT));

    private final PfwGatewayRouteSnapshot snapshot;
    private final CpfEndpointResolver endpointResolver;
    private final CpfGatewayAuthorizationPort authorizationPort;
    private final CpfChannelPolicyService channelPolicyService;
    private final RestClient restClient;

    public PfwGatewayProxyService(
            PfwGatewayRouteSnapshot snapshot,
            CpfEndpointResolver endpointResolver,
            CpfGatewayAuthorizationPort authorizationPort,
            CpfChannelPolicyService channelPolicyService,
            RestClient restClient) {
        this.snapshot = snapshot;
        this.endpointResolver = endpointResolver;
        this.authorizationPort = authorizationPort;
        this.channelPolicyService = channelPolicyService;
        this.restClient = restClient;
    }

    public ResponseEntity<byte[]> execute(String executionId, HttpHeaders inboundHeaders, byte[] body) {
        CpfGatewayRoute route = snapshot.resolve(executionId);
        Map<String, String> trustedHeaders = trustedHeaders(inboundHeaders);
        CpfChannelPolicyDecision channelDecision = channelPolicyService.evaluate(
                route.standardExecutionId(),
                inboundHeaders.getFirst(CpfHeaderNames.ORIGINAL_CHANNEL_CODE),
                inboundHeaders.getFirst(CpfHeaderNames.CHANNEL_CODE),
                inboundHeaders.getFirst(CpfHeaderNames.REQUEST_TYPE),
                hasAuthentication(inboundHeaders),
                inboundHeaders.containsKey(CpfHeaderNames.REQUEST_SIGNATURE));
        if (!channelDecision.allowed()) {
            throw new SecurityException("Gateway 채널 정책에서 요청을 거부했습니다. reason=" + channelDecision.reason());
        }
        if (!authorizationPort.isAllowed(route, trustedHeaders)) {
            throw new SecurityException("Gateway route 실행 권한이 없습니다. permission=" + route.requiredPermission());
        }
        ServiceCallResolvedTarget target = endpointResolver.resolve(ServiceCallRequest.builder(route.serviceId())
                .httpMethod(route.httpMethod())
                .requestPath(route.endpoint())
                .attribute("standardExecutionId", route.standardExecutionId())
                .build());
        URI targetUri = URI.create(trimTrailingSlash(target.baseUrl()) + normalizePath(route.endpoint()));
        HttpHeaders outboundHeaders = outboundHeaders(inboundHeaders, route);
        return restClient.method(httpMethod(route.httpMethod()))
                .uri(targetUri)
                .headers(headers -> headers.putAll(outboundHeaders))
                .body(body == null ? new byte[0] : body)
                .exchange((request, response) -> {
                    byte[] responseBody = response.bodyTo(byte[].class);
                    HttpHeaders responseHeaders = new HttpHeaders();
                    response.getHeaders().forEach((name, values) -> {
                        if (!HOP_BY_HOP.contains(name.toLowerCase(Locale.ROOT))) {
                            responseHeaders.put(name, values);
                        }
                    });
                    responseHeaders.set(CpfHeaderNames.GATEWAY_INSTANCE_ID,
                            ServerInstanceIdentity.current().serverInstanceId());
                    responseHeaders.set(CpfHeaderNames.GATEWAY_ROUTE_ID, route.standardExecutionId());
                    return new ResponseEntity<>(responseBody, responseHeaders, response.getStatusCode());
                });
    }

    private HttpHeaders outboundHeaders(HttpHeaders inbound, CpfGatewayRoute route) {
        HttpHeaders result = new HttpHeaders();
        inbound.forEach((name, values) -> {
            String lower = name.toLowerCase(Locale.ROOT);
            if (!HOP_BY_HOP.contains(lower) && !REGENERATED_INTERNAL.contains(lower)) {
                result.put(name, values);
            }
        });
        result.set(CpfHeaderNames.STANDARD_EXECUTION_ID, route.standardExecutionId());
        result.set(CpfHeaderNames.GATEWAY_INSTANCE_ID, ServerInstanceIdentity.current().serverInstanceId());
        result.set(CpfHeaderNames.GATEWAY_ROUTE_ID, route.standardExecutionId());
        result.set(CpfHeaderNames.GATEWAY_ROUTE_VERSION, route.routeVersion());
        result.set(CpfHeaderNames.INGRESS_TYPE, "PFW_GATEWAY");
        return result;
    }

    private Map<String, String> trustedHeaders(HttpHeaders headers) {
        Map<String, String> result = new LinkedHashMap<>();
        headers.forEach((name, values) -> {
            String lower = name.toLowerCase(Locale.ROOT);
            if (!REGENERATED_INTERNAL.contains(lower) && !HOP_BY_HOP.contains(lower) && !values.isEmpty()) {
                result.put(name, values.getFirst());
            }
        });
        return Map.copyOf(result);
    }

    private boolean hasAuthentication(HttpHeaders headers) {
        return headers.containsKey(CpfHeaderNames.AUTHORIZATION)
                || headers.containsKey(CpfHeaderNames.API_KEY);
    }

    private HttpMethod httpMethod(String value) {
        return value == null || value.isBlank() ? HttpMethod.POST : HttpMethod.valueOf(value);
    }

    private String normalizePath(String value) {
        return value == null || value.isBlank() ? "/" : value.startsWith("/") ? value : "/" + value;
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Gateway 대상 instance의 baseUrl이 비어 있습니다.");
        }
        String normalized = value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
