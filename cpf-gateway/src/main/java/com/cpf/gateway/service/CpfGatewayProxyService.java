package com.cpf.gateway.service;

import com.cpf.core.api.gateway.CpfGatewayAuditEvent;
import com.cpf.core.api.gateway.CpfGatewayAuditPort;
import com.cpf.core.api.gateway.CpfGatewayAuthenticationPort;
import com.cpf.core.api.gateway.CpfGatewayAuthorizationPort;
import com.cpf.core.api.gateway.CpfGatewayPrincipal;
import com.cpf.core.api.gateway.CpfGatewayLedgerPort;
import com.cpf.core.api.gateway.CpfGatewayRoute;
import com.cpf.core.api.header.CpfHeaderNames;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.runtime.CpfInstanceIdentity;
import com.cpf.core.api.servicecall.CpfServiceCallAttempt;
import com.cpf.core.api.servicecall.CpfServiceCallCommand;
import com.cpf.core.api.servicecall.CpfServiceCallExecutor;
import com.cpf.core.api.servicecall.CpfServiceCallFailedException;
import com.cpf.core.api.servicecall.CpfServiceCallOutcome;
import com.cpf.core.api.servicecall.CpfServiceCallTarget;
import com.cpf.core.channel.application.CpfChannelPolicyService;
import com.cpf.core.channel.model.CpfChannelPolicyDecision;
import com.cpf.gateway.config.CpfGatewaySafetyEnforcer;
import com.cpf.gateway.config.CpfGatewaySafetyProperties;
import com.cpf.gateway.route.CpfGatewayPathRewriter;
import com.cpf.gateway.route.CpfGatewayRouteSnapshot;
import com.cpf.gateway.logging.CpfGatewayCaptureService;
import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.gateway.runtime.CpfGatewayRuntimePolicy;
import com.cpf.gateway.transport.CpfGatewayHttpExchangePort;
import com.cpf.gateway.transport.CpfGatewayProxyResponse;
import com.cpf.gateway.transport.CpfGatewayReplayableBody;
import com.cpf.gateway.transport.CpfGatewayTransferPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.util.UUID;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * route·registry·인증·권한·감사 신뢰경계를 유지하면서 대용량 요청/응답을 streaming으로 전달합니다.
 * 요청 본문은 retry/failover를 위해 메모리 임계치 이후 임시파일로 전환하고, 응답은 downstream stream을
 * Servlet output으로 직접 복사하여 전체 본문을 byte[]로 적재하지 않습니다.
 */
@Service
public class CpfGatewayProxyService {
    private static final int MAX_QUERY_LENGTH = 8 * 1024;
    private static final Set<String> HOP_BY_HOP = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailer", "transfer-encoding", "upgrade", "host");
    private static final Set<String> NEVER_FORWARD = Set.of(
            CpfHeaderNames.AUTHORIZATION.toLowerCase(Locale.ROOT),
            CpfHeaderNames.API_KEY.toLowerCase(Locale.ROOT),
            CpfHeaderNames.REQUEST_SIGNATURE.toLowerCase(Locale.ROOT));
    private static final Set<String> PASSTHROUGH = Set.of(
            HttpHeaders.ACCEPT.toLowerCase(Locale.ROOT),
            HttpHeaders.ACCEPT_LANGUAGE.toLowerCase(Locale.ROOT),
            HttpHeaders.ACCEPT_ENCODING.toLowerCase(Locale.ROOT),
            HttpHeaders.CONTENT_TYPE.toLowerCase(Locale.ROOT),
            HttpHeaders.CONTENT_ENCODING.toLowerCase(Locale.ROOT),
            HttpHeaders.CONTENT_DISPOSITION.toLowerCase(Locale.ROOT),
            HttpHeaders.USER_AGENT.toLowerCase(Locale.ROOT),
            HttpHeaders.RANGE.toLowerCase(Locale.ROOT),
            "if-range",
            HttpHeaders.IF_MATCH.toLowerCase(Locale.ROOT),
            HttpHeaders.IF_NONE_MATCH.toLowerCase(Locale.ROOT),
            HttpHeaders.IF_MODIFIED_SINCE.toLowerCase(Locale.ROOT),
            HttpHeaders.IF_UNMODIFIED_SINCE.toLowerCase(Locale.ROOT),
            HttpHeaders.CACHE_CONTROL.toLowerCase(Locale.ROOT),
            HttpHeaders.PRAGMA.toLowerCase(Locale.ROOT),
            CpfHeaderNames.ORIGINAL_CHANNEL_CODE.toLowerCase(Locale.ROOT),
            CpfHeaderNames.CHANNEL_CODE.toLowerCase(Locale.ROOT),
            CpfHeaderNames.REQUEST_TYPE.toLowerCase(Locale.ROOT),
            CpfHeaderNames.IDEMPOTENCY_KEY.toLowerCase(Locale.ROOT));

