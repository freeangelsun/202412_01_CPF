package com.cpf.integration.http.internal.servicecall;

import com.cpf.platform.operations.reconciliation.CpfReconciliationPort;
import com.cpf.platform.operations.reconciliation.CpfUnknownResultRecord;
import com.cpf.platform.operations.observability.api.lineage.CpfLineageRecord;
import com.cpf.integration.api.servicecall.CpfServiceCallResponseMetadata;
import com.cpf.integration.api.servicecall.CpfServiceCallBusinessException;
import com.cpf.integration.api.servicecall.CpfServiceCallTransportException;
import com.cpf.platform.operations.observability.api.lineage.CpfLineageRecorder;
import com.cpf.platform.operations.observability.api.logging.CpfTransactionSegmentPort;
import com.cpf.platform.operations.observability.api.logging.CpfTransactionSegmentPort.SegmentScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * CPF 표준 서비스 호출 엔진입니다.
 *
 * <p>레지스트리 조회, endpoint/instance 선택, retry/failover/circuit 상태 전이, call history 기록을
 * 한 곳에서 처리합니다. 실제 HTTP 전송은 {@code CpfWebClient}나 Remote Facade Proxy가 넘긴
 * 호출 함수가 수행하고, 엔진은 선택된 {@link ServiceCallResolvedTarget}을 호출 함수에 전달합니다.</p>
 */
public class CpfServiceCallEngine {
    private static final Logger log = LoggerFactory.getLogger(CpfServiceCallEngine.class);

    private final CpfEndpointResolver endpointResolver;
    private final CpfServiceCallLogWriter logWriter;
    private final CpfServiceCallProperties properties;
    private final CpfTransactionSegmentPort segmentService;
    private final CpfReconciliationPort reconciliationPort;
    private final CpfLineageRecorder lineageRecorder;
    private final Clock clock;
    private final Supplier<String> recoveryIdSupplier;

    public CpfServiceCallEngine(
            CpfEndpointResolver endpointResolver,
            CpfServiceCallLogWriter logWriter,
            CpfServiceCallProperties properties) {
        this(endpointResolver, logWriter, properties, null, null, null, Clock.systemUTC(), () -> UUID.randomUUID().toString());
    }

    public CpfServiceCallEngine(
            CpfEndpointResolver endpointResolver,
            CpfServiceCallLogWriter logWriter,
            CpfServiceCallProperties properties,
            CpfTransactionSegmentPort segmentService,
            CpfReconciliationPort reconciliationPort) {
        this(endpointResolver, logWriter, properties, segmentService, reconciliationPort, null, Clock.systemUTC(), () -> UUID.randomUUID().toString());
    }

    public CpfServiceCallEngine(
            CpfEndpointResolver endpointResolver,
            CpfServiceCallLogWriter logWriter,
            CpfServiceCallProperties properties,
            CpfTransactionSegmentPort segmentService,
            CpfReconciliationPort reconciliationPort,
            CpfLineageRecorder lineageRecorder) {
        this(endpointResolver, logWriter, properties, segmentService, reconciliationPort, lineageRecorder,
                Clock.systemUTC(), () -> UUID.randomUUID().toString());
    }

