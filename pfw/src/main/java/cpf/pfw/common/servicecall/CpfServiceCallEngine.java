package cpf.pfw.common.servicecall;

import cpf.pfw.common.logging.segment.TransactionSegmentDirection;
import cpf.pfw.common.logging.segment.TransactionSegmentRole;
import cpf.pfw.common.logging.segment.TransactionSegmentScope;
import cpf.pfw.common.logging.segment.TransactionSegmentService;
import cpf.pfw.common.reconciliation.CpfReconciliationPort;
import cpf.pfw.common.reconciliation.CpfUnknownResultRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
    private final TransactionSegmentService segmentService;
    private final CpfReconciliationPort reconciliationPort;

    public CpfServiceCallEngine(
            CpfEndpointResolver endpointResolver,
            CpfServiceCallLogWriter logWriter,
            CpfServiceCallProperties properties) {
        this(endpointResolver, logWriter, properties, null, null);
    }

    public CpfServiceCallEngine(
            CpfEndpointResolver endpointResolver,
            CpfServiceCallLogWriter logWriter,
            CpfServiceCallProperties properties,
            TransactionSegmentService segmentService,
            CpfReconciliationPort reconciliationPort) {
        this.endpointResolver = endpointResolver;
        this.logWriter = logWriter;
        this.properties = properties;
        this.segmentService = segmentService;
        this.reconciliationPort = reconciliationPort;
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
        ServiceCallRequest requested = applyRequestDefaults(request);
        Set<String> excludedInstanceIds = new LinkedHashSet<>();
        ServiceCallResult<T> lastFailure = null;
        int maxAttempts = maxAttempts(requested);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            ServiceCallResolvedTarget target = endpointResolver.resolve(requested, excludedInstanceIds);
            ServiceCallRequest effectiveRequest = applyTargetDefaults(requested, target);
            TransactionSegmentScope scope = startAttempt(effectiveRequest, target, attempt, !excludedInstanceIds.isEmpty());
            if (logWriter.isCircuitOpen(target, properties.getCircuitOpenRetryAfterMillis())) {
                logWriter.write(
                        effectiveRequest,
                        target,
                        "CIRCUIT_OPEN",
                        null,
                        0,
                        "CIRCUIT_OPEN",
                        "서비스 호출 circuit이 OPEN 상태입니다.");
                failScope(scope, target, attempt, !excludedInstanceIds.isEmpty(), "OPEN", null,
                        "FAILED", null, "CIRCUIT_OPEN", "서비스 호출 circuit이 OPEN 상태입니다.");
                return ServiceCallResult.failure(target, null, 0L, attempt, "CIRCUIT_OPEN", "서비스 호출 circuit이 OPEN 상태입니다.");
            }

            long started = System.nanoTime();
            try {
                T response = remoteCall.apply(target);
                long elapsed = elapsedMillis(started);
                logWriter.write(effectiveRequest, target, "SUCCESS", 200, elapsed, null, null);
                logWriter.markSuccess(target, 200, elapsed);
                successScope(scope, target, attempt, !excludedInstanceIds.isEmpty(), 200);
                return ServiceCallResult.success(target, response, 200, elapsed, attempt);
            } catch (RuntimeException ex) {
                long elapsed = elapsedMillis(started);
                Integer httpStatus = httpStatus(ex);
                String failureCode = ex.getClass().getSimpleName();
                String failureMessage = safeMessage(ex);
                logWriter.write(effectiveRequest, target, "FAILED", httpStatus, elapsed, failureCode, failureMessage);
                logWriter.markFailure(target, httpStatus, elapsed, failureMessage, properties.getCircuitOpenFailureThreshold());
                boolean unknown = isUnknownResult(ex);
                String unknownId = unknown && attempt >= maxAttempts
                        ? registerUnknown(effectiveRequest, scope, target, failureCode, failureMessage)
                        : null;
                String resultState = unknown && attempt >= maxAttempts ? "UNKNOWN" : "FAILED";
                failScope(scope, target, attempt, !excludedInstanceIds.isEmpty(), "CLOSED", httpStatus,
                        resultState, unknownId, failureCode, failureMessage);
                lastFailure = unknown && attempt >= maxAttempts
                        ? ServiceCallResult.unknown(target, elapsed, attempt, failureCode, failureMessage)
                        : ServiceCallResult.failure(target, httpStatus, elapsed, attempt, failureCode, failureMessage);
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

    private Integer httpStatus(RuntimeException ex) {
        if (ex instanceof WebClientResponseException responseException) {
            return responseException.getStatusCode().value();
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

    private long elapsedMillis(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }

    private TransactionSegmentScope startAttempt(
            ServiceCallRequest request,
            ServiceCallResolvedTarget target,
            int attempt,
            boolean failover) {
        if (segmentService == null) {
            return null;
        }
        String sourceModule = textAttribute(request, "sourceModuleCode", "PFW");
        TransactionSegmentScope scope = segmentService.start(
                TransactionSegmentRole.EXTERNAL,
                TransactionSegmentDirection.OUTBOUND,
                sourceModule,
                sourceModule,
                request.serviceId().toUpperCase(),
                request.requestPath(),
                "Service Call " + request.serviceId() + " attempt " + attempt);
        scope.record().setSelectedInstanceId(target.instanceId());
        scope.record().setAttemptNo(attempt);
        scope.record().setRetryYn(attempt > 1 ? "Y" : "N");
        scope.record().setFailoverYn(failover ? "Y" : "N");
        scope.record().setCircuitState("CLOSED");
        scope.record().setResultState("RUNNING");
        return scope;
    }

    private void successScope(
            TransactionSegmentScope scope,
            ServiceCallResolvedTarget target,
            int attempt,
            boolean failover,
            Integer httpStatus) {
        if (scope == null) {
            return;
        }
        scope.record().setSelectedInstanceId(target.instanceId());
        scope.record().setAttemptNo(attempt);
        scope.record().setRetryYn(attempt > 1 ? "Y" : "N");
        scope.record().setFailoverYn(failover ? "Y" : "N");
        scope.record().setCircuitState("CLOSED");
        scope.record().setDownstreamHttpStatus(httpStatus);
        scope.record().setResultState("SUCCESS");
        scope.success();
    }

    private void failScope(
            TransactionSegmentScope scope,
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
        scope.record().setSelectedInstanceId(target != null ? target.instanceId() : null);
        scope.record().setAttemptNo(attempt);
        scope.record().setRetryYn(attempt > 1 ? "Y" : "N");
        scope.record().setFailoverYn(failover ? "Y" : "N");
        scope.record().setCircuitState(circuitState);
        scope.record().setDownstreamHttpStatus(httpStatus);
        scope.record().setResultState(resultState);
        scope.record().setUnknownResultId(unknownId);
        scope.fail(failureCode, failureMessage);
    }

    private String registerUnknown(
            ServiceCallRequest request,
            TransactionSegmentScope scope,
            ServiceCallResolvedTarget target,
            String failureCode,
            String failureMessage) {
        String unknownId = UUID.randomUUID().toString();
        if (reconciliationPort == null) {
            return unknownId;
        }
        try {
            reconciliationPort.register(new CpfUnknownResultRecord(
                    unknownId,
                    "SERVICE_CALL",
                    "CHECK_PENDING",
                    scope != null ? scope.transactionGlobalId() : textAttribute(request, "transactionGlobalId", null),
                    scope != null ? scope.transactionSegmentId() : null,
                    textAttribute(request, "externalKey", target != null ? target.instanceId() : null),
                    failureCode,
                    failureMessage,
                    "POLL_OR_MANUAL_RECONCILIATION",
                    Instant.now(),
                    null));
        } catch (RuntimeException ex) {
            log.error("서비스 호출 unknown 결과 등록에 실패했습니다. unknownId={}", unknownId, ex);
        }
        return unknownId;
    }

    private boolean isUnknownResult(RuntimeException failure) {
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
