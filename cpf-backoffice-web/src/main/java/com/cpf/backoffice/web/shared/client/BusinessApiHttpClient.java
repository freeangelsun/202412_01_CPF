package com.cpf.backoffice.web.shared.client;

import com.cpf.backoffice.web.shared.config.BackofficeWebProperties;
import com.cpf.backoffice.web.shared.protocol.CanonicalTransactionHeaders;
import com.cpf.backoffice.web.shared.protocol.ChannelTransactionIdGenerator;
import com.cpf.backoffice.web.shared.routing.BackofficeOperationRouteCatalog;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Backoffice Web의 공식 HTTP Channel client입니다.
 * Browser credential/Header를 그대로 신뢰하지 않고 Canonical Context와 인증 credential을 BFF가 소유합니다.
 */
@Component
public final class BusinessApiHttpClient {
    private static final Set<String> REQUEST_RELAY_HEADERS = Set.of(
            "content-type", "accept", "accept-language", "user-agent", "if-match", "if-none-match", "x-request-id", "idempotency-key");
    private static final Set<String> RESPONSE_RELAY_HEADERS = Set.of(
            "content-type", "content-disposition", "cache-control", "etag", "location", "retry-after");

    private final HttpClient httpClient;
    private final BackofficeWebProperties properties;
    private final BackofficeOperationRouteCatalog routes;
    private final ChannelTransactionIdGenerator transactionIds;

    public BusinessApiHttpClient(HttpClient httpClient, BackofficeWebProperties properties, BackofficeOperationRouteCatalog routes) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.routes = routes;
        this.transactionIds = ChannelTransactionIdGenerator.runtime(properties.callerSystemCode());
    }

    public ResponseEntity<byte[]> forward(HttpServletRequest request) throws IOException, InterruptedException {
        return forward(request, null, Map.of());
    }

    public ResponseEntity<byte[]> forward(HttpServletRequest request, byte[] bodyOverride, Map<String, String> bffHeaders)
            throws IOException, InterruptedException {
        String path = request.getRequestURI();
        BackofficeOperationRouteCatalog.Route route = routes.require(request.getMethod(), path);
        HttpRequest upstreamRequest = buildRequest(request, route, upstreamUri(path, request.getQueryString()), bodyOverride, bffHeaders);
        HttpResponse<byte[]> response = httpClient.send(upstreamRequest, HttpResponse.BodyHandlers.ofByteArray());
        return toResponseEntity(response);
    }

    private HttpRequest buildRequest(HttpServletRequest servletRequest, BackofficeOperationRouteCatalog.Route route,
                                     URI upstream, byte[] bodyOverride, Map<String, String> bffHeaders) throws IOException {
        byte[] body = bodyOverride == null ? servletRequest.getInputStream().readAllBytes() : bodyOverride;
        HttpRequest.BodyPublisher publisher = body.length == 0 ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofByteArray(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder(upstream).timeout(properties.requestTimeout()).method(servletRequest.getMethod(), publisher);
        relayRequestHeaders(servletRequest, builder);
        String accessToken = cookie(servletRequest, properties.accessCookieName());
        if (accessToken != null && !accessToken.isBlank()) builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        bffHeaders.forEach((name, value) -> { if (value != null && !value.isBlank()) builder.header(name, value); });
        addCanonicalTransactionHeaders(builder, route.operationId());
        return builder.build();
    }

    private void relayRequestHeaders(HttpServletRequest servletRequest, HttpRequest.Builder builder) {
        var names = servletRequest.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (!REQUEST_RELAY_HEADERS.contains(name.toLowerCase(Locale.ROOT))) continue;
            var values = servletRequest.getHeaders(name);
            while (values.hasMoreElements()) builder.header(name, values.nextElement());
        }
    }

    private void addCanonicalTransactionHeaders(HttpRequest.Builder builder, String operationId) {
        String transactionId = transactionIds.next();
        builder.header(CanonicalTransactionHeaders.TRANSACTION_ID, transactionId);
        builder.header(CanonicalTransactionHeaders.ORIGINAL_SYSTEM_CODE, properties.callerSystemCode());
        builder.header(CanonicalTransactionHeaders.SYSTEM_CODE, properties.targetSystemCode());
        builder.header(CanonicalTransactionHeaders.CALLER_SYSTEM_CODE, properties.callerSystemCode());
        builder.header(CanonicalTransactionHeaders.TARGET_SYSTEM_CODE, properties.targetSystemCode());
        builder.header(CanonicalTransactionHeaders.TARGET_OPERATION_ID, operationId);
        if (properties.callerChannel() != null && !properties.callerChannel().isBlank()) {
            builder.header(CanonicalTransactionHeaders.CALLER_CHANNEL, properties.callerChannel());
        }
    }

    private ResponseEntity<byte[]> toResponseEntity(HttpResponse<byte[]> response) {
        HttpHeaders outgoing = new HttpHeaders();
        response.headers().map().forEach((name, values) -> {
            if (RESPONSE_RELAY_HEADERS.contains(name.toLowerCase(Locale.ROOT))) outgoing.put(name, List.copyOf(values));
        });
        return new ResponseEntity<>(response.body(), outgoing, HttpStatusCode.valueOf(response.statusCode()));
    }

    private URI upstreamUri(String path, String query) {
        URI base = properties.selectedBaseUri();
        String baseText = base.toString().replaceAll("/+$", "");
        return URI.create(baseText + path + (query == null || query.isBlank() ? "" : "?" + query));
    }

    public static String cookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) if (name.equals(cookie.getName())) return cookie.getValue();
        return null;
    }
}