    /** 시간과 복구 ID 정책을 주입해 Runtime/Test에서 동일한 실행 생명주기를 재현할 수 있게 합니다. */
    public CpfServiceCallEngine(
            CpfEndpointResolver endpointResolver,
            CpfServiceCallLogWriter logWriter,
            CpfServiceCallProperties properties,
            CpfTransactionSegmentPort segmentService,
            CpfReconciliationPort reconciliationPort,
            CpfLineageRecorder lineageRecorder,
            Clock clock,
            Supplier<String> recoveryIdSupplier) {
        this.endpointResolver = java.util.Objects.requireNonNull(endpointResolver, "endpointResolver");
        this.logWriter = java.util.Objects.requireNonNull(logWriter, "logWriter");
        this.properties = java.util.Objects.requireNonNull(properties, "properties");
        this.segmentService = segmentService;
        this.reconciliationPort = reconciliationPort;
        this.lineageRecorder = lineageRecorder;
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
        this.recoveryIdSupplier = java.util.Objects.requireNonNull(recoveryIdSupplier, "recoveryIdSupplier");
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public boolean fallbackToConfiguredEndpoint() {
        return properties.isFallbackToConfiguredEndpoint();
    }

    public ServiceCallResolvedTarget resolve(ServiceCallRequest request) {
        return endpointResolver.resolve(applyRequestDefaults(request));
    }

    public <T> ServiceCallResult<T> invoke(ServiceCallRequest request, Supplier<T> remoteCall) {
        return invoke(request, ignored -> remoteCall.get());
    }

    public <T> ServiceCallResult<T> invoke(
            ServiceCallRequest request,
            Function<ServiceCallResolvedTarget, T> remoteCall) {
        return invoke(request, remoteCall, ServiceCallAttemptObserver.noOp());
    }

    <T> ServiceCallResult<T> invoke(
            ServiceCallRequest request,
            Function<ServiceCallResolvedTarget, T> remoteCall,
            ServiceCallAttemptObserver observer) {
        ServiceCallRequest requested = applyRequestDefaults(request);
        Set<String> excludedInstanceIds = new LinkedHashSet<>();
        ServiceCallResult<T> lastFailure = null;
        int maxAttempts = maxAttempts(requested);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            ServiceCallResolvedTarget target = endpointResolver.resolve(requested, excludedInstanceIds);
            ServiceCallRequest effectiveRequest = applyTargetDefaults(requested, target);
            boolean failover = !excludedInstanceIds.isEmpty();
            SegmentScope scope = startAttempt(effectiveRequest, target, attempt, failover);
            Instant attemptStartedAt = clock.instant();
            if (logWriter.isCircuitOpen(target, properties.getCircuitOpenRetryAfterMillis())) {
                logWriter.write(
                        effectiveRequest,
                        target,
                        "CIRCUIT_OPEN",
                        null,
                        0,
                        "CIRCUIT_OPEN",
                        "서비스 호출 circuit이 OPEN 상태입니다.");
                failScope(scope, target, attempt, failover, "OPEN", null,
                        "TECHNICAL_FAILURE", null, "CIRCUIT_OPEN", "서비스 호출 circuit이 OPEN 상태입니다.");
                observer.onAttempt(new ServiceCallAttemptEvent(
                        attempt, target, failover, "TECHNICAL_FAILURE", null, 0L, "CIRCUIT_OPEN",
                        "서비스 호출 circuit이 OPEN 상태입니다.", false, attemptStartedAt, clock.instant()));
                return ServiceCallResult.failure(target, null, 0L, attempt, "CIRCUIT_OPEN", "서비스 호출 circuit이 OPEN 상태입니다.");
            }

            long started = System.nanoTime();
            try {
                T response = remoteCall.apply(target);
                long elapsed = elapsedMillis(started);
                Integer responseStatus = responseStatus(response);
                logWriter.write(effectiveRequest, target, "SUCCESS", responseStatus, elapsed, null, null);
                logWriter.markSuccess(target, responseStatus, elapsed);
                successScope(scope, target, attempt, failover, responseStatus);
                recordLineage(effectiveRequest, target, "SUCCESS", attempt, elapsed);
                observer.onAttempt(new ServiceCallAttemptEvent(
                        attempt, target, failover, "SUCCESS", responseStatus, elapsed, null, null,
                        false, attemptStartedAt, clock.instant()));
                return ServiceCallResult.success(target, response, responseStatus, elapsed, attempt);
            } catch (RuntimeException ex) {
                long elapsed = elapsedMillis(started);
                Integer httpStatus = httpStatus(ex);
                String failureCode = ex instanceof CpfServiceCallBusinessException businessFailure
                        ? businessFailure.failureCode() : ex.getClass().getSimpleName();
                String failureMessage = safeMessage(ex);
                if (ex instanceof CpfServiceCallBusinessException businessFailure) {
                    Integer businessHttpStatus = businessFailure.httpStatus() != null ? businessFailure.httpStatus() : httpStatus;
                    logWriter.write(effectiveRequest, target, "BUSINESS_FAILURE", businessHttpStatus, elapsed, failureCode, failureMessage);
                    failScope(scope, target, attempt, failover, "CLOSED", businessHttpStatus,
                            "BUSINESS_FAILURE", null, failureCode, failureMessage);
                    recordLineage(effectiveRequest, target, "BUSINESS_FAILURE", attempt, elapsed);
                    observer.onAttempt(new ServiceCallAttemptEvent(
                            attempt, target, failover, "BUSINESS_FAILURE", businessHttpStatus, elapsed, failureCode,
                            failureMessage, false, attemptStartedAt, clock.instant()));
                    return ServiceCallResult.businessFailure(target, businessHttpStatus, elapsed, attempt, failureCode, failureMessage);
                }
                logWriter.write(effectiveRequest, target, "TECHNICAL_FAILURE", httpStatus, elapsed, failureCode, failureMessage);
                logWriter.markFailure(target, httpStatus, elapsed, failureMessage, properties.getCircuitOpenFailureThreshold());
                boolean unknown = isUnknownResult(ex);
                boolean retryable = isRetryable(effectiveRequest, httpStatus, ex);
                boolean terminalUnknown = unknown && (!retryable || attempt >= maxAttempts);
                String unknownId = terminalUnknown
                        ? registerUnknown(effectiveRequest, scope, target, failureCode, failureMessage)
                        : null;
                String resultState = terminalUnknown ? "UNKNOWN" : "TECHNICAL_FAILURE";
                failScope(scope, target, attempt, failover, "CLOSED", httpStatus,
                        resultState, unknownId, failureCode, failureMessage);
                lastFailure = terminalUnknown
                        ? ServiceCallResult.unknown(target, elapsed, attempt, failureCode, failureMessage, unknownId, "POLL_OR_MANUAL_RECONCILIATION")
                        : ServiceCallResult.failure(target, httpStatus, elapsed, attempt, failureCode, failureMessage);
                recordLineage(effectiveRequest, target, resultState, attempt, elapsed);
                observer.onAttempt(new ServiceCallAttemptEvent(
                        attempt, target, failover, terminalUnknown ? "UNKNOWN_RESULT" : "TECHNICAL_FAILURE",
                        httpStatus, elapsed, failureCode, failureMessage, terminalUnknown,
                        attemptStartedAt, clock.instant()));
                if (!retryable) {
                    return lastFailure;
                }
                excludeForFailover(target, excludedInstanceIds);
                if (!target.failoverEnabled() && attempt >= maxAttempts) {
                    return lastFailure;
                }
                if (attempt < maxAttempts) {
                    backoff(attempt);
                }
            }
        }
        return lastFailure != null
                ? lastFailure
                : ServiceCallResult.failure(null, null, 0L, 0, "SERVICE_CALL_NOT_EXECUTED", "서비스 호출이 실행되지 않았습니다.");
    }