    private final CpfGatewayRouteSnapshot snapshot;
    private final CpfServiceCallExecutor serviceCallEngine;
    private final CpfGatewayAuthenticationPort authenticationPort;
    private final CpfGatewayAuthorizationPort authorizationPort;
    private final CpfGatewayAuditPort auditPort;
    private final CpfChannelPolicyService channelPolicyService;
    private final CpfGatewayRuntimePolicy runtimePolicy;
    private final CpfGatewayTransferPolicy transferPolicy;
    private final CpfGatewayHttpExchangePort httpExchange;
    private final CpfGatewayLedgerPort ledger;
    private final CpfGatewayCaptureService captureService;
    private final CpfGatewaySafetyEnforcer safety;

    public CpfGatewayProxyService(
            CpfGatewayRouteSnapshot snapshot,
            CpfServiceCallExecutor serviceCallEngine,
            CpfGatewayAuthenticationPort authenticationPort,
            CpfGatewayAuthorizationPort authorizationPort,
            CpfGatewayAuditPort auditPort,
            CpfChannelPolicyService channelPolicyService,
            CpfGatewayRuntimePolicy runtimePolicy,
            CpfGatewayTransferPolicy transferPolicy,
            CpfGatewayHttpExchangePort httpExchange,
            CpfGatewayLedgerPort ledger,
            CpfGatewayCaptureService captureService) {
        this(snapshot, serviceCallEngine, authenticationPort, authorizationPort, auditPort,
                channelPolicyService, runtimePolicy, transferPolicy, httpExchange, ledger, captureService,
                new CpfGatewaySafetyEnforcer(new CpfGatewaySafetyProperties()));
    }

    @Autowired
    public CpfGatewayProxyService(
            CpfGatewayRouteSnapshot snapshot,
            CpfServiceCallExecutor serviceCallEngine,
            CpfGatewayAuthenticationPort authenticationPort,
            CpfGatewayAuthorizationPort authorizationPort,
            CpfGatewayAuditPort auditPort,
            CpfChannelPolicyService channelPolicyService,
            CpfGatewayRuntimePolicy runtimePolicy,
            CpfGatewayTransferPolicy transferPolicy,
            CpfGatewayHttpExchangePort httpExchange,
            CpfGatewayLedgerPort ledger,
            CpfGatewayCaptureService captureService,
            CpfGatewaySafetyEnforcer safety) {
        this.snapshot = snapshot;
        this.serviceCallEngine = serviceCallEngine;
        this.authenticationPort = authenticationPort;
        this.authorizationPort = authorizationPort;
        this.auditPort = auditPort;
        this.channelPolicyService = channelPolicyService;
        this.runtimePolicy = runtimePolicy;
        this.transferPolicy = transferPolicy;
        this.httpExchange = httpExchange;
        this.ledger = Objects.requireNonNull(ledger, "Mandatory Gateway ledger is required");
        this.captureService = Objects.requireNonNull(captureService, "Gateway capture service is required");
        this.safety = Objects.requireNonNull(safety, "Gateway safety enforcer is required");
    }

    /** 기존 내부 Consumer 호환용 POST 진입점입니다. 대용량 외부 요청은 executeStreaming을 사용합니다. */
    public ResponseEntity<byte[]> execute(String executionId, HttpHeaders inboundHeaders, byte[] body) {
        return execute(executionId, "POST", inboundHeaders, body, "", "");
    }

    public ResponseEntity<byte[]> execute(
            String executionId, String inboundMethod, HttpHeaders inboundHeaders, byte[] body) {
        return execute(executionId, inboundMethod, inboundHeaders, body, "", "");
    }

    public ResponseEntity<byte[]> execute(
            String executionId,
            String inboundMethod,
            HttpHeaders inboundHeaders,
            byte[] body,
            String verifiedClientIp,
            String verifiedCertificateSerial) {
        byte[] safeBody = body == null ? new byte[0] : body;
        try (CpfGatewayReplayableBody replayable = CpfGatewayReplayableBody.capture(
                new ByteArrayInputStream(safeBody), safeBody.length, transferPolicy);
             CpfGatewayProxyResponse response = dispatch(
                     executionId, inboundMethod, inboundHeaders, replayable, null, null,
                     verifiedClientIp, verifiedCertificateSerial)) {
            return new ResponseEntity<>(response.readAllBytes(), response.headers(), response.status());
        }
    }

