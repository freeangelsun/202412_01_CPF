package com.cpf.gateway.scg;

import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;

import com.cpf.core.api.gateway.CpfGatewayAuditEvent;
import com.cpf.core.api.gateway.CpfGatewayAuthenticationPort;
import com.cpf.core.api.gateway.CpfGatewayAuthorizationPort;
import com.cpf.core.api.gateway.CpfGatewayLedgerPort;
import com.cpf.core.api.gateway.CpfGatewayPrincipal;
import com.cpf.core.api.gateway.CpfGatewayRoute;
import com.cpf.gateway.config.CpfGatewaySafetyProperties;
import com.cpf.gateway.route.CpfGatewayPathRewriter;
import com.cpf.gateway.route.CpfGatewayRouteSnapshot;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/** SCG MVC Data Plane에 body replay, Header trust boundary, durable recovery를 적용합니다. */
@Component
public final class CpfScgPrimaryHandler implements HandlerFunction<ServerResponse> {
    public static final String TX_ATTR = "cpf.gateway.tx";
    public static final String TARGET_ATTR = "cpf.gateway.target";
    public static final String START_ATTR = "cpf.gateway.start";
    public static final String PRINCIPAL_ATTR = "cpf.gateway.principal";
    public static final String REASON_ATTR = "cpf.gateway.reason";
    public static final String BODY_HASH_ATTR = "cpf.gateway.bodyHash";
    public static final String EXECUTION_ATTR = "cpf.gateway.executionId";
    public static final String ROUTE_ATTR = "cpf.gateway.routeId";

    private final CpfGatewayRouteSnapshot snapshot;
    private final CpfScgTargetResolver targets;
    private final CpfGatewayAuthenticationPort authentication;
    private final CpfGatewayAuthorizationPort authorization;
    private final CpfGatewayAuditRecoverySpool auditRecovery;
    private final CpfGatewayLedgerRecoverySpool ledgerRecovery;
    private final CircuitBreakerFactory<?, ?> circuitBreakers;
    private final CpfGatewaySafetyProperties safety;

    public CpfScgPrimaryHandler(
            CpfGatewayRouteSnapshot snapshot,
            CpfScgTargetResolver targets,
            CpfGatewayAuthenticationPort authentication,
            CpfGatewayAuthorizationPort authorization,
            CpfGatewayAuditRecoverySpool auditRecovery,
            CpfGatewayLedgerRecoverySpool ledgerRecovery,
            CircuitBreakerFactory<?, ?> circuitBreakers,
            CpfGatewaySafetyProperties safety) {
        this.snapshot = snapshot;
        this.targets = targets;
        this.authentication = authentication;
        this.authorization = authorization;
        this.auditRecovery = auditRecovery;
        this.ledgerRecovery = ledgerRecovery;
        this.circuitBreakers = circuitBreakers;
        this.safety = safety;
    }