    private ServiceCallRequest applyRequestDefaults(ServiceCallRequest request) {
        return new ServiceCallRequest(
                request.serviceId(),
                request.endpointCode(),
                request.instanceId(),
                defaultIfBlank(request.httpMethod(), "GET"),
                defaultIfBlank(request.requestPath(), "/"),
                request.timeoutMillis(),
                request.retryCount(),
                request.headers(),
                request.attributes());
    }

    private ServiceCallRequest applyTargetDefaults(ServiceCallRequest request, ServiceCallResolvedTarget target) {
        return new ServiceCallRequest(
                request.serviceId(),
                target.endpointCode(),
                target.instanceId(),
                defaultIfBlank(request.httpMethod(), "GET"),
                defaultIfBlank(request.requestPath(), "/"),
                firstPositive(request.timeoutMillis(), intValue(target.endpoint(), "defaultTimeoutMs"), properties.getDefaultTimeoutMillis()),
                firstNonNegative(request.retryCount(), intValue(target.endpoint(), "defaultRetryCount"), properties.getDefaultRetryCount()),
                request.headers(),
                request.attributes());
    }

    private int maxAttempts(ServiceCallRequest request) {
        int retryCount = firstNonNegative(request.retryCount(), properties.getDefaultRetryCount());
        int boundedRetry = Math.max(0, Math.min(retryCount, properties.getMaxRetryCount()));
        return Math.max(1, boundedRetry + 1);
    }

    private void excludeForFailover(ServiceCallResolvedTarget target, Set<String> excludedInstanceIds) {
        String instanceId = target == null ? null : target.instanceId();
        if (instanceId != null && !instanceId.isBlank()) {
            excludedInstanceIds.add(instanceId);
        }
    }