    /**
     * Servlet request stream을 retry 가능한 저장소로 캡처한 뒤, 최종 downstream 응답은 one-shot stream으로 반환합니다.
     */
    public ResponseEntity<StreamingResponseBody> executeStreaming(
            String executionId,
            String inboundMethod,
            HttpHeaders inboundHeaders,
            InputStream bodyInput,
            long declaredLength,
            String rawQuery,
            String verifiedClientIp,
            String verifiedCertificateSerial) {
        return executeStreaming(executionId, inboundMethod, inboundHeaders, bodyInput, declaredLength,
                null, rawQuery, verifiedClientIp, verifiedCertificateSerial);
    }

    public ResponseEntity<StreamingResponseBody> executeStreaming(
            String executionId,
            String inboundMethod,
            HttpHeaders inboundHeaders,
            InputStream bodyInput,
            long declaredLength,
            String inboundPath,
            String rawQuery,
            String verifiedClientIp,
            String verifiedCertificateSerial) {
        safety.validateRequest(inboundHeaders, declaredLength);
        CpfGatewayProxyResponse response;
        try (CpfGatewayReplayableBody body = CpfGatewayReplayableBody.capture(
                bodyInput, declaredLength, transferPolicy)) {
            response = dispatch(
                    executionId, inboundMethod, inboundHeaders, body, inboundPath, rawQuery,
                    verifiedClientIp, verifiedCertificateSerial);
        }

        StreamingResponseBody streamingBody = output -> {
            try (response) {
                response.transferTo(output, transferPolicy.ioBufferBytes());
                output.flush();
            }
        };
        return new ResponseEntity<>(streamingBody, response.headers(), response.status());
    }