    @Override
    public ServerResponse handle(ServerRequest request) throws Exception {
        String environment = safety.getEnvironmentCode();
        String host = request.headers().firstHeader(HttpHeaders.HOST);
        String path = request.uri().getRawPath();
        String version = request.headers().firstHeader("X-Api-Version");
        CpfGatewayRoute route = snapshot.resolveRequest(
                environment, host, path, request.methodName(), version);

        Map<String, String> trusted = trustedHeaders(request);
        CpfGatewayPrincipal principal = authentication.authenticate(route, trusted);
        if (!principal.authenticated()) {
            throw new SecurityException("Gateway authentication failed");
        }
        trusted.put("cpf.principalId", principal.principalId());
        if (!authorization.isAllowed(route, Map.copyOf(trusted))) {
            throw new SecurityException("Gateway authorization denied");
        }
        String reason = trusted.get("x-operation-reason");
        if (route.auditReasonRequired() && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException("위험 Gateway 호출 사유가 필요합니다.");
        }

        BodyPlan body = bodyPlan(request, route, safety.getRequestBodyBytesCap());
        int maxAttempts = body.replaySafe()
                ? Math.min(route.maxRetryCount() + 1, safety.getRetryCountCap() + 1)
                : 1;
        String tx = UUID.randomUUID().toString();
        String transactionId = trustedTransactionId(trusted.get("x-transaction-id"));
        String targetPath = CpfGatewayPathRewriter.rewrite(
                route.pathPattern(), route.targetPath(), path);
        OffsetDateTime started = OffsetDateTime.now();

        ledgerRecovery.begin(new CpfGatewayLedgerPort.TransactionStart(
                tx,
                transactionId,
                validatedTrace(trusted.get("traceparent")),
                value(trusted.get("x-channel-id"), ""),
                sourceIp(request),
                sourcePort(request),
                safety.getInstanceId(),
                route.routeId(),
                route.routeId(),
                route.routeVersion(),
                route.expectedVersion(),
                route.routeKey(),
                route.serverGroupId(),
                request.methodName(),
                path,
                body.requestBytes(),
                started));

        request.servletRequest().setAttribute(TX_ATTR, tx);
        request.servletRequest().setAttribute(START_ATTR, started);
        request.servletRequest().setAttribute(PRINCIPAL_ATTR, principal.principalId());
        request.servletRequest().setAttribute(REASON_ATTR, reason);
        request.servletRequest().setAttribute(BODY_HASH_ATTR, body.bodyHash());
        request.servletRequest().setAttribute(EXECUTION_ATTR, route.standardExecutionId());
        request.servletRequest().setAttribute(ROUTE_ATTR, route.routeId());
        auditRecovery.record(new CpfGatewayAuditEvent(
                tx,
                route.standardExecutionId(),
                principal.principalId(),
                reason,
                "BEFORE",
                "ACCEPTED",
                null,
                null,
                java.time.Instant.now(),
                Map.of(
                        "routeId", route.routeId(),
                        "bodySha256", body.bodyHash(),
                        "bodyMode", body.buffered() ? "BUFFERED_REPLAY" : "STREAMING_SINGLE_ATTEMPT")));

        var circuitBreaker = circuitBreakers.create("gateway-" + route.routeId());
        Throwable lastFailure = null;
        for (int attemptNo = 1; attemptNo <= maxAttempts; attemptNo++) {
            OffsetDateTime attemptStarted = OffsetDateTime.now();
            CpfScgTargetResolver.Target target = targets.resolve(
                    route, targetPath, request.uri().getRawQuery());
            request.servletRequest().setAttribute(TARGET_ATTR, target.instanceId());
            URI uri = target.uri();
            ServerRequest upstreamRequest = upstreamRequest(
                    request, body, trusted, principal, transactionId, tx, route, target);
            try {
                ServerResponse response = circuitBreaker.run(() -> {
                    try {
                        return CpfGatewayPinnedAddressContext.call(
                                target.uri().getHost(),
                                target.pinnedAddress(),
                                () -> http().handle(upstreamRequest));
                    } catch (Exception failure) {
                        throw new GatewayUpstreamException(failure);
                    }
                });
                boolean retryableStatus = body.replaySafe()
                        && retryableStatus(response.statusCode().value())
                        && attemptNo < maxAttempts;
                ledgerRecovery.recordAttempt(attempt(
                        tx,
                        attemptNo,
                        target,
                        uri,
                        attemptStarted,
                        retryableStatus ? "RETRYABLE_FAILURE" : "SUCCESS",
                        Integer.toString(response.statusCode().value()),
                        retryableStatus ? "UPSTREAM_RETRYABLE_STATUS" : null,
                        null,
                        false));
                if (!retryableStatus) {
                    return response;
                }
                pause(attemptNo);
            } catch (Throwable raw) {
                Throwable failure = unwrap(raw);
                lastFailure = failure;
                boolean retryable = body.replaySafe()
                        && retryable(failure)
                        && attemptNo < maxAttempts;
                boolean unknown = unknownResult(failure);
                ledgerRecovery.recordAttempt(attempt(
                        tx,
                        attemptNo,
                        target,
                        uri,
                        attemptStarted,
                        retryable ? "RETRYABLE_FAILURE" : "FAILED",
                        "",
                        "UPSTREAM_CALL_FAILED",
                        safe(failure.getMessage()),
                        unknown));
                if (!retryable) {
                    if (failure instanceof Exception exception) {
                        throw exception;
                    }
                    throw new IllegalStateException(failure);
                }
                pause(attemptNo);
            }
        }
        throw new IllegalStateException("Gateway retry attempts exhausted", lastFailure);
    }