    private Integer responseStatus(Object response) {
        if (response instanceof CpfServiceCallResponseMetadata metadata) {
            Integer status = metadata.httpStatus();
            return status == null || status < 100 || status > 599 ? 200 : status;
        }
        return 200;
    }

    private Integer httpStatus(RuntimeException ex) {
        CpfServiceCallTransportException transportFailure = transportFailure(ex);
        if (transportFailure != null) {
            return transportFailure.httpStatus();
        }
        if (ex instanceof WebClientResponseException responseException) {
            return responseException.getStatusCode().value();
        }
        if (ex instanceof RestClientResponseException responseException) {
            return responseException.getStatusCode().value();
        }
        return null;
    }

    /**
     * 명백한 Client 오류는 다른 인스턴스로 보내도 결과가 바뀌지 않으므로 재시도하지 않습니다.
     * POST/PATCH처럼 본질적으로 비멱등인 호출은 표준 멱등키가 있을 때만 자동 재시도합니다.
     */
    private boolean isRetryable(ServiceCallRequest request, Integer httpStatus, RuntimeException ex) {
        CpfServiceCallTransportException transportFailure = transportFailure(ex);
        boolean transportRetryable;
        if (transportFailure != null) {
            transportRetryable = transportFailure.retryable();
        } else if (httpStatus == null) {
            transportRetryable = true;
        } else if (httpStatus == 408 || httpStatus == 425 || httpStatus == 429) {
            transportRetryable = true;
        } else {
            transportRetryable = httpStatus >= 500;
        }
        return transportRetryable && retrySafe(request);
    }

    private boolean retrySafe(ServiceCallRequest request) {
        String method = defaultIfBlank(request.httpMethod(), "GET").toUpperCase();
        if (Set.of("GET", "HEAD", "OPTIONS", "PUT", "DELETE").contains(method)) {
            return true;
        }
        if (!("POST".equals(method) || "PATCH".equals(method))) {
            return false;
        }
        String idempotencyKey = firstText(
                headerIgnoreCase(request.headers(), "X-Cpf-Idempotency-Key"),
                stringValue(request.attributes().get("idempotencyKey")));
        return idempotencyKey != null && !idempotencyKey.isBlank();
    }