    private CpfGatewayProxyResponse dispatch(
            String executionId,
            String inboundMethod,
            HttpHeaders inboundHeaders,
            CpfGatewayReplayableBody body,
            String inboundPath,
            String rawQuery,
            String verifiedClientIp,
            String verifiedCertificateSerial) {
        CpfGatewayRoute route = snapshot.resolve(executionId);
        String resolvedInboundPath = resolveInboundPath(route, inboundPath);
        String targetPath = CpfGatewayPathRewriter.rewrite(route.pathPattern(), route.targetPath(), resolvedInboundPath);
        HttpMethod method = httpMethod(inboundMethod);
        String gatewayTransactionId = UUID.randomUUID().toString();
        OffsetDateTime ledgerStartedAt = OffsetDateTime.now();
        String transactionId = trimToNull(CpfTransactionContext.transactionId());
        if (transactionId == null) transactionId = gatewayTransactionId;
        String traceId = Objects.toString(inboundHeaders.getFirst("traceparent"), transactionId);
        String gatewayInstanceId = CpfInstanceIdentity.current().serverInstanceId();
        LogPolicyDecision logPolicy = captureService.resolve(route.standardExecutionId());
        ledger.begin(new CpfGatewayLedgerPort.TransactionStart(
                gatewayTransactionId, transactionId, traceId,
                Objects.toString(inboundHeaders.getFirst(CpfHeaderNames.CHANNEL_CODE), ""),
                Objects.toString(verifiedClientIp, ""), 0, gatewayInstanceId, route.routeId(),
                route.standardExecutionId(), route.routeVersion(), route.expectedVersion(), route.routeVersion(),
                route.serverGroupId(), method.name(), targetPath, body.length(), ledgerStartedAt));
        CpfGatewayPrincipal principal = CpfGatewayPrincipal.anonymous();
        String auditReason = null;
        boolean terminalRecorded = false;
        try {
        safety.validateRoute(route);
        safety.validateLogPolicy(logPolicy);
        safety.validateRequest(inboundHeaders, body.length());
        captureService.captureRequest(gatewayTransactionId, rawQuery, inboundHeaders, body, logPolicy);
        if (!method.equals(httpMethod(route.httpMethod()))) {
            throw new IllegalArgumentException(
                    "Gateway route HTTP method 불일치. inbound=" + method + ", route=" + route.httpMethod());
        }
        if (method == HttpMethod.GET && body.length() > 0L) {
            throw new IllegalArgumentException("Gateway GET 요청에는 body를 허용하지 않습니다.");
        }
        String query = validateRawQuery(rawQuery);
        CpfGatewayRuntimePolicy.CorsDecision corsDecision = corsDecision(inboundHeaders, method.name());
        if (!corsDecision.allowed()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Gateway CORS 정책 거부: " + corsDecision.reason());
        }

        Map<String, String> credentials = credentialHeaders(
                inboundHeaders, verifiedClientIp, verifiedCertificateSerial);
        principal = Objects.requireNonNullElse(
                authenticationPort.authenticate(route, credentials), CpfGatewayPrincipal.anonymous());
        if (!runtimePolicy.tryAcquire(
                route.standardExecutionId(),
                principal.principalId(),
                inboundHeaders.getFirst(CpfHeaderNames.CHANNEL_CODE))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Gateway rate limit 초과");
        }

        Map<String, String> trusted = trustedHeaders(inboundHeaders, principal, route);
        CpfChannelPolicyDecision channelDecision = channelPolicyService.evaluate(
                route.standardExecutionId(),
                inboundHeaders.getFirst(CpfHeaderNames.ORIGINAL_CHANNEL_CODE),
                inboundHeaders.getFirst(CpfHeaderNames.CHANNEL_CODE),
                inboundHeaders.getFirst(CpfHeaderNames.REQUEST_TYPE),
                principal.authenticated(),
                requestSignatureVerified(principal));
        if (!channelDecision.allowed()) {
            throw new SecurityException(
                    "Gateway 채널 정책에서 요청을 거부했습니다. reason=" + channelDecision.reason());
        }
        if (route.requiredPermission() != null
                && !route.requiredPermission().isBlank()
                && !principal.authenticated()) {
            throw new SecurityException("보호 Gateway route에는 검증된 Principal이 필요합니다.");
        }
        if (!authorizationPort.isAllowed(route, trusted)) {
            throw new SecurityException(
                    "Gateway route 실행 권한이 없습니다. permission=" + route.requiredPermission());
        }

        auditReason = trimToNull(inboundHeaders.getFirst(CpfHeaderNames.AUDIT_REASON));
        if (route.auditReasonRequired()) {
            if (auditReason == null) {
                throw new IllegalArgumentException(
                        "Gateway 위험 거래에는 " + CpfHeaderNames.AUDIT_REASON + "가 필요합니다.");
            }
            if (!auditPort.durable()) {
                throw new IllegalStateException("Gateway 위험 거래용 durable Audit adapter가 구성되지 않았습니다.");
            }
            audit(route, principal, auditReason, "PRE_DISPATCH", "ACCEPTED", null, null);
        }

        CpfServiceCallCommand command = CpfServiceCallCommand.builder(route.serviceId())
                .httpMethod(route.httpMethod())
                .requestPath(targetPath)
                .timeoutMillis(route.overallTimeoutMs())
                .retryCount(route.maxRetryCount())
                .header(CpfHeaderNames.IDEMPOTENCY_KEY, inboundHeaders.getFirst(CpfHeaderNames.IDEMPOTENCY_KEY))
                .attribute("standardExecutionId", route.standardExecutionId())
                .attribute("requestBodyBytes", body.length())
                .build();
        HttpHeaders outbound = outboundHeaders(inboundHeaders, route);
        CpfServiceCallOutcome<CpfGatewayProxyResponse> result = serviceCallEngine.invoke(
                    command,
                    target -> invokeTarget(target, route, method, outbound, body, targetPath, query),
                    attempt -> recordAttempt(gatewayTransactionId, gatewayInstanceId, route, attempt));
            CpfServiceCallTarget ledgerTarget = result.target();
            OffsetDateTime attemptFinishedAt = OffsetDateTime.now();
            if (!"SUCCESS".equals(result.status()) || result.responseBody() == null) {
                ledger.complete(new CpfGatewayLedgerPort.TransactionCompletion(
                        gatewayTransactionId, ledgerTarget == null ? null : ledgerTarget.instanceId(),
                        result.status(), result.httpStatus() == null ? null : String.valueOf(result.httpStatus()),
                        result.failureCode(), "TARGET_CALL", "UNKNOWN_RESULT".equals(result.status()),
                        Duration.between(ledgerStartedAt, attemptFinishedAt).toMillis(), 0L, attemptFinishedAt));
                terminalRecorded = true;
                throw new CpfServiceCallFailedException(result);
            }
            CpfGatewayProxyResponse gatewayResponse = withGatewayResponseHeaders(
                    result.responseBody(), route, corsDecision);
            safety.validateResponse(gatewayResponse.headers());
            CpfGatewayProxyResponse boundedResponse = gatewayResponse.mapBody(
                    input -> new ResponseLimitInputStream(input, safety.responseBodyBytesCap()));
            CpfGatewayProxyResponse response = captureService.wrapResponse(
                    gatewayTransactionId, boundedResponse, logPolicy);
            try {
                if (route.auditReasonRequired()) {
                    audit(
                            route,
                            principal,
                            auditReason,
                            "POST_DISPATCH",
                            "SUCCESS",
                            result.target() == null ? null : result.target().instanceId(),
                            response.status());
                }
                CpfServiceCallTarget completedTarget = result.target();
                int responseStatus = response.status();
                return response.observe(new CpfGatewayProxyResponse.TransferObserver() {
                    @Override
                    public void completed(long transferredBytes) {
                        OffsetDateTime completedAt = OffsetDateTime.now();
                        ledger.complete(new CpfGatewayLedgerPort.TransactionCompletion(
                                gatewayTransactionId,
                                completedTarget == null ? null : completedTarget.instanceId(),
                                "SUCCESS", String.valueOf(responseStatus), null, null, false,
                                Duration.between(ledgerStartedAt, completedAt).toMillis(),
                                transferredBytes, completedAt));
                    }

                    @Override
                    public void failed(RuntimeException failure, long transferredBytes) {
                        completeStreamFailure(failure, "RESPONSE_STREAM_FAILED", transferredBytes);
                    }

                    @Override
                    public void abandoned(long transferredBytes) {
                        completeStreamFailure(
                                new IllegalStateException("Gateway response closed before full client transfer"),
                                "RESPONSE_STREAM_ABANDONED", transferredBytes);
                    }

                    private void completeStreamFailure(
                            RuntimeException failure, String code, long transferredBytes) {
                        OffsetDateTime failedAt = OffsetDateTime.now();
                        try { captureService.captureError(gatewayTransactionId, failure, logPolicy); }
                        catch (RuntimeException captureFailure) { failure.addSuppressed(captureFailure); }
                        ledger.complete(new CpfGatewayLedgerPort.TransactionCompletion(
                                gatewayTransactionId,
                                completedTarget == null ? null : completedTarget.instanceId(),
                                "UNKNOWN_RESULT", String.valueOf(responseStatus), code,
                                "RESPONSE_STREAM", true,
                                Duration.between(ledgerStartedAt, failedAt).toMillis(),
                                Math.max(0L, transferredBytes), failedAt));
                    }
                });
            } catch (RuntimeException ex) {
                response.close();
                throw ex;
            }
        } catch (RuntimeException ex) {
            OffsetDateTime failedAt = OffsetDateTime.now();
            try { captureService.captureError(gatewayTransactionId, ex, logPolicy); }
            catch (RuntimeException captureFailure) { ex.addSuppressed(captureFailure); }
            if (!terminalRecorded) {
                try {
                    ledger.complete(new CpfGatewayLedgerPort.TransactionCompletion(
                            gatewayTransactionId, null, failureStatus(ex), httpStatus(ex),
                            ex.getClass().getSimpleName(), failureStage(ex), unknownResult(ex),
                            Duration.between(ledgerStartedAt, failedAt).toMillis(), 0L, failedAt));
                    terminalRecorded = true;
                } catch (RuntimeException ledgerFailure) {
                    ex.addSuppressed(ledgerFailure);
                }
            }
            if (route.auditReasonRequired()) {
                try {
                    audit(route, principal, auditReason, "POST_DISPATCH", "FAILED", null, null);
                } catch (RuntimeException auditFailure) {
                    ex.addSuppressed(auditFailure);
                }
            }
            throw ex;
        }
    }