    private ServerRequest upstreamRequest(
            ServerRequest original,
            BodyPlan body,
            Map<String, String> trusted,
            CpfGatewayPrincipal principal,
            String transactionId,
            String gatewayTransactionId,
            CpfGatewayRoute route,
            CpfScgTargetResolver.Target target) {
        ServerRequest bodyRequest;
        if (body.buffered()) {
            bodyRequest = ServerRequest.from(original).body(body.bytes()).build();
        } else {
            HttpServletRequest bounded = new BoundedBodyRequest(
                    original.servletRequest(), safety.getRequestBodyBytesCap());
            bodyRequest = ServerRequest.create(bounded, original.messageConverters());
        }

        return ServerRequest.from(bodyRequest)
                .headers(headers -> {
                    headers.clear();
                    trusted.forEach((name, value) -> {
                        if (value != null && !value.isBlank()) {
                            headers.set(name, value);
                        }
                    });
                    headers.remove(HttpHeaders.HOST);
                    headers.set(HttpHeaders.HOST, target.authorityHeader());
                    headers.remove(HttpHeaders.COOKIE);
                    headers.remove(HttpHeaders.CONTENT_LENGTH);
                    headers.set("X-Cpf-Principal-Id", principal.principalId());
                    headers.set("X-Cpf-Transaction-Id", transactionId);
                    headers.set("X-Cpf-Gateway-Transaction-Id", gatewayTransactionId);
                    headers.set("X-Cpf-Gateway-Route-Id", route.routeId());
                    headers.set("X-Cpf-Gateway-Instance-Id", safety.getInstanceId());
                })
                .cookies(cookies -> cookies.clear())
                .attribute(MvcUtils.GATEWAY_REQUEST_URL_ATTR, target.uri())
                .build();
    }