    private String headerIgnoreCase(Map<String, String> headers, String name) {
        if (headers == null || headers.isEmpty()) return null;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private CpfServiceCallTransportException transportFailure(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current instanceof CpfServiceCallTransportException transportException) {
                return transportException;
            }
            current = current.getCause();
        }
        return null;
    }

    private String safeMessage(RuntimeException ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return ex.getClass().getSimpleName();
        }
        return message.length() > 900 ? message.substring(0, 900) : message;
    }

    private int intValue(Map<String, Object> row, String key) {
        if (row == null || !row.containsKey(key) || row.get(key) == null) {
            return -1;
        }
        Object value = row.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private int firstPositive(Integer... values) {
        for (Integer value : values) {
            if (value != null && value > 0) {
                return value;
            }
        }
        return properties.getDefaultTimeoutMillis();
    }

    private int firstNonNegative(Integer... values) {
        for (Integer value : values) {
            if (value != null && value >= 0) {
                return value;
            }
        }
        return properties.getDefaultRetryCount();
    }


    private void recordLineage(
            ServiceCallRequest request,
            ServiceCallResolvedTarget target,
            String state,
            int attempt,
            long elapsedMillis) {
        if (lineageRecorder == null || request == null) {
            return;
        }
        try {
            String transactionId = firstText(
                    request.headers().get("X-Cpf-Transaction-Id"),
                    stringValue(request.attributes().get("transactionId")));
            String endpoint = target == null ? request.endpointCode() : target.endpointCode();
            String instance = target == null ? request.instanceId() : target.instanceId();
            lineageRecorder.record(new CpfLineageRecord(
                    transactionId,
                    "CPF",
                    request.requestPath(),
                    request.serviceId(),
                    endpoint,
                    request.httpMethod(),
                    clock.instant(),
                    Map.of(
                            "state", defaultIfBlank(state, "UNKNOWN"),
                            "instanceId", defaultIfBlank(instance, "-"),
                            "attempt", String.valueOf(attempt),
                            "durationMillis", String.valueOf(Math.max(0L, elapsedMillis)))));
        } catch (RuntimeException lineageFailure) {
            log.warn("CPF lineage 기록 실패를 서비스 호출 결과로 전파하지 않습니다. serviceId={}", request.serviceId(), lineageFailure);
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String firstText(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private long elapsedMillis(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }

    private SegmentScope startAttempt(
            ServiceCallRequest request,
            ServiceCallResolvedTarget target,
            int attempt,
            boolean failover) {
        if (segmentService == null) {
            return null;
        }
        String sourceModule = textAttribute(request, "sourceModuleCode", "CPF");
        SegmentScope scope = segmentService.start(
                CpfTransactionSegmentPort.Role.EXTERNAL,
                CpfTransactionSegmentPort.Direction.OUTBOUND,
                sourceModule,
                sourceModule,
                request.serviceId().toUpperCase(),
                request.requestPath(),
                "Service Call " + request.serviceId() + " attempt " + attempt);
        scope.update(new CpfTransactionSegmentPort.SegmentAttributes(
                target.instanceId(), attempt, attempt > 1, failover, "CLOSED", null, "RUNNING", null));
        return scope;
    }

    private void successScope(
            SegmentScope scope,
            ServiceCallResolvedTarget target,
            int attempt,
            boolean failover,
            Integer httpStatus) {
        if (scope == null) {
            return;
        }
        scope.update(new CpfTransactionSegmentPort.SegmentAttributes(
                target.instanceId(), attempt, attempt > 1, failover, "CLOSED", httpStatus, "SUCCESS", null));
        scope.success();
    }

    private void failScope(
            SegmentScope scope,
            ServiceCallResolvedTarget target,
            int attempt,
            boolean failover,
            String circuitState,
            Integer httpStatus,
            String resultState,
            String unknownId,
            String failureCode,
            String failureMessage) {
        if (scope == null) {
            return;
        }
        scope.update(new CpfTransactionSegmentPort.SegmentAttributes(
                target != null ? target.instanceId() : null, attempt, attempt > 1, failover,
                circuitState, httpStatus, resultState, unknownId));
        scope.fail(failureCode, failureMessage);
    }

    private String requireRecoveryId(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            throw new IllegalStateException("UNKNOWN 결과 복구 ID 생성에 실패했습니다.");
        }
        return candidate.trim();
    }

    private String registerUnknown(
            ServiceCallRequest request,
            SegmentScope scope,
            ServiceCallResolvedTarget target,
            String failureCode,
            String failureMessage) {
        String unknownId = requireRecoveryId(recoveryIdSupplier.get());
        if (reconciliationPort == null) {
            return unknownId;
        }
        try {
            reconciliationPort.register(new CpfUnknownResultRecord(
                    unknownId,
                    "SERVICE_CALL",
                    "CHECK_PENDING",
                    scope != null ? scope.transactionId() : textAttribute(request, "transactionId", null),
                    scope != null ? scope.transactionSegmentId() : null,
                    textAttribute(request, "externalKey", target != null ? target.instanceId() : null),
                    failureCode,
                    failureMessage,
                    "POLL_OR_MANUAL_RECONCILIATION",
                    clock.instant(),
                    null));
        } catch (RuntimeException ex) {
            log.error("서비스 호출 unknown 결과 등록에 실패했습니다. unknownId={}", unknownId, ex);
        }
        return unknownId;
    }

    private boolean isUnknownResult(RuntimeException failure) {
        CpfServiceCallTransportException transportFailure = transportFailure(failure);
        if (transportFailure != null) {
            return transportFailure.unknownResult();
        }
        Throwable current = failure;
        while (current != null) {
            String name = current.getClass().getName().toLowerCase();
            if (name.contains("timeout") || name.contains("timedout")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private void backoff(int attempt) {
        long initial = Math.max(0L, properties.getRetryBackoffMillis());
        long maximum = Math.max(initial, properties.getMaxRetryBackoffMillis());
        long delay = Math.min(maximum, initial * (1L << Math.min(Math.max(0, attempt - 1), 20)));
        if (delay <= 0) {
            return;
        }
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("서비스 호출 retry 대기가 중단되었습니다.", ex);
        }
    }

    private String textAttribute(ServiceCallRequest request, String key, String fallback) {
        Object value = request.attributes().get(key);
        return value == null || String.valueOf(value).isBlank() ? fallback : String.valueOf(value).trim();
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
