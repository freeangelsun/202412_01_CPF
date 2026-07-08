package cpf.pfw.common.servicecall;

import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
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
    private final CpfEndpointResolver endpointResolver;
    private final CpfServiceCallLogWriter logWriter;
    private final CpfServiceCallProperties properties;

    public CpfServiceCallEngine(
            CpfEndpointResolver endpointResolver,
            CpfServiceCallLogWriter logWriter,
            CpfServiceCallProperties properties) {
        this.endpointResolver = endpointResolver;
        this.logWriter = logWriter;
        this.properties = properties;
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
            if (logWriter.isCircuitOpen(target, properties.getCircuitOpenRetryAfterMillis())) {
                logWriter.write(
                        effectiveRequest,
                        target,
                        "CIRCUIT_OPEN",
                        null,
                        0,
                        "CIRCUIT_OPEN",
                        "서비스 호출 circuit이 OPEN 상태입니다.");
                return ServiceCallResult.failure(target, null, 0L, attempt, "CIRCUIT_OPEN", "서비스 호출 circuit이 OPEN 상태입니다.");
            }

            long started = System.nanoTime();
            try {
                T response = remoteCall.apply(target);
                long elapsed = elapsedMillis(started);
                logWriter.write(effectiveRequest, target, "SUCCESS", 200, elapsed, null, null);
                logWriter.markSuccess(target, 200, elapsed);
                return ServiceCallResult.success(target, response, 200, elapsed, attempt);
            } catch (RuntimeException ex) {
                long elapsed = elapsedMillis(started);
                Integer httpStatus = httpStatus(ex);
                String failureCode = ex.getClass().getSimpleName();
                String failureMessage = safeMessage(ex);
                logWriter.write(effectiveRequest, target, "FAILED", httpStatus, elapsed, failureCode, failureMessage);
                logWriter.markFailure(target, httpStatus, elapsed, failureMessage, properties.getCircuitOpenFailureThreshold());
                lastFailure = ServiceCallResult.failure(target, httpStatus, elapsed, attempt, failureCode, failureMessage);
                excludeForFailover(target, excludedInstanceIds);
                if (!target.failoverEnabled() && attempt >= maxAttempts) {
                    return lastFailure;
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

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