    public ResponseEntity<byte[]> preflight(String executionId, HttpHeaders inboundHeaders) {
        CpfGatewayRoute route = snapshot.resolve(executionId);
        safety.validateRoute(route);
        safety.validateRequest(inboundHeaders, 0L);
        String requestedMethod = inboundHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
        CpfGatewayRuntimePolicy.CorsDecision decision = corsDecision(
                inboundHeaders, requestedMethod == null ? route.httpMethod() : requestedMethod);
        if (!decision.allowed()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Gateway CORS 정책 거부: " + decision.reason());
        }
        HttpHeaders response = new HttpHeaders();
        applyCorsHeaders(response, decision);
        response.setAccessControlAllowMethods(List.of(httpMethod(route.httpMethod()), HttpMethod.OPTIONS));
        List<String> requestHeaders = accessControlRequestHeaders(inboundHeaders);
        if (!requestHeaders.isEmpty()) response.setAccessControlAllowHeaders(requestHeaders);
        return new ResponseEntity<>(new byte[0], response, HttpStatus.NO_CONTENT);
    }

    private void recordAttempt(
            String gatewayTransactionId,
            String gatewayInstanceId,
            CpfGatewayRoute route,
            CpfServiceCallAttempt attempt) {
        CpfServiceCallTarget target = attempt.target();
        URI uri = target == null || target.baseUrl() == null ? null : URI.create(target.baseUrl());
        ledger.recordAttempt(new CpfGatewayLedgerPort.Attempt(
                UUID.randomUUID().toString(), gatewayTransactionId, Math.max(1, attempt.attemptNo()),
                target == null ? null : target.instanceId(), uri == null ? null : uri.getHost(),
                uri == null ? null : effectivePort(uri), route.targetProtocol().name(), 0L,
                Math.max(0L, attempt.durationMillis()), attempt.status(),
                attempt.httpStatus() == null ? null : String.valueOf(attempt.httpStatus()),
                attempt.failureCode(), attempt.failureMessage(), gatewayInstanceId,
                attempt.failover() ? "FAILOVER" : "PRIMARY", attempt.unknownResult(),
                OffsetDateTime.ofInstant(attempt.startedAt(), java.time.ZoneOffset.UTC),
                OffsetDateTime.ofInstant(attempt.finishedAt(), java.time.ZoneOffset.UTC)));
    }

