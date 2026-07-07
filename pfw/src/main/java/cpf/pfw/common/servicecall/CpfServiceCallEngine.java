package cpf.pfw.common.servicecall;

import java.util.function.Supplier;

/**
 * CPF 표준 서비스 호출 엔진입니다.
 *
 * <p>레지스트리 조회, endpoint/instance 선택, 라우팅 정책 적용, 호출 이력 기록을 공통화합니다.
 * 실제 원격 호출은 WebClient/RestClient 어댑터 또는 업무 Facade가 Supplier로 주입하여 실행하므로
 * 업무 모듈은 호출 정책과 로그 구조를 반복 구현하지 않아도 됩니다.</p>
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

    public ServiceCallResolvedTarget resolve(ServiceCallRequest request) {
        return endpointResolver.resolve(applyDefaults(request));
    }

    public <T> ServiceCallResult<T> invoke(ServiceCallRequest request, Supplier<T> remoteCall) {
        ServiceCallRequest resolvedRequest = applyDefaults(request);
        ServiceCallResolvedTarget target = resolve(resolvedRequest);
        long started = System.nanoTime();
        try {
            T response = remoteCall.get();
            long elapsed = elapsedMillis(started);
            logWriter.write(resolvedRequest, target, "SUCCESS", 200, elapsed, null, null);
            return ServiceCallResult.success(target, response, 200, elapsed);
        } catch (RuntimeException ex) {
            long elapsed = elapsedMillis(started);
            logWriter.write(
                    resolvedRequest,
                    target,
                    "FAILED",
                    null,
                    elapsed,
                    ex.getClass().getSimpleName(),
                    ex.getMessage());
            return ServiceCallResult.failure(target, elapsed, ex.getClass().getSimpleName(), ex.getMessage());
        }
    }

    private ServiceCallRequest applyDefaults(ServiceCallRequest request) {
        return new ServiceCallRequest(
                request.serviceId(),
                request.endpointCode(),
                request.instanceId(),
                defaultIfBlank(request.httpMethod(), "GET"),
                defaultIfBlank(request.requestPath(), "/"),
                request.timeoutMillis() == null ? properties.getDefaultTimeoutMillis() : request.timeoutMillis(),
                request.retryCount() == null ? properties.getDefaultRetryCount() : request.retryCount(),
                request.headers(),
                request.attributes());
    }

    private long elapsedMillis(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
