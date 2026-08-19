package com.cpf.bzachannel.shared.client;

import com.cpf.bzachannel.shared.config.BzaChannelProperties;
import com.cpf.bzachannel.shared.protocol.CanonicalTransactionHeaders;
import com.cpf.bzachannel.shared.protocol.ChannelTransactionIdGenerator;
import com.cpf.bzachannel.shared.routing.BzaOperationRouteCatalog;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public final class BusinessApiHttpClient {
    private static final Set<String> REQUEST_RELAY_HEADERS = Set.of(
            "authorization", "cookie", "content-type", "accept", "accept-language", "if-match", "if-none-match",
            "x-csrf-token", "x-xsrf-token", "x-request-id");
    private static final Set<String> RESPONSE_RELAY_HEADERS = Set.of(
            "content-type", "content-disposition", "cache-control", "etag", "location", "retry-after", "set-cookie");

    private final HttpClient httpClient;
    private final BzaChannelProperties properties;
    private final BzaOperationRouteCatalog routes;
    private final ChannelTransactionIdGenerator transactionIds;

    public BusinessApiHttpClient(HttpClient httpClient, BzaChannelProperties properties, BzaOperationRouteCatalog routes) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.routes = routes;
        this.transactionIds = ChannelTransactionIdGenerator.runtime(properties.callerSystemCode());
    }

    public ResponseEntity<byte[]> forward(HttpServletRequest request) throws IOException, InterruptedException {
        String path = request.getRequestURI();
        BzaOperationRouteCatalog.Route route = routes.require(request.getMethod(), path);
        HttpRequest upstreamRequest = buildRequest(request, route, upstreamUri(path, request.getQueryString()));
        HttpResponse<byte[]> response = httpClient.send(upstreamRequest, HttpResponse.BodyHandlers.ofByteArray());
        return toResponseEntity(response);
    }

    private HttpRequest buildRequest(
            HttpServletRequest servletRequest,
            BzaOperationRouteCatalog.Route route,
            URI upstream) throws IOException {
        byte[] body = servletRequest.getInputStream().readAllBytes();
        HttpRequest.BodyPublisher publisher = body.length == 0
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofByteArray(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder(upstream)
                .timeout(properties.requestTimeout())
                .method(servletRequest.getMethod(), publisher);

        relayRequestHeaders(servletRequest, builder);
        addCanonicalTransactionHeaders(builder, route.operationId());
        return builder.build();
    }

    private void relayRequestHeaders(HttpServletRequest servletRequest, HttpRequest.Builder builder) {
        var names = servletRequest.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (!REQUEST_RELAY_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                continue;
            }
            var values = servletRequest.getHeaders(name);
            while (values.hasMoreElements()) {
                builder.header(name, values.nextElement());
            }
        }
    }

    private void addCanonicalTransactionHeaders(HttpRequest.Builder builder, String operationId) {
        builder.header(CanonicalTransactionHeaders.TRANSACTION_ID, transactionIds.next());
        builder.header(CanonicalTransactionHeaders.ORIGINAL_SYSTEM_CODE, properties.callerSystemCode());
        builder.header(CanonicalTransactionHeaders.CALLER_SYSTEM_CODE, properties.callerSystemCode());
        builder.header(CanonicalTransactionHeaders.TARGET_SYSTEM_CODE, properties.targetSystemCode());
        builder.header(CanonicalTransactionHeaders.TARGET_OPERATION_ID, operationId);
        if (properties.callerChannel() != null && !properties.callerChannel().isBlank()) {
            builder.header(CanonicalTransactionHeaders.CALLER_CHANNEL, properties.callerChannel());
        }
        // X-System-Code is receiver-owned and must never be authored by the external BZA Channel.
    }

    private ResponseEntity<byte[]> toResponseEntity(HttpResponse<byte[]> response) {
        HttpHeaders outgoing = new HttpHeaders();
        response.headers().map().forEach((name, values) -> {
            if (RESPONSE_RELAY_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                outgoing.put(name, List.copyOf(values));
            }
        });
        return new ResponseEntity<>(response.body(), outgoing, HttpStatusCode.valueOf(response.statusCode()));
    }

    private URI upstreamUri(String path, String query) {
        URI base = properties.selectedBaseUri();
        String baseText = base.toString().replaceAll("/+$", "");
        return URI.create(baseText + path + (query == null || query.isBlank() ? "" : "?" + query));
    }
}