    private CpfGatewayProxyResponse invokeTarget(
            CpfServiceCallTarget target,
            CpfGatewayRoute route,
            HttpMethod method,
            HttpHeaders outbound,
            CpfGatewayReplayableBody body,
            String targetPath,
            String rawQuery) {
        URI uri = targetUri(target.baseUrl(), targetPath, rawQuery);
        return httpExchange.exchange(
                uri,
                method,
                outbound,
                body,
                new CpfGatewayHttpExchangePort.TimeoutPolicy(
                        route.connectTimeoutMs(),
                        route.responseTimeoutMs(),
                        Math.min(transferPolicy.requestTimeoutMillis(), route.overallTimeoutMs())));
    }

    private CpfGatewayProxyResponse withGatewayResponseHeaders(
            CpfGatewayProxyResponse downstream,
            CpfGatewayRoute route,
            CpfGatewayRuntimePolicy.CorsDecision corsDecision) {
        HttpHeaders headers = new HttpHeaders();
        downstream.headers().forEach((name, values) -> {
            String lower = name.toLowerCase(Locale.ROOT);
            if (!HOP_BY_HOP.contains(lower) && runtimePolicy.allowResponseHeader(lower)) {
                headers.put(name, List.copyOf(values));
            }
        });
        headers.set(CpfHeaderNames.GATEWAY_INSTANCE_ID, CpfInstanceIdentity.current().serverInstanceId());
        headers.set(CpfHeaderNames.GATEWAY_ROUTE_ID, route.standardExecutionId());
        headers.set(CpfHeaderNames.GATEWAY_ROUTE_VERSION, route.routeVersion());
        applyCorsHeaders(headers, corsDecision);
        return downstream.replaceHeaders(headers);
    }

    private CpfGatewayRuntimePolicy.CorsDecision corsDecision(HttpHeaders headers, String method) {
        return runtimePolicy.evaluateCors(
                headers.getOrigin(), method, accessControlRequestHeaders(headers));
    }