    private Map<String, String> trustedHeaders(ServerRequest request) {
        Map<String, String> values = new LinkedHashMap<>();
        int count = 0;
        int bytes = 0;
        for (var entry : request.headers().asHttpHeaders().entrySet()) {
            String lower = entry.getKey().toLowerCase(Locale.ROOT);
            if (lower.startsWith("x-cpf-") || lower.startsWith("x-forwarded-")) {
                throw new SecurityException("Untrusted internal/proxy header: " + entry.getKey());
            }
            if (entry.getValue().isEmpty()) {
                continue;
            }
            if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(entry.getKey())) {
                String credential = entry.getValue().getFirst();
                if (credential.length() > 8192
                        || credential.indexOf('\r') >= 0
                        || credential.indexOf('\n') >= 0) {
                    throw new SecurityException("Gateway authorization header denied");
                }
                count++;
                bytes += lower.length() + credential.length();
                if (count > safety.getHeaderCountCap() || bytes > safety.getHeaderBytesCap()) {
                    throw new IllegalArgumentException("Gateway trusted header budget exceeded");
                }
                values.put("authorization", credential);
                continue;
            }
            if (!safety.getTrustedContextHeaders().contains(lower)) {
                continue;
            }
            String headerValue = entry.getValue().getFirst();
            if ("traceparent".equals(lower) && validatedTrace(headerValue).isEmpty()) {
                throw new SecurityException("Gateway traceparent header denied");
            }
            if (headerValue.indexOf('\r') >= 0 || headerValue.indexOf('\n') >= 0) {
                throw new SecurityException("Gateway trusted header contains control characters");
            }
            count++;
            bytes += lower.length() + headerValue.length();
            if (count > safety.getHeaderCountCap() || bytes > safety.getHeaderBytesCap()) {
                throw new IllegalArgumentException("Gateway trusted header budget exceeded");
            }
            values.put(lower, headerValue);
        }
        return values;
    }

    private static BodyPlan bodyPlan(
            ServerRequest request,
            CpfGatewayRoute route,
            long cap) throws IOException {
        String method = request.methodName();
        long declared = request.headers().contentLength().orElse(-1);
        MediaType type = request.headers().contentType().orElse(MediaType.APPLICATION_OCTET_STREAM);
        boolean bodyless = "GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method);
        boolean streamLike = MediaType.MULTIPART_FORM_DATA.isCompatibleWith(type)
                || MediaType.APPLICATION_OCTET_STREAM.isCompatibleWith(type)
                || MediaType.TEXT_EVENT_STREAM.isCompatibleWith(type);
        String idempotencyKey = request.headers().firstHeader("Idempotency-Key");
        boolean replaySafe = bodyless || (route.idempotent()
                && idempotencyKey != null
                && idempotencyKey.matches("[A-Za-z0-9._:-]{8,128}")
                && !streamLike);
        if (!replaySafe) {
            return new BodyPlan(null, Math.max(0L, declared), "UNBUFFERED", false);
        }
        byte[] bytes = readBodyBounded(request, cap);
        return new BodyPlan(bytes, bytes.length, sha256(bytes), true);
    }

    private static byte[] readBodyBounded(ServerRequest request, long configuredCap) throws IOException {
        long cap = Math.min(configuredCap, Integer.MAX_VALUE);
        long declared = request.headers().contentLength().orElse(-1);
        if (declared > cap) {
            throw new IllegalArgumentException("Gateway request body exceeds configured cap");
        }
        try (InputStream input = request.servletRequest().getInputStream();
                ByteArrayOutputStream output = new ByteArrayOutputStream(
                        declared > 0 ? Math.toIntExact(declared) : 1024)) {
            byte[] buffer = new byte[8192];
            long total = 0;
            for (int read; (read = input.read(buffer)) >= 0; ) {
                total += read;
                if (total > cap) {
                    throw new IllegalArgumentException("Gateway request body exceeds configured cap");
                }
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    private static CpfGatewayLedgerPort.Attempt attempt(
            String tx,
            int attemptNo,
            CpfScgTargetResolver.Target target,
            URI uri,
            OffsetDateTime started,
            String status,
            String protocolStatus,
            String failureCode,
            String failureMessage,
            boolean unknown) {
        OffsetDateTime finished = OffsetDateTime.now();
        int port = uri.getPort() > 0
                ? uri.getPort()
                : ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80);
        String attemptId = UUID.nameUUIDFromBytes(
                        (tx + ":" + attemptNo).getBytes(StandardCharsets.UTF_8))
                .toString();
        return new CpfGatewayLedgerPort.Attempt(
                attemptId,
                tx,
                attemptNo,
                target.instanceId(),
                uri.getHost(),
                port,
                uri.getScheme(),
                0,
                Duration.between(started, finished).toMillis(),
                status,
                protocolStatus,
                failureCode,
                failureMessage,
                "gateway",
                "SCG_SERVICE_REGISTRY",
                unknown,
                started,
                finished);
    }

    private static boolean retryableStatus(int status) {
        return status == 502 || status == 503 || status == 504;
    }

    private static boolean retryable(Throwable failure) {
        Throwable value = unwrap(failure);
        return value instanceof IOException || value instanceof TimeoutException;
    }

    private static boolean unknownResult(Throwable failure) {
        Throwable value = unwrap(failure);
        if (value instanceof ConnectException || value instanceof UnknownHostException) {
            return false;
        }
        return value instanceof SocketTimeoutException
                || value instanceof TimeoutException
                || value instanceof EOFException
                || value instanceof SocketException;
    }

    private static Throwable unwrap(Throwable failure) {
        Throwable value = failure;
        while ((value instanceof GatewayUpstreamException
                        || value instanceof java.util.concurrent.CompletionException)
                && value.getCause() != null) {
            value = value.getCause();
        }
        return value;
    }

    private static void pause(int attemptNo) {
        try {
            Thread.sleep(Math.min(500L, 50L * attemptNo));
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gateway retry interrupted", interrupted);
        }
    }

    private static String trustedTransactionId(String value) {
        return value != null && value.matches("[A-Za-z0-9._:-]{1,100}")
                ? value
                : UUID.randomUUID().toString();
    }

    private static String validatedTrace(String value) {
        return value != null && value.matches("[0-9a-f]{2}-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}")
                ? value
                : "";
    }

    private static String sourceIp(ServerRequest request) {
        return request.remoteAddress().map(value -> value.getAddress().getHostAddress()).orElse("");
    }

    private static int sourcePort(ServerRequest request) {
        return request.remoteAddress().map(value -> value.getPort()).orElse(0);
    }

    private static String value(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        String cleaned = value.replaceAll(
                "(?i)(token|password|secret|authorization)[=: ]+[^&\\s]+", "$1=***");
        return cleaned.length() > 500 ? cleaned.substring(0, 500) : cleaned;
    }

    private static String sha256(byte[] bytes) {
        try {
            return java.util.HexFormat.of().formatHex(
                    java.security.MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (Exception failure) {
            throw new IllegalStateException(failure);
        }
    }


    private static final class BoundedBodyRequest extends HttpServletRequestWrapper {
        private final long cap;
        private ServletInputStream input;
        private BufferedReader reader;

        private BoundedBodyRequest(HttpServletRequest request, long cap) {
            super(request);
            if (cap < 0) {
                throw new IllegalArgumentException("Gateway request body cap must not be negative");
            }
            this.cap = cap;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (reader != null) {
                throw new IllegalStateException("getReader already called");
            }
            if (input == null) {
                input = new BoundedServletInputStream(super.getInputStream(), cap);
            }
            return input;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            if (input != null) {
                throw new IllegalStateException("getInputStream already called");
            }
            if (reader == null) {
                String encoding = getCharacterEncoding();
                Charset charset = encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding);
                input = new BoundedServletInputStream(super.getInputStream(), cap);
                reader = new BufferedReader(new InputStreamReader(input, charset));
            }
            return reader;
        }
    }

    private static final class BoundedServletInputStream extends ServletInputStream {
        private final ServletInputStream delegate;
        private final long cap;
        private long readBytes;

        private BoundedServletInputStream(ServletInputStream delegate, long cap) {
            this.delegate = delegate;
            this.cap = cap;
        }

        @Override
        public int read() throws IOException {
            int value = delegate.read();
            if (value >= 0) {
                requireBudget(1);
            }
            return value;
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            int read = delegate.read(bytes, offset, length);
            if (read > 0) {
                requireBudget(read);
            }
            return read;
        }

        private void requireBudget(int increment) throws IOException {
            readBytes += increment;
            if (readBytes > cap) {
                throw new IOException("Gateway streaming request body exceeds configured cap");
            }
        }

        @Override public boolean isFinished() { return delegate.isFinished(); }
        @Override public boolean isReady() { return delegate.isReady(); }
        @Override public void setReadListener(ReadListener listener) { delegate.setReadListener(listener); }
        @Override public void close() throws IOException { delegate.close(); }
    }

    private record BodyPlan(byte[] bytes, long requestBytes, String bodyHash, boolean replaySafe) {
        boolean buffered() {
            return bytes != null;
        }
    }

    private static final class GatewayUpstreamException extends RuntimeException {
        GatewayUpstreamException(Throwable cause) {
            super(cause);
        }
    }
}
