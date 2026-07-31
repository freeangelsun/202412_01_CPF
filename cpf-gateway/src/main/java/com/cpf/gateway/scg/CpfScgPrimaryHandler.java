package com.cpf.gateway.scg;

import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

import com.cpf.core.api.gateway.CpfGatewayAuditEvent;
import com.cpf.core.api.gateway.CpfGatewayAuditPort;
import com.cpf.core.api.gateway.CpfGatewayAuthenticationPort;
import com.cpf.core.api.gateway.CpfGatewayAuthorizationPort;
import com.cpf.core.api.gateway.CpfGatewayLedgerPort;
import com.cpf.core.api.gateway.CpfGatewayPrincipal;
import com.cpf.core.api.gateway.CpfGatewayRoute;
import com.cpf.gateway.route.CpfGatewayPathRewriter;
import com.cpf.gateway.route.CpfGatewayRouteSnapshot;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/** Spring Cloud Gateway Server Web MVC가 실제 Data Plane을 소유하고 CPF는 정책·감사·원장만 확장합니다. */
@Component
public final class CpfScgPrimaryHandler implements HandlerFunction<ServerResponse> {
    public static final String TX_ATTR = "cpf.gateway.tx";
    public static final String TARGET_ATTR = "cpf.gateway.target";
    public static final String START_ATTR = "cpf.gateway.start";

    private final CpfGatewayRouteSnapshot snapshot;
    private final CpfScgTargetResolver targets;
    private final CpfGatewayAuthenticationPort authentication;
    private final CpfGatewayAuthorizationPort authorization;
    private final CpfGatewayAuditPort audit;
    private final CpfGatewayLedgerPort ledger;
    private final CircuitBreakerFactory<?, ?> circuitBreakers;

    public CpfScgPrimaryHandler(CpfGatewayRouteSnapshot snapshot, CpfScgTargetResolver targets,
            CpfGatewayAuthenticationPort authentication, CpfGatewayAuthorizationPort authorization,
            CpfGatewayAuditPort audit, CpfGatewayLedgerPort ledger,
            CircuitBreakerFactory<?, ?> circuitBreakers) {
        this.snapshot = snapshot; this.targets = targets; this.authentication = authentication;
        this.authorization = authorization; this.audit = audit; this.ledger = ledger;
        this.circuitBreakers = circuitBreakers;
    }