    private List<String> accessControlRequestHeaders(HttpHeaders headers) {
        List<String> values = headers.get(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
        if (values == null || values.isEmpty()) return List.of();
        ArrayList<String> result = new ArrayList<>();
        for (String value : values) {
            for (String part : value.split(",")) {
                if (!part.isBlank()) result.add(part.trim());
            }
        }
        return List.copyOf(result);
    }

    private void applyCorsHeaders(
            HttpHeaders headers, CpfGatewayRuntimePolicy.CorsDecision decision) {
        if (decision == null || decision.allowOrigin().isBlank()) return;
        headers.setAccessControlAllowOrigin(decision.allowOrigin());
        headers.setAccessControlAllowCredentials(decision.allowCredentials());
        headers.setAccessControlMaxAge(decision.maxAgeSeconds());
        if (!decision.exposedHeaders().isEmpty()) {
            headers.setAccessControlExposeHeaders(new ArrayList<>(decision.exposedHeaders()));
        }
        headers.add(HttpHeaders.VARY, HttpHeaders.ORIGIN);
    }

    private HttpHeaders outboundHeaders(HttpHeaders inbound, CpfGatewayRoute route) {
        HttpHeaders result = new HttpHeaders();
        inbound.forEach((name, values) -> {
            String lower = name.toLowerCase(Locale.ROOT);
            if (PASSTHROUGH.contains(lower)
                    && runtimePolicy.allowRequestHeader(lower)
                    && !HOP_BY_HOP.contains(lower)
                    && !NEVER_FORWARD.contains(lower)) {
                result.put(name, List.copyOf(values));
            }
        });
        result.set(CpfHeaderNames.STANDARD_EXECUTION_ID, route.standardExecutionId());
        result.set(CpfHeaderNames.GATEWAY_INSTANCE_ID, CpfInstanceIdentity.current().serverInstanceId());
        result.set(CpfHeaderNames.GATEWAY_ROUTE_ID, route.standardExecutionId());
        result.set(CpfHeaderNames.GATEWAY_ROUTE_VERSION, route.routeVersion());
        result.set(CpfHeaderNames.INGRESS_TYPE, "CPF_GATEWAY");
        return result;
    }

    private Map<String, String> credentialHeaders(
            HttpHeaders headers, String verifiedClientIp, String verifiedCertificateSerial) {
        Map<String, String> result = new LinkedHashMap<>();
        copyFirst(headers, result, CpfHeaderNames.AUTHORIZATION);
        copyFirst(headers, result, CpfHeaderNames.API_KEY);
        copyFirst(headers, result, CpfHeaderNames.REQUEST_SIGNATURE);
        if (verifiedClientIp != null && !verifiedClientIp.isBlank()) {
            result.put("cpf.client.ip", verifiedClientIp.trim());
        }
        if (verifiedCertificateSerial != null && !verifiedCertificateSerial.isBlank()) {
            result.put("cpf.client.cert.serial", verifiedCertificateSerial.trim());
        }
        return Map.copyOf(result);
    }

    private Map<String, String> trustedHeaders(
            HttpHeaders headers, CpfGatewayPrincipal principal, CpfGatewayRoute route) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String name : List.of(
                CpfHeaderNames.ORIGINAL_CHANNEL_CODE,
                CpfHeaderNames.CHANNEL_CODE,
                CpfHeaderNames.REQUEST_TYPE)) {
            copyFirst(headers, result, name);
        }
        // 실행 ID는 외부 입력을 신뢰하지 않고 이미 resolve된 Route를 정본으로 사용합니다.
        result.put(CpfHeaderNames.STANDARD_EXECUTION_ID, route.standardExecutionId());
        if (principal.authenticated()) {
            result.put("cpf.principal.id", principal.principalId());
            result.put("cpf.principal.authorities", String.join(",", principal.authorities()));
        }
        principal.attributes().forEach((key, value) -> result.put("cpf.principal." + key, value));
        return Map.copyOf(result);
    }

    private void audit(
            CpfGatewayRoute route,
            CpfGatewayPrincipal principal,
            String reason,
            String phase,
            String outcome,
            String target,
            Integer status) {
        auditPort.record(new CpfGatewayAuditEvent(
                CpfTransactionContext.transactionId(),
                route.standardExecutionId(),
                principal.principalId(),
                reason,
                phase,
                outcome,
                target,
                status,
                Instant.now(),
                Map.of("routeVersion", Objects.toString(route.routeVersion(), ""))));
    }

    private static final class ResponseLimitInputStream extends FilterInputStream {
        private final long maxBytes;
        private long observed;

        private ResponseLimitInputStream(InputStream input, long maxBytes) {
            super(input);
            this.maxBytes = maxBytes;
        }

        @Override
        public int read() throws IOException {
            int value = super.read();
            if (value >= 0) count(1L);
            return value;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int read = super.read(buffer, offset, length);
            if (read > 0) count(read);
            return read;
        }

        private void count(long bytes) throws IOException {
            observed += bytes;
            if (observed > maxBytes) {
                throw new IOException("Gateway response body 안전 상한을 초과했습니다.");
            }
        }
    }

    private static String failureStage(RuntimeException ex) {
        if (ex instanceof ResponseStatusException status && status.getStatusCode().value() == 429) return "RATE_LIMIT";
        if (ex instanceof SecurityException) return "AUTHORIZATION";
        if (ex instanceof IllegalArgumentException) return "REQUEST_VALIDATION";
        if (ex instanceof CpfServiceCallFailedException) return "TARGET_CALL";
        return "GATEWAY_PIPELINE";
    }

    private static String failureStatus(RuntimeException ex) {
        return unknownResult(ex) ? "UNKNOWN_RESULT" : "FAILED";
    }

    private static String httpStatus(RuntimeException ex) {
        return ex instanceof ResponseStatusException status
                ? Integer.toString(status.getStatusCode().value()) : null;
    }

    private static boolean unknownResult(RuntimeException ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof java.net.SocketTimeoutException
                    || current instanceof java.io.InterruptedIOException
                    || current instanceof java.util.concurrent.TimeoutException) return true;
            current = current.getCause();
        }
        return false;
    }

    private static String resolveInboundPath(CpfGatewayRoute route, String inboundPath) {
        if (inboundPath != null && !inboundPath.isBlank()) {
            return CpfGatewayPathRewriter.normalizeRequestPath(inboundPath);
        }
        String pattern = route.pathPattern();
        if (pattern.contains("*") || pattern.contains("{")) {
            throw new IllegalArgumentException(
                    "동적 Gateway Route에는 실제 ingress path가 필요합니다. routeId=" + route.routeId());
        }
        return CpfGatewayPathRewriter.normalizeRequestPath(pattern);
    }

    private static int effectivePort(URI uri) {
        if (uri.getPort() > 0) return uri.getPort();
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    private URI targetUri(String baseUrl, String endpoint, String rawQuery) {
        try {
            URI base = new URI(requireText(baseUrl, "baseUrl").trim());
            if (!("http".equalsIgnoreCase(base.getScheme()) || "https".equalsIgnoreCase(base.getScheme()))
                    || base.getHost() == null
                    || base.getUserInfo() != null
                    || base.getQuery() != null
                    || base.getFragment() != null) {
                throw new IllegalArgumentException(
                        "Gateway 대상 baseUrl은 userInfo/query/fragment 없는 http(s) absolute URI여야 합니다.");
            }
            String path = validateEndpointPath(endpoint);
            String basePath = base.getPath() == null ? "" : base.getPath();
            while (basePath.endsWith("/")) basePath = basePath.substring(0, basePath.length() - 1);
            URI withoutQuery = new URI(
                    base.getScheme(), null, base.getHost(), base.getPort(), basePath + path, null, null);
            return rawQuery == null || rawQuery.isBlank()
                    ? withoutQuery
                    : URI.create(withoutQuery.toASCIIString() + "?" + rawQuery);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Gateway 대상 URI가 올바르지 않습니다.", ex);
        }
    }

    private boolean requestSignatureVerified(CpfGatewayPrincipal principal) {
        String value = principal.attributes().get("requestSignatureVerified");
        return value != null && Boolean.parseBoolean(value);
    }

    private String validateEndpointPath(String endpoint) {
        String path = normalizePath(endpoint);
        String lower = path.toLowerCase(Locale.ROOT);
        if (path.indexOf('\\') >= 0
                || path.contains("//")
                || path.chars().anyMatch(ch -> ch < 0x20 || ch == 0x7f)
                || lower.contains("%2e")
                || lower.contains("%2f")
                || lower.contains("%5c")) {
            throw new IllegalArgumentException("Gateway endpoint path가 안전하지 않습니다.");
        }
        try {
            URI relative = new URI(null, null, path, null);
            String normalized = relative.normalize().getPath();
            if (!path.equals(normalized) || path.contains("..") || !path.startsWith("/")) {
                throw new IllegalArgumentException("Gateway endpoint path 정규화 결과가 안전하지 않습니다.");
            }
            return path;
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Gateway endpoint path가 올바르지 않습니다.", ex);
        }
    }

    private String validateRawQuery(String value) {
        if (value == null || value.isBlank()) return null;
        if (value.length() > MAX_QUERY_LENGTH) {
            throw new IllegalArgumentException("Gateway query 길이가 허용 범위를 초과했습니다.");
        }
        if (value.indexOf('#') >= 0 || value.chars().anyMatch(ch -> ch < 0x20 || ch == 0x7f)) {
            throw new IllegalArgumentException("Gateway query에 허용되지 않은 문자가 있습니다.");
        }
        return value;
    }

    private HttpMethod httpMethod(String value) {
        String normalized = requireText(value, "httpMethod").toUpperCase(Locale.ROOT);
        HttpMethod method = HttpMethod.valueOf(normalized);
        if (!(method == HttpMethod.GET
                || method == HttpMethod.POST
                || method == HttpMethod.PUT
                || method == HttpMethod.PATCH
                || method == HttpMethod.DELETE)) {
            throw new IllegalArgumentException("허용되지 않은 Gateway HTTP method: " + value);
        }
        return method;
    }

    private String normalizePath(String value) {
        String normalized = value == null || value.isBlank() ? "/" : value.trim();
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }

    private String requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + "가 필요합니다.");
        return value;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void copyFirst(HttpHeaders source, Map<String, String> target, String name) {
        String value = source.getFirst(name);
        if (value != null) target.put(name, value);
    }
}
