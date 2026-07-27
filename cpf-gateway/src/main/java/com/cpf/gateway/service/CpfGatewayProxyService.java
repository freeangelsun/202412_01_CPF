package com.cpf.gateway.service;

import com.cpf.core.channel.application.CpfChannelPolicyService;
import com.cpf.core.api.servicecall.CpfServiceCallCommand;
import com.cpf.core.api.servicecall.CpfServiceCallExecutor;
import com.cpf.core.api.servicecall.CpfServiceCallFailedException;
import com.cpf.core.api.servicecall.CpfServiceCallOutcome;
import com.cpf.core.api.servicecall.CpfServiceCallTarget;

import com.cpf.core.channel.model.CpfChannelPolicyDecision;
import com.cpf.core.api.gateway.CpfGatewayAuthorizationPort;
import com.cpf.core.api.gateway.CpfGatewayRoute;
import com.cpf.core.api.header.CpfHeaderNames;
import com.cpf.core.api.runtime.CpfInstanceIdentity;
import com.cpf.gateway.route.CpfGatewayRouteSnapshot;
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
public class CpfGatewayProxyService {
    private static final Set<String> HOP_BY_HOP = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailer", "transfer-encoding", "upgrade", "host", "content-length");
    private static final Set<String> REGENERATED_INTERNAL = Set.of(
            CpfHeaderNames.GATEWAY_INSTANCE_ID.toLowerCase(Locale.ROOT),
            CpfHeaderNames.GATEWAY_ROUTE_ID.toLowerCase(Locale.ROOT),
            CpfHeaderNames.GATEWAY_ROUTE_VERSION.toLowerCase(Locale.ROOT),
            CpfHeaderNames.INGRESS_TYPE.toLowerCase(Locale.ROOT));

    private final CpfGatewayRouteSnapshot snapshot;
    private final CpfServiceCallExecutor serviceCallEngine;
    private final CpfGatewayAuthorizationPort authorizationPort;
    private final CpfChannelPolicyService channelPolicyService;
    private final RestClient restClient;

    public CpfGatewayProxyService(
            CpfGatewayRouteSnapshot snapshot,
            CpfServiceCallExecutor serviceCallEngine,
            CpfGatewayAuthorizationPort authorizationPort,
            CpfChannelPolicyService channelPolicyService,
            RestClient restClient) {
        this.snapshot = snapshot;
        this.serviceCallEngine = serviceCallEngine;
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
        CpfServiceCallCommand callRequest = CpfServiceCallCommand.builder(route.serviceId())
                .httpMethod(route.httpMethod())
                .requestPath(route.endpoint())
                .attribute("standardExecutionId", route.standardExecutionId())
                .build();
        HttpHeaders outboundHeaders = outboundHeaders(inboundHeaders, route);
        CpfServiceCallOutcome<ResponseEntity<byte[]>> result = serviceCallEngine.invoke(callRequest, target ->
                invokeTarget(target, route, outboundHeaders, body));
        if (!"SUCCESS".equals(result.status()) || result.responseBody() == null) {
            throw new CpfServiceCallFailedException(result);
        }
        return withGatewayResponseHeaders(result.responseBody(), route);
    }


    private ResponseEntity<byte[]> invokeTarget(
            CpfServiceCallTarget target,
            CpfGatewayRoute route,
            HttpHeaders outboundHeaders,
            byte[] body) {
        URI targetUri = URI.create(trimTrailingSlash(target.baseUrl()) + normalizePath(route.endpoint()));
        // retrieve/toEntity는 4xx/5xx를 RestClientResponseException으로 변환합니다.
        // ServiceCallEngine이 5xx/timeout/429만 retry/failover하고 일반 4xx는 즉시 종료합니다.
        return restClient.method(httpMethod(route.httpMethod()))
                .uri(targetUri)
                .headers(headers -> headers.putAll(outboundHeaders))
                .body(body == null ? new byte[0] : body)
                .retrieve()
                .toEntity(byte[].class);
    }

    private ResponseEntity<byte[]> withGatewayResponseHeaders(
            ResponseEntity<byte[]> downstream, CpfGatewayRoute route) {
        HttpHeaders responseHeaders = new HttpHeaders();
        downstream.getHeaders().forEach((name, values) -> {
            if (!HOP_BY_HOP.contains(name.toLowerCase(Locale.ROOT))) {
                responseHeaders.put(name, values);
            }
        });
        responseHeaders.set(CpfHeaderNames.GATEWAY_INSTANCE_ID, CpfInstanceIdentity.current().serverInstanceId());
        responseHeaders.set(CpfHeaderNames.GATEWAY_ROUTE_ID, route.standardExecutionId());
        responseHeaders.set(CpfHeaderNames.GATEWAY_ROUTE_VERSION, route.routeVersion());
        return new ResponseEntity<>(downstream.getBody(), responseHeaders, downstream.getStatusCode());
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
        result.set(CpfHeaderNames.GATEWAY_INSTANCE_ID, CpfInstanceIdentity.current().serverInstanceId());
        result.set(CpfHeaderNames.GATEWAY_ROUTE_ID, route.standardExecutionId());
        result.set(CpfHeaderNames.GATEWAY_ROUTE_VERSION, route.routeVersion());
        result.set(CpfHeaderNames.INGRESS_TYPE, "CPF_GATEWAY");
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