    @Override
    public ServerResponse handle(ServerRequest request) throws Exception {
        String environment = request.headers().firstHeader("X-Environment-Code");
        String host = request.headers().firstHeader(HttpHeaders.HOST);
        String path = request.uri().getRawPath();
        String version = request.headers().firstHeader("X-Api-Version");
        CpfGatewayRoute route = snapshot.resolveRequest(environment, host, path, request.methodName(), version);
        Map<String, String> trusted = headers(request);
        CpfGatewayPrincipal principal = authentication.authenticate(route, trusted);
        if (!principal.authenticated()) throw new SecurityException("Gateway authentication failed");
        trusted.put("cpf.principalId", principal.principalId());
        if (!authorization.isAllowed(route, Map.copyOf(trusted))) throw new SecurityException("Gateway authorization denied");
        String reason = request.headers().firstHeader("X-Operation-Reason");
        if (route.auditReasonRequired() && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException("위험 Gateway 호출 사유가 필요합니다.");
        }

        String tx = UUID.randomUUID().toString();
        String targetPath = CpfGatewayPathRewriter.rewrite(route.pathPattern(), route.targetPath(), path);
        OffsetDateTime started = OffsetDateTime.now();
        ledger.begin(new CpfGatewayLedgerPort.TransactionStart(tx, header(request, "X-Transaction-Id", tx),
                header(request, "traceparent", ""), header(request, "X-Channel-Id", ""),
                request.remoteAddress().map(a -> a.getAddress().getHostAddress()).orElse(""),
                request.remoteAddress().map(a -> a.getPort()).orElse(0),
                header(request, "X-Instance-Id", "gateway"), route.routeId(), route.routeId(),
                route.routeVersion(), route.expectedVersion(), route.routeKey(), route.serverGroupId(),
                request.methodName(), path, request.headers().contentLength().orElse(0), started));
        request.servletRequest().setAttribute(TX_ATTR, tx);
        request.servletRequest().setAttribute(START_ATTR, started);
        audit.record(new CpfGatewayAuditEvent(tx, route.standardExecutionId(), principal.principalId(), reason,
                "BEFORE", "ACCEPTED", null, null, java.time.Instant.now(), Map.of("routeId", route.routeId())));

        var circuitBreaker = circuitBreakers.create("gateway-" + route.routeId());
        Retry retry = Retry.of("gateway-" + route.routeId(), RetryConfig.custom()
                .maxAttempts(route.idempotent() ? route.maxRetryCount() + 1 : 1)
                .waitDuration(Duration.ofMillis(50))
                .retryExceptions(java.io.IOException.class, java.util.concurrent.TimeoutException.class)
                .build());
        AtomicInteger attempt = new AtomicInteger();
        try {
            return Retry.decorateCheckedSupplier(retry, () -> {
                int attemptNo = attempt.incrementAndGet();
                OffsetDateTime attemptStarted = OffsetDateTime.now();
                CpfScgTargetResolver.Target target = targets.resolve(route, targetPath, request.uri().getRawQuery());
                request.servletRequest().setAttribute(TARGET_ATTR, target.instanceId());
                URI uri = target.uri();
                Throwable failure = null;
                try {
                    ServerResponse response = circuitBreaker.run(() -> http(uri).handle(request));
                    ledger.recordAttempt(attempt(tx, attemptNo, target, uri, attemptStarted, "SUCCESS",
                            Integer.toString(response.statusCode().value()), null, null, false));
                    return response;
                } catch (Throwable ex) {
                    failure = ex;
                    ledger.recordAttempt(attempt(tx, attemptNo, target, uri, attemptStarted, "FAILED", "",
                            "UPSTREAM_CALL_FAILED", safe(ex.getMessage()), true));
                    if (ex instanceof Exception exception) throw exception;
                    throw new IllegalStateException(ex);
                }
            }).get();
        } catch (Throwable ex) {
            if (ex instanceof Exception exception) throw exception;
            throw new IllegalStateException(ex);
        }
    }

    private static CpfGatewayLedgerPort.Attempt attempt(String tx, int attemptNo,
            CpfScgTargetResolver.Target target, URI uri, OffsetDateTime started, String status,
            String protocolStatus, String failureCode, String failureMessage, boolean unknown) {
        OffsetDateTime finished = OffsetDateTime.now();
        int port = uri.getPort() > 0 ? uri.getPort() : ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80);
        return new CpfGatewayLedgerPort.Attempt(UUID.randomUUID().toString(), tx, attemptNo,
                target.instanceId(), uri.getHost(), port, uri.getScheme(), 0,
                Duration.between(started, finished).toMillis(), status, protocolStatus,
                failureCode, failureMessage, "gateway", "SCG_SERVICE_REGISTRY", unknown, started, finished);
    }

    private static Map<String, String> headers(ServerRequest request) {
        Map<String, String> values = new LinkedHashMap<>();
        request.headers().asHttpHeaders().forEach((name, list) -> {
            if (!list.isEmpty() && !name.equalsIgnoreCase(HttpHeaders.AUTHORIZATION)) values.put(name, list.getFirst());
        });
        return values;
    }
    private static String header(ServerRequest request, String name, String fallback) {
        String value = request.headers().firstHeader(name); return value == null ? fallback : value;
    }
    private static String safe(String value) {
        if (value == null) return ""; String cleaned = value.replaceAll("(?i)(token|password|secret)=[^&\\s]+", "$1=***");
        return cleaned.length() > 500 ? cleaned.substring(0, 500) : cleaned;
    }
}
