package com.cpf.gateway.scg;

import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.cloud.gateway.server.mvc.filter.RetryFilterFunctions;

import com.cpf.gateway.api.CpfGatewayAuditEvent;
import com.cpf.gateway.api.CpfGatewayEntryPolicyPort;
import com.cpf.gateway.api.CpfGatewayAuthenticationPort;
import com.cpf.gateway.api.CpfGatewayAuthorizationPort;
import com.cpf.gateway.api.CpfGatewayLedgerPort;
import com.cpf.gateway.api.CpfGatewayPrincipal;
import com.cpf.gateway.api.CpfGatewayRateLimitPort;
import com.cpf.gateway.api.CpfGatewayRoute;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.transaction.CpfTransactionIds;
import com.cpf.foundation.id.DefaultCpfTransactionIdGenerator;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;

import com.cpf.gateway.context.*;
import com.cpf.web.context.*;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyDecision;
import com.cpf.platform.operations.channelregistry.application.CpfChannelPolicyService;
import com.cpf.platform.operations.channelregistry.model.CpfChannelPolicyDecision;
import com.cpf.gateway.config.CpfGatewaySafetyEnforcer;
import com.cpf.gateway.config.CpfGatewaySafetyProperties;
import com.cpf.gateway.logging.CpfGatewayCaptureService;
import com.cpf.gateway.route.CpfGatewayPathRewriter;
import com.cpf.gateway.route.CpfGatewayRouteSnapshot;
import com.cpf.gateway.runtime.CpfGatewayRuntimePolicy;
import com.cpf.gateway.runtime.DefaultCpfGatewayEntryPolicy;
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
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeoutException;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

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
    public static final String ROUTE_VERSION_ATTR = "cpf.gateway.routeVersion";
    public static final String LOG_POLICY_ATTR = "cpf.gateway.logPolicy";
    public static final String CORS_DECISION_ATTR = "cpf.gateway.corsDecision";
    public static final String RATE_LIMIT_DECISION_ATTR = "cpf.gateway.rateLimitDecision";

    private static final String EXECUTE_PATH = "/cpf/execute";
    private static final String LEGACY_PUBLIC_PREFIX = "/gateway/public";
    private static final Set<String> EXTERNAL_CPF_HEADERS = Set.of(
            CpfGatewayHeaderNames.EXECUTION_ROUTE_ID.toLowerCase(Locale.ROOT),
            CpfHttpHeaderNames.AUDIT_REASON.toLowerCase(Locale.ROOT),
            CpfHttpHeaderNames.IDEMPOTENCY_KEY.toLowerCase(Locale.ROOT));
    private static final Set<String> STANDARD_CONTEXT_HEADERS = Set.of(
            CpfGatewayHeaderNames.ORIGINAL_CLIENT_CHANNEL_CODE.toLowerCase(Locale.ROOT),
            CpfGatewayHeaderNames.CLIENT_CHANNEL_CODE.toLowerCase(Locale.ROOT),
            CpfHttpHeaderNames.REQUEST_TYPE.toLowerCase(Locale.ROOT),
            CpfHttpHeaderNames.IDEMPOTENCY_KEY.toLowerCase(Locale.ROOT));

    private final CpfGatewayRouteSnapshot snapshot;
    private final CpfScgTargetResolver targets;
    private final CpfGatewayAuthenticationPort authentication;
    private final CpfGatewayAuthorizationPort authorization;
    private final CpfChannelPolicyService channelPolicies;
    private final CpfGatewayRuntimePolicy runtimePolicy;
    private final CpfGatewayAuditRecoverySpool auditRecovery;
    private final CpfGatewayLedgerRecoverySpool ledgerRecovery;
    private final CpfGatewayCaptureService captureService;
    private final CircuitBreakerFactory<?, ?> circuitBreakers;
    private final CpfGatewaySafetyProperties safety;
    private final CpfGatewaySafetyEnforcer safetyEnforcer;
    private final CpfGatewayEntryPolicyPort entryPolicy;
    private final CpfGatewayContextFactory gatewayContexts;
    private final CpfTransactionIdGenerator transactionIds;
    private final CpfHttpOutboundContextAdapter httpContextOutbound;
    private final Clock clock;

    @Autowired
    public CpfScgPrimaryHandler(
            CpfGatewayRouteSnapshot snapshot,
            CpfScgTargetResolver targets,
            CpfGatewayAuthenticationPort authentication,
            CpfGatewayAuthorizationPort authorization,
            CpfChannelPolicyService channelPolicies,
            CpfGatewayRuntimePolicy runtimePolicy,
            CpfGatewayAuditRecoverySpool auditRecovery,
            CpfGatewayLedgerRecoverySpool ledgerRecovery,
            CpfGatewayCaptureService captureService,
            CircuitBreakerFactory<?, ?> circuitBreakers,
            CpfGatewaySafetyProperties safety,
            CpfGatewaySafetyEnforcer safetyEnforcer,
            CpfGatewayEntryPolicyPort entryPolicy,
            CpfTransactionIdGenerator transactionIds,
            CpfExecutionIdGenerator executionIds,
            CpfHttpOutboundContextAdapter httpContextOutbound,
            Clock clock) {
        this.snapshot = snapshot;
        this.targets = targets;
        this.authentication = authentication;
        this.authorization = authorization;
        this.channelPolicies = channelPolicies;
        this.runtimePolicy = runtimePolicy;
        this.auditRecovery = auditRecovery;
        this.ledgerRecovery = ledgerRecovery;
        this.captureService = captureService;
        this.circuitBreakers = circuitBreakers;
        this.safety = safety;
        this.safetyEnforcer = safetyEnforcer;
        this.entryPolicy = entryPolicy;
        this.transactionIds = Objects.requireNonNull(transactionIds, "transactionIds");
        this.gatewayContexts = new CpfGatewayContextFactory(executionIds);
        this.httpContextOutbound = Objects.requireNonNull(httpContextOutbound, "httpContextOutbound");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** Source compatibility용 생성자이며 제품 Runtime은 Clock Bean까지 주입하는 생성자를 사용합니다. */
    @Deprecated(forRemoval = false)
    public CpfScgPrimaryHandler(
            CpfGatewayRouteSnapshot snapshot,
            CpfScgTargetResolver targets,
            CpfGatewayAuthenticationPort authentication,
            CpfGatewayAuthorizationPort authorization,
            CpfChannelPolicyService channelPolicies,
            CpfGatewayRuntimePolicy runtimePolicy,
            CpfGatewayAuditRecoverySpool auditRecovery,
            CpfGatewayLedgerRecoverySpool ledgerRecovery,
            CpfGatewayCaptureService captureService,
            CircuitBreakerFactory<?, ?> circuitBreakers,
            CpfGatewaySafetyProperties safety,
            CpfGatewaySafetyEnforcer safetyEnforcer,
            CpfGatewayEntryPolicyPort entryPolicy) {
        this(snapshot, targets, authentication, authorization, channelPolicies, runtimePolicy,
                auditRecovery, ledgerRecovery, captureService, circuitBreakers, safety, safetyEnforcer,
                entryPolicy, fallbackTransactionIdGenerator(), fallbackExecutionIdGenerator(), new CpfHttpOutboundContextAdapter(), Clock.systemUTC());
    }

    /** 기존 15-인자 생성자 호환이며 제품 Runtime은 Clock Bean을 주입합니다. */
    @Deprecated(forRemoval = false)
    public CpfScgPrimaryHandler(
            CpfGatewayRouteSnapshot snapshot,
            CpfScgTargetResolver targets,
            CpfGatewayAuthenticationPort authentication,
            CpfGatewayAuthorizationPort authorization,
            CpfChannelPolicyService channelPolicies,
            CpfGatewayRuntimePolicy runtimePolicy,
            CpfGatewayAuditRecoverySpool auditRecovery,
            CpfGatewayLedgerRecoverySpool ledgerRecovery,
            CpfGatewayCaptureService captureService,
            CircuitBreakerFactory<?, ?> circuitBreakers,
            CpfGatewaySafetyProperties safety,
            CpfGatewaySafetyEnforcer safetyEnforcer,
            CpfGatewayEntryPolicyPort entryPolicy,
            CpfExecutionIdGenerator executionIds,
            CpfHttpOutboundContextAdapter httpContextOutbound) {
        this(snapshot, targets, authentication, authorization, channelPolicies, runtimePolicy,
                auditRecovery, ledgerRecovery, captureService, circuitBreakers, safety, safetyEnforcer,
                entryPolicy, fallbackTransactionIdGenerator(), executionIds, httpContextOutbound, Clock.systemUTC());
    }

    /** Source compatibility용 생성자이며 제품 Runtime은 Entry Policy Bean을 주입합니다. */
    @Deprecated(forRemoval = false)
    public CpfScgPrimaryHandler(
            CpfGatewayRouteSnapshot snapshot,
            CpfScgTargetResolver targets,
            CpfGatewayAuthenticationPort authentication,
            CpfGatewayAuthorizationPort authorization,
            CpfChannelPolicyService channelPolicies,
            CpfGatewayRuntimePolicy runtimePolicy,
            CpfGatewayAuditRecoverySpool auditRecovery,
            CpfGatewayLedgerRecoverySpool ledgerRecovery,
            CpfGatewayCaptureService captureService,
            CircuitBreakerFactory<?, ?> circuitBreakers,
            CpfGatewaySafetyProperties safety,
            CpfGatewaySafetyEnforcer safetyEnforcer) {
        this(snapshot, targets, authentication, authorization, channelPolicies, runtimePolicy,
                auditRecovery, ledgerRecovery, captureService, circuitBreakers, safety, safetyEnforcer,
                new DefaultCpfGatewayEntryPolicy(safety), fallbackTransactionIdGenerator(), fallbackExecutionIdGenerator(), new CpfHttpOutboundContextAdapter(), Clock.systemUTC());
    }

    @Override
    public ServerResponse handle(ServerRequest request) throws Exception {
        CpfGatewayEntryPolicyPort.Decision entryDecision = entryPolicy.evaluate(
                new CpfGatewayEntryPolicyPort.Request(
                        request.path(),
                        request.method().name(),
                        request.servletRequest().getProtocol(),
                        request.servletRequest().isSecure(),
                        request.servletRequest().getLocalPort(),
                        request.servletRequest().getRemoteAddr(),
                        clock.instant()));
        if (!entryDecision.allowed()) {
            return entryDenied(request, entryDecision);
        }
        ResolvedRoute resolved = resolveRoute(request);
        CpfGatewayRoute route = resolved.route();
        safetyEnforcer.validateRoute(route);
        long declaredLength = request.headers().contentLength().orElse(-1L);
        safetyEnforcer.validateRequest(request.headers().asHttpHeaders(), declaredLength);
        if (HttpMethod.OPTIONS.matches(request.method().name())) {
            return preflight(request, route);
        }

        LogPolicyDecision logPolicy = captureService.resolve(route.standardExecutionId());
        String tx = UUID.randomUUID().toString();
        String transactionId = CpfContexts.requireCurrent().transaction().transactionId();
        OffsetDateTime started = OffsetDateTime.now(clock);
        long ledgerRequestBytes = Math.max(0L, declaredLength);
        ledgerRecovery.begin(new CpfGatewayLedgerPort.TransactionStart(
                tx,
                transactionId,
                validatedTrace(request.headers().firstHeader("traceparent")),
                value(request.headers().firstHeader(CpfGatewayHeaderNames.CLIENT_CHANNEL_CODE), ""),
                sourceIp(request),
                sourcePort(request),
                com.cpf.platform.operations.api.runtime.CpfInstanceIdentity.current().instanceId(),
                route.routeId(),
                route.routeId(),
                route.routeVersion(),
                route.expectedVersion(),
                route.routeKey(),
                route.serverGroupId(),
                request.method().name(),
                resolved.inboundPath(),
                ledgerRequestBytes,
                started));

        request.servletRequest().setAttribute(TX_ATTR, tx);
        request.servletRequest().setAttribute(START_ATTR, started);
        request.servletRequest().setAttribute(EXECUTION_ATTR, route.standardExecutionId());
        request.servletRequest().setAttribute(ROUTE_ATTR, route.routeId());
        request.servletRequest().setAttribute(ROUTE_VERSION_ATTR, route.routeVersion());
        request.servletRequest().setAttribute(LOG_POLICY_ATTR, logPolicy);

        try {
            captureService.captureRequestMetadata(
                    tx,
                    request.uri().getRawQuery(),
                    request.headers().asHttpHeaders(),
                    logPolicy);

            CpfGatewayPrincipal principal = Objects.requireNonNullElse(
                    authentication.authenticate(route, credentialHeaders(request)),
                    CpfGatewayPrincipal.anonymous());
            Map<String, String> trusted = trustedHeaders(request);
            String channelId = value(
                    request.headers().firstHeader(CpfGatewayHeaderNames.CLIENT_CHANNEL_CODE), "");
            String tenantId = principal.authenticated()
                    ? value(principal.attributes().get("tenantId"), "")
                    : "";
            String clientId = principal.authenticated()
                    ? value(principal.attributes().get("clientId"), principal.principalId())
                    : sourceIp(request);
            CpfGatewayRateLimitPort.Decision rateDecision = runtimePolicy.acquire(
                    new CpfGatewayRateLimitPort.Request(
                            route.standardExecutionId(),
                            route.routeId(),
                            clientId,
                            channelId,
                            tenantId,
                            rateLimitRequestId(trusted, route.routeId(), tx),
                            1,
                            clock.instant()));
            request.servletRequest().setAttribute(RATE_LIMIT_DECISION_ATTR, rateDecision);
            if (!rateDecision.allowed()) {
                request.servletRequest().setAttribute(PRINCIPAL_ATTR, principal.principalId());
                auditRecovery.record(new CpfGatewayAuditEvent(
                        tx,
                        route.standardExecutionId(),
                        principal.principalId(),
                        null,
                        "RATE_LIMIT",
                        "DENIED",
                        rateDecision.reason(),
                        null,
                        clock.instant(),
                        Map.of(
                                "routeId", route.routeId(),
                                "policyId", rateDecision.policyId(),
                                "scope", rateDecision.limitingScope() == null
                                        ? "" : rateDecision.limitingScope().name(),
                                "degraded", Boolean.toString(rateDecision.degraded()))));
                return rateLimitResponse(rateDecision);
            }

            if (route.requiredPermission() != null
                    && !route.requiredPermission().isBlank()
                    && !principal.authenticated()) {
                throw new SecurityException("보호 Gateway route에는 검증된 Principal이 필요합니다.");
            }

            CpfChannelPolicyDecision channelDecision = channelPolicies.evaluate(
                    route.standardExecutionId(),
                    request.headers().firstHeader(CpfGatewayHeaderNames.ORIGINAL_CLIENT_CHANNEL_CODE),
                    request.headers().firstHeader(CpfGatewayHeaderNames.CLIENT_CHANNEL_CODE),
                    request.headers().firstHeader(CpfHttpHeaderNames.REQUEST_TYPE),
                    principal.authenticated(),
                    requestSignatureVerified(principal));
            if (!channelDecision.allowed()) {
                throw new SecurityException(
                        "Gateway 채널 정책에서 요청을 거부했습니다. reason=" + channelDecision.reason());
            }
            String contextTenantId = principal.authenticated() ? value(principal.attributes().get("tenantId"), "") : "";
            CpfGatewayContextFactory.EnrichedContext gatewayContext = gatewayContexts.enrichCurrent(
                    principal.authenticated(), principal.authenticated() ? principal.principalId() : null,
                    principal.attributes().get("actorId"), principal.attributes().get("authenticationContextId"),
                    contextTenantId.isBlank() ? null : contextTenantId, route.standardExecutionId(), route.routeId(), route.routeVersion(),
                    route.serverGroupId(), com.cpf.platform.operations.api.runtime.CpfInstanceIdentity.current().instanceId(), CpfContexts.requireCurrent().execution().deadline());
            try (AutoCloseable ignoredGatewayContext = CpfContexts.bind(gatewayContext.snapshot())) {

            trusted.put("cpf.principal.id", principal.principalId());
            trusted.put("cpf.principal.authorities", String.join(",", principal.authorities()));
            principal.attributes().forEach((key, value) ->
                    trusted.put("cpf.principal." + key, value));
            if (!authorization.isAllowed(route, Map.copyOf(trusted))) {
                throw new SecurityException(
                        "Gateway route 실행 권한이 없습니다. permission=" + route.requiredPermission());
            }
            CpfGatewayRuntimePolicy.CorsDecision corsDecision = corsDecision(
                    request, request.method().name());
            if (!corsDecision.allowed()) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Gateway CORS 정책 거부: " + corsDecision.reason());
            }
            request.servletRequest().setAttribute(CORS_DECISION_ATTR, corsDecision);

            String reason = firstText(
                    request.headers().firstHeader(CpfHttpHeaderNames.AUDIT_REASON),
                    request.headers().firstHeader("X-Operation-Reason"));
            if (route.auditReasonRequired() && reason == null) {
                throw new IllegalArgumentException(
                        "Gateway 위험 거래에는 " + CpfHttpHeaderNames.AUDIT_REASON + "가 필요합니다.");
            }

            BodyPlan body = bodyPlan(request, route, safety.getRequestBodyBytesCap());
            if (!"*".equals(route.httpMethod())
                    && !route.httpMethod().equalsIgnoreCase(request.method().name())) {
                throw new IllegalArgumentException(
                        "Gateway route HTTP method 불일치. inbound=" + request.method().name()
                                + ", route=" + route.httpMethod());
            }
            if (HttpMethod.GET.matches(request.method().name()) && body.requestBytes() > 0L) {
                throw new IllegalArgumentException("Gateway GET 요청에는 body를 허용하지 않습니다.");
            }
            if (body.buffered()) {
                captureService.captureRequestBody(
                        tx,
                        body.bytes(),
                        body.requestBytes(),
                        body.bodyHash(),
                        false,
                        logPolicy);
            }

            String targetPath = CpfGatewayPathRewriter.rewrite(
                    route.pathPattern(), route.targetPath(), resolved.inboundPath());
            request.servletRequest().setAttribute(PRINCIPAL_ATTR, principal.principalId());
            request.servletRequest().setAttribute(REASON_ATTR, reason);
            request.servletRequest().setAttribute(BODY_HASH_ATTR, body.bodyHash());

            CpfGatewayAuditEvent before = new CpfGatewayAuditEvent(
                    tx,
                    route.standardExecutionId(),
                    principal.principalId(),
                    reason,
                    "BEFORE",
                    "ACCEPTED",
                    null,
                    null,
                    clock.instant(),
                    Map.of(
                            "routeId", route.routeId(),
                            "bodySha256", body.bodyHash(),
                            "bodyMode", body.buffered()
                                    ? "BOUNDED_BUFFERED_REPLAY"
                                    : "BOUNDED_STREAMING_SINGLE_ATTEMPT"));
            if (route.auditReasonRequired()) {
                auditRecovery.recordRequired(before);
            } else {
                auditRecovery.record(before);
            }

            int maxRetries = body.replaySafe()
                    ? Math.min(route.maxRetryCount(), safety.getRetryCountCap())
                    : 0;
            int maxAttempts = maxRetries + 1;
            AtomicInteger attempts = new AtomicInteger();
            var circuitBreaker = circuitBreakers.create("gateway-" + route.routeId());

            HandlerFunction<ServerResponse> upstream = ignored -> {
                int attemptNo = attempts.incrementAndGet();
                OffsetDateTime attemptStarted = OffsetDateTime.now(clock);
                CpfScgTargetResolver.Target target = targets.resolve(
                        route, targetPath, request.uri().getRawQuery());
                request.servletRequest().setAttribute(TARGET_ATTR, target.instanceId());
                URI uri = target.uri();
                try (AutoCloseable ignoredAttemptContext = gatewayContexts.bindUpstreamAttempt(
                        route.standardExecutionId(), attemptNo, route.serverGroupId(), target.instanceId(),
                        CpfContexts.requireCurrent().execution().deadline())) {
                    ServerRequest upstreamRequest = upstreamRequest(
                            request, body, trusted, tx, route, target, logPolicy);
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
                    boolean retryStatus = body.replaySafe()
                            && retryableStatus(response.statusCode().value())
                            && attemptNo < maxAttempts;
                    boolean failedStatus = response.statusCode().is5xxServerError();
                    ledgerRecovery.recordAttempt(attempt(
                            tx,
                            attemptNo,
                            target,
                            uri,
                            attemptStarted,
                            retryStatus ? "RETRYABLE_FAILURE" : failedStatus ? "FAILED" : "SUCCESS",
                            Integer.toString(response.statusCode().value()),
                            retryStatus ? "UPSTREAM_RETRYABLE_STATUS" : null,
                            null,
                            false));
                    return response;
                } catch (Throwable raw) {
                    Throwable failure = unwrap(raw);
                    boolean retryFailure = body.replaySafe()
                            && retryable(failure)
                            && attemptNo < maxAttempts;
                    ledgerRecovery.recordAttempt(attempt(
                            tx,
                            attemptNo,
                            target,
                            uri,
                            attemptStarted,
                            retryFailure ? "RETRYABLE_FAILURE" : "FAILED",
                            "",
                            "UPSTREAM_CALL_FAILED",
                            safe(failure.getMessage()),
                            unknownResult(failure)));
                    if (retryFailure) {
                        throw new GatewayRetryableException(failure);
                    }
                        if (failure instanceof Exception exception) throw exception;
                        throw new IllegalStateException(failure);
                    }
                }
            };

            if (maxRetries > 0) {
                RetryFilterFunctions.RetryConfig retry = new RetryFilterFunctions.RetryConfig()
                        .setRetries(maxRetries)
                        .setMethods(Set.of(request.method()))
                        .setCacheBody(false)
                        .setExceptions(Set.of(
                                GatewayRetryableException.class,
                                IOException.class,
                                TimeoutException.class,
                                RetryFilterFunctions.RetryException.class));
                upstream = RetryFilterFunctions.retry(retry).apply(upstream);
            }
            return upstream.handle(request);
            }
        } catch (Exception failure) {
            try {
                captureService.captureError(tx, failure, logPolicy);
            } catch (RuntimeException captureFailure) {
                failure.addSuppressed(captureFailure);
            }
            throw failure;
        }
    }

    private ResolvedRoute resolveRoute(ServerRequest request) {
        String path = request.uri().getRawPath();
        String executionHeader = request.headers().firstHeader(CpfGatewayHeaderNames.EXECUTION_ROUTE_ID);
        if (EXECUTE_PATH.equals(path)) {
            String executionId = requireExecutionId(executionHeader);
            CpfGatewayRoute route = snapshot.resolve(executionId);
            return new ResolvedRoute(route, staticInboundPath(route));
        }
        if (path.startsWith(EXECUTE_PATH + "/")) {
            String executionId = requireExecutionId(path.substring((EXECUTE_PATH + "/").length()));
            if (executionHeader != null && !executionHeader.equals(executionId)) {
                throw new IllegalArgumentException("URI와 header의 표준 실행 ID가 일치하지 않습니다.");
            }
            CpfGatewayRoute route = snapshot.resolve(executionId);
            return new ResolvedRoute(route, staticInboundPath(route));
        }

        String inboundPath = path.startsWith(LEGACY_PUBLIC_PREFIX + "/")
                ? path.substring(LEGACY_PUBLIC_PREFIX.length())
                : path;
        String routeMethod = HttpMethod.OPTIONS.matches(request.method().name())
                ? firstText(
                        request.headers().firstHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD),
                        request.method().name())
                : request.method().name();
        String version = firstText(
                request.headers().firstHeader("X-Api-Version"),
                request.headers().firstHeader("X-Cpf-Api-Version"));
        CpfGatewayRoute route = snapshot.resolveRequest(
                safety.getEnvironmentCode(),
                request.headers().firstHeader(HttpHeaders.HOST),
                inboundPath,
                routeMethod,
                version);
        if (executionHeader != null && !executionHeader.equals(route.standardExecutionId())) {
            throw new SecurityException("외부 경로와 표준 실행 ID header가 일치하지 않습니다.");
        }
        return new ResolvedRoute(route, inboundPath);
    }

    private ServerResponse preflight(ServerRequest request, CpfGatewayRoute route) {
        String requestedMethod = firstText(
                request.headers().firstHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD),
                route.httpMethod());
        CpfGatewayRuntimePolicy.CorsDecision decision = corsDecision(request, requestedMethod);
        if (!decision.allowed()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Gateway CORS 정책 거부: " + decision.reason());
        }
        return ServerResponse.noContent().headers(headers -> {
            applyCorsHeaders(headers, decision);
            HttpMethod allowed = HttpMethod.valueOf(requestedMethod.toUpperCase(Locale.ROOT));
            headers.setAccessControlAllowMethods(List.of(allowed, HttpMethod.OPTIONS));
            List<String> requestedHeaders = accessControlRequestHeaders(request);
            if (!requestedHeaders.isEmpty()) {
                headers.setAccessControlAllowHeaders(requestedHeaders);
            }
        }).build();
    }

    private CpfGatewayRuntimePolicy.CorsDecision corsDecision(
            ServerRequest request, String method) {
        return runtimePolicy.evaluateCors(
                request.headers().firstHeader(HttpHeaders.ORIGIN),
                method,
                accessControlRequestHeaders(request));
    }

    private ServerResponse entryDenied(
            ServerRequest request, CpfGatewayEntryPolicyPort.Decision decision) {
        String transactionId = newCanonicalTransactionId();
        try {
            auditRecovery.record(new CpfGatewayAuditEvent(
                    transactionId,
                    "",
                    "anonymous",
                    null,
                    "ENTRY",
                    "DENIED",
                    com.cpf.platform.operations.api.runtime.CpfInstanceIdentity.current().instanceId(),
                    decision.httpStatus(),
                    clock.instant(),
                    Map.of(
                            "state", decision.state().name(),
                            "reason", decision.reason(),
                            "method", request.method().name(),
                            "pathHash", opaqueValue(request.path()),
                            "policyVersion", Long.toString(decision.policyVersion()))));
        } catch (RuntimeException auditFailure) {
            return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .header("X-Cpf-Gateway-State", decision.state().name())
                    .header("X-Cpf-Gateway-Entry-Reason", "ENTRY_AUDIT_UNAVAILABLE")
                    .build();
        }
        ServerResponse.BodyBuilder response = ServerResponse.status(decision.httpStatus())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("X-Cpf-Gateway-State", decision.state().name())
                .header("X-Cpf-Gateway-Entry-Reason", decision.reason())
                .header("X-Cpf-Gateway-Policy-Version", Long.toString(decision.policyVersion()));
        if (!decision.retryAfter().isZero()) {
            response.header(HttpHeaders.RETRY_AFTER,
                    Long.toString(Math.max(1L, decision.retryAfter().toSeconds())));
        }
        return response.build();
    }

    private static String opaqueValue(String value) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest, 0, 16);
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    private static ServerResponse rateLimitResponse(CpfGatewayRateLimitPort.Decision decision) {
        long retrySeconds = Math.max(1L, (decision.retryAfter().toMillis() + 999L) / 1_000L);
        return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, Long.toString(retrySeconds))
                .header("X-RateLimit-Remaining", Long.toString(decision.remaining()))
                .header("X-RateLimit-Reset", Long.toString(decision.resetAt().getEpochSecond()))
                .header("X-Cpf-RateLimit-Policy", decision.policyId())
                .header("X-Cpf-RateLimit-Scope",
                        decision.limitingScope() == null ? "" : decision.limitingScope().name())
                .header("X-Cpf-RateLimit-Degraded", Boolean.toString(decision.degraded()))
                .header("X-Cpf-RateLimit-Reason", decision.reason())
                .build();
    }

    private static boolean isConnectPhaseFailure(Throwable failure) {
        String name = failure.getClass().getName();
        return name.endsWith("ConnectTimeoutException")
                || name.endsWith("ConnectionRequestTimeoutException")
                || name.endsWith("NoRouteToHostException")
                || name.endsWith("PortUnreachableException");
    }

    private static List<String> accessControlRequestHeaders(ServerRequest request) {
        List<String> values = request.headers().header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
        if (values.isEmpty()) return List.of();
        return values.stream()
                .flatMap(value -> java.util.Arrays.stream(value.split(",")))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .toList();
    }

    private static void applyCorsHeaders(
            HttpHeaders headers, CpfGatewayRuntimePolicy.CorsDecision decision) {
        if (decision == null || decision.allowOrigin().isBlank()) return;
        headers.setAccessControlAllowOrigin(decision.allowOrigin());
        headers.setAccessControlAllowCredentials(decision.allowCredentials());
        headers.setAccessControlMaxAge(decision.maxAgeSeconds());
        if (!decision.exposedHeaders().isEmpty()) {
            headers.setAccessControlExposeHeaders(List.copyOf(decision.exposedHeaders()));
        }
        headers.add(HttpHeaders.VARY, HttpHeaders.ORIGIN);
    }

    private Map<String, String> credentialHeaders(ServerRequest request) {
        Map<String, String> credentials = new LinkedHashMap<>();
        copyCredential(request, credentials, CpfHttpHeaderNames.AUTHORIZATION);
        copyCredential(request, credentials, CpfHttpHeaderNames.API_KEY);
        copyCredential(request, credentials, CpfGatewayHeaderNames.REQUEST_SIGNATURE);
        credentials.put("cpf.client.ip", sourceIp(request));
        String serial = certificateSerial(request.servletRequest());
        if (!serial.isBlank()) credentials.put("cpf.client.cert.serial", serial);
        return Map.copyOf(credentials);
    }

    private static void copyCredential(
            ServerRequest request, Map<String, String> target, String name) {
        List<String> values = request.headers().header(name);
        if (values.isEmpty()) return;
        if (values.size() != 1) {
            throw new SecurityException("Gateway duplicate credential header denied: " + name);
        }
        String value = values.getFirst();
        if (value.length() > 8_192 || containsControl(value)) {
            throw new SecurityException("Gateway credential header denied: " + name);
        }
        target.put(name, value);
    }

    private static boolean requestSignatureVerified(CpfGatewayPrincipal principal) {
        return Boolean.parseBoolean(principal.attributes().get("requestSignatureVerified"));
    }

    private static String certificateSerial(HttpServletRequest request) {
        Object value = request.getAttribute("jakarta.servlet.request.X509Certificate");
        if (value instanceof X509Certificate[] certificates
                && certificates.length > 0
                && certificates[0] != null) {
            return certificates[0].getSerialNumber().toString(16).toUpperCase(Locale.ROOT);
        }
        return "";
    }

    private static String requireExecutionId(String value) {
        if (value == null || !value.matches("O[A-Z0-9]{9}")) {
            throw new IllegalArgumentException("10자리 O 유형 표준 실행 ID가 필요합니다.");
        }
        return value;
    }

    private static String staticInboundPath(CpfGatewayRoute route) {
        String pattern = route.pathPattern();
        if (pattern.indexOf('*') >= 0 || pattern.indexOf('{') >= 0) {
            throw new IllegalArgumentException(
                    "실행 ID 진입점의 동적 Route에는 실제 외부 Path가 필요합니다. routeId="
                            + route.routeId());
        }
        return pattern.startsWith("/") ? pattern : "/" + pattern;
    }

    private static String firstText(String first, String second) {
        if (first != null && !first.isBlank()) return first.trim();
        return second == null || second.isBlank() ? null : second.trim();
    }

    private static boolean containsControl(String value) {
        return value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0;
    }


    private ServerRequest upstreamRequest(
            ServerRequest original,
            BodyPlan body,
            Map<String, String> trusted,
            String gatewayTransactionId,
            CpfGatewayRoute route,
            CpfScgTargetResolver.Target target,
            LogPolicyDecision logPolicy) {
        ServerRequest bodyRequest;
        if (body.buffered()) {
            bodyRequest = ServerRequest.from(original).body(body.bytes()).build();
        } else {
            HttpServletRequest bounded = new BoundedBodyRequest(
                    original.servletRequest(),
                    safety.getRequestBodyBytesCap(),
                    original.headers().contentLength().orElse(-1L),
                    captureService.requestCaptureLimit(logPolicy),
                    capture -> captureService.captureRequestBody(
                            gatewayTransactionId,
                            capture.preview(),
                            capture.observedBytes(),
                            capture.sha256(),
                            capture.truncated(),
                            logPolicy));
            bodyRequest = ServerRequest.create(bounded, original.messageConverters());
        }

        return ServerRequest.from(bodyRequest)
                .headers(headers -> {
                    headers.clear();
                    trusted.forEach((name, value) -> {
                        if (value != null
                                && !value.isBlank()
                                && !name.startsWith("cpf.principal.")
                                && runtimePolicy.allowRequestHeader(name)) {
                            headers.set(name, value);
                        }
                    });
                    headers.remove(HttpHeaders.HOST);
                    headers.set(HttpHeaders.HOST, target.authorityHeader());
                    headers.remove(HttpHeaders.COOKIE);
                    headers.remove(HttpHeaders.CONTENT_LENGTH);
                    CpfWebContext interaction = new CpfWebContext(
                            null, null, null, null, null, null, null, null, null, null, null,
                            trusted.get(CpfHttpHeaderNames.TRACEPARENT.toLowerCase(Locale.ROOT)),
                            trusted.get(CpfHttpHeaderNames.TRACESTATE.toLowerCase(Locale.ROOT)),
                            com.cpf.web.context.CpfHttpIngressTrust.INTERNAL_TRUSTED);
                    httpContextOutbound.headers(CpfContexts.requireCurrent(), interaction,
                            new CpfHttpOutboundRequest(route.serviceId(), route.operationId(), route.routeVersion(), true))
                            .forEach(headers::set);
                    headers.set(CpfGatewayHeaderNames.GATEWAY_TRANSACTION_ID, gatewayTransactionId);
                    headers.set(CpfGatewayHeaderNames.GATEWAY_ROUTE_ID, route.routeId());
                    headers.set(CpfGatewayHeaderNames.GATEWAY_ROUTE_VERSION, route.routeVersion());
                    headers.set(CpfGatewayHeaderNames.GATEWAY_INSTANCE_ID, com.cpf.platform.operations.api.runtime.CpfInstanceIdentity.current().instanceId());
                    headers.set(CpfGatewayHeaderNames.INGRESS_TYPE, "CPF_GATEWAY");
                })
                .cookies(cookies -> cookies.clear())
                .attribute(MvcUtils.GATEWAY_REQUEST_URL_ATTR, target.uri())
                .build();
    }

    private Map<String, String> trustedHeaders(ServerRequest request) {
        Map<String, String> values = new LinkedHashMap<>();
        int count = 0;
        int bytes = 0;
        for (var entry : request.headers().asHttpHeaders().headerSet()) {
            String lower = entry.getKey().toLowerCase(Locale.ROOT);
            if (CpfHttpHeaderCatalog.isCanonicalTransaction(entry.getKey())
                    || lower.startsWith("x-forwarded-")
                    || (lower.startsWith("x-cpf-") && !EXTERNAL_CPF_HEADERS.contains(lower))) {
                throw new SecurityException("Untrusted internal/proxy header: " + entry.getKey());
            }
            if (entry.getValue().isEmpty()) {
                continue;
            }
            if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(entry.getKey())
                    || CpfHttpHeaderNames.API_KEY.equalsIgnoreCase(entry.getKey())
                    || CpfGatewayHeaderNames.REQUEST_SIGNATURE.equalsIgnoreCase(entry.getKey())
                    || CpfGatewayHeaderNames.EXECUTION_ROUTE_ID.equalsIgnoreCase(entry.getKey())
                    || CpfHttpHeaderNames.AUDIT_REASON.equalsIgnoreCase(entry.getKey())) {
                continue;
            }
            if (!safety.getTrustedContextHeaders().contains(lower)
                    && !STANDARD_CONTEXT_HEADERS.contains(lower)) {
                continue;
            }
            if (entry.getValue().size() != 1) {
                throw new SecurityException("Gateway trusted header must have exactly one value: " + entry.getKey());
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


    private static String rateLimitRequestId(
            Map<String, String> trustedHeaders, String routeId, String fallbackTransactionId) {
        String raw = firstText(
                trustedHeaders.get(CpfHttpHeaderNames.IDEMPOTENCY_KEY.toLowerCase(Locale.ROOT)),
                trustedHeaders.get("idempotency-key"));
        if (raw == null || !raw.matches("[A-Za-z0-9._:-]{8,128}")) {
            return fallbackTransactionId;
        }
        return sha256((routeId + '|' + raw).getBytes(StandardCharsets.UTF_8));
    }

    private static BodyPlan bodyPlan(
            ServerRequest request,
            CpfGatewayRoute route,
            long cap) throws IOException {
        String method = request.method().name();
        long declared = request.headers().contentLength().orElse(-1);
        MediaType type = request.headers().contentType().orElse(MediaType.APPLICATION_OCTET_STREAM);
        boolean bodyless = "GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method);
        boolean streamLike = MediaType.MULTIPART_FORM_DATA.isCompatibleWith(type)
                || MediaType.APPLICATION_OCTET_STREAM.isCompatibleWith(type)
                || MediaType.TEXT_EVENT_STREAM.isCompatibleWith(type);
        String idempotencyKey = firstText(
                request.headers().firstHeader(CpfHttpHeaderNames.IDEMPOTENCY_KEY),
                request.headers().firstHeader("Idempotency-Key"));
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

    private CpfGatewayLedgerPort.Attempt attempt(
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
        OffsetDateTime finished = OffsetDateTime.now(clock);
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
        return status >= 500;
    }

    private static boolean retryable(Throwable failure) {
        Throwable value = unwrap(failure);
        return value instanceof IOException || value instanceof TimeoutException;
    }

    private static boolean unknownResult(Throwable failure) {
        Throwable value = unwrap(failure);
        if (value instanceof ConnectException
                || value instanceof UnknownHostException
                || isConnectPhaseFailure(value)) {
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
                        || value instanceof GatewayRetryableException
                        || value instanceof java.util.concurrent.CompletionException)
                && value.getCause() != null) {
            value = value.getCause();
        }
        return value;
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
        private final long declaredLength;
        private final int captureLimit;
        private final BodyCaptureCompletion completion;
        private ServletInputStream input;
        private BufferedReader reader;

        private BoundedBodyRequest(
                HttpServletRequest request,
                long cap,
                long declaredLength,
                int captureLimit,
                BodyCaptureCompletion completion) {
            super(request);
            if (cap < 0) {
                throw new IllegalArgumentException("Gateway request body cap must not be negative");
            }
            this.cap = cap;
            this.declaredLength = declaredLength;
            this.captureLimit = Math.max(0, captureLimit);
            this.completion = Objects.requireNonNull(completion, "completion");
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (reader != null) {
                throw new IllegalStateException("getReader already called");
            }
            if (input == null) {
                input = new BoundedServletInputStream(
                        super.getInputStream(), cap, declaredLength, captureLimit, completion);
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
                input = new BoundedServletInputStream(
                        super.getInputStream(), cap, declaredLength, captureLimit, completion);
                reader = new BufferedReader(new InputStreamReader(input, charset));
            }
            return reader;
        }
    }

    private static final class BoundedServletInputStream extends ServletInputStream {
        private final ServletInputStream delegate;
        private final long cap;
        private final long declaredLength;
        private final int captureLimit;
        private final BodyCaptureCompletion completion;
        private final ByteArrayOutputStream preview;
        private final MessageDigest digest;
        private long readBytes;
        private boolean completed;

        private BoundedServletInputStream(
                ServletInputStream delegate,
                long cap,
                long declaredLength,
                int captureLimit,
                BodyCaptureCompletion completion) {
            this.delegate = delegate;
            this.cap = cap;
            this.declaredLength = declaredLength;
            this.captureLimit = Math.max(0, captureLimit);
            this.completion = completion;
            this.preview = new ByteArrayOutputStream(Math.min(this.captureLimit, 65_536));
            try {
                this.digest = MessageDigest.getInstance("SHA-256");
            } catch (Exception impossible) {
                throw new IllegalStateException("SHA-256 unavailable", impossible);
            }
        }

        @Override
        public int read() throws IOException {
            int value = delegate.read();
            if (value >= 0) {
                accept(new byte[] {(byte) value}, 0, 1);
            } else {
                finish(false);
            }
            return value;
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            int read = delegate.read(bytes, offset, length);
            if (read > 0) {
                accept(bytes, offset, read);
            } else if (read < 0) {
                finish(false);
            }
            return read;
        }

        private void accept(byte[] bytes, int offset, int increment) throws IOException {
            readBytes += increment;
            if (readBytes > cap) {
                finish(true);
                throw new IOException("Gateway streaming request body exceeds configured cap");
            }
            digest.update(bytes, offset, increment);
            int remaining = captureLimit - preview.size();
            if (remaining > 0) {
                preview.write(bytes, offset, Math.min(remaining, increment));
            }
        }

        private void finish(boolean failedOrIncomplete) {
            if (completed) return;
            completed = true;
            boolean incomplete = failedOrIncomplete
                    || (declaredLength >= 0L && readBytes < declaredLength);
            completion.accept(new BodyCapture(
                    preview.toByteArray(),
                    readBytes,
                    java.util.HexFormat.of().formatHex(digest.digest()),
                    incomplete || readBytes > preview.size()));
        }

        @Override public boolean isFinished() { return delegate.isFinished(); }
        @Override public boolean isReady() { return delegate.isReady(); }
        @Override public void setReadListener(ReadListener listener) { delegate.setReadListener(listener); }
        @Override public void close() throws IOException {
            try {
                delegate.close();
            } finally {
                finish(false);
            }
        }
    }

    private record BodyPlan(byte[] bytes, long requestBytes, String bodyHash, boolean replaySafe) {
        boolean buffered() {
            return bytes != null;
        }
    }

    private record ResolvedRoute(CpfGatewayRoute route, String inboundPath) {}

    private record BodyCapture(
            byte[] preview, long observedBytes, String sha256, boolean truncated) {}

    @FunctionalInterface
    private interface BodyCaptureCompletion {
        void accept(BodyCapture capture);
    }

    private static final class GatewayUpstreamException extends RuntimeException {
        GatewayUpstreamException(Throwable cause) {
            super(cause);
        }
    }

    private static final class GatewayRetryableException extends IOException {
        GatewayRetryableException(Throwable cause) {
            super("Retryable SCG upstream failure", cause);
        }
    }
    private String newCanonicalTransactionId() {
        return CpfTransactionIds.requireCanonical(transactionIds.newTransactionId());
    }

    private static CpfTransactionIdGenerator fallbackTransactionIdGenerator() {
        return new DefaultCpfTransactionIdGenerator("GWY", Clock.systemUTC());
    }

    private static CpfExecutionIdGenerator fallbackExecutionIdGenerator() {
        return new CpfExecutionIdGenerator() {
            public String newExecutionId() { return "GW-EX-" + UUID.randomUUID(); }
            public String newSegmentId() { return "GW-SG-" + UUID.randomUUID(); }
        };
    }

}
