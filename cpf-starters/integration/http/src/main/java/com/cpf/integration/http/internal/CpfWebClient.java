package com.cpf.integration.http.internal;

import com.cpf.integration.api.http.CpfRestClient;
import com.cpf.core.api.result.CpfRecoveryInfo;
import com.cpf.core.api.result.CpfResult;

import com.cpf.foundation.context.header.CpfHeaderNames;
import com.cpf.integration.http.internal.servicecall.CpfServiceCallEngine;
import com.cpf.integration.http.internal.servicecall.CpfServiceCallException;
import com.cpf.integration.http.internal.servicecall.ServiceCallRequest;
import com.cpf.integration.http.internal.servicecall.ServiceCallResolvedTarget;
import com.cpf.integration.http.internal.servicecall.ServiceCallResult;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * CPF 서비스 간 호출에 사용하는 WebClient 파사드입니다.
 *
 * <p>{@link #get(String, Function, Class)}와 {@link #post(String, String, Object, Class)} 계열 메서드는
 * CPF Service Call Engine을 우선 경유합니다. 레지스트리 DB가 아직 준비되지 않은 개발 환경에서는
 * 기존 {@code cpf.services.*.base-url} 설정으로 fallback하여 로컬 기동성을 유지합니다.</p>
 */
public class CpfWebClient implements CpfRestClient {

    private final WebClient.Builder webClientBuilder;
    private final CpfServiceEndpointRegistry endpointRegistry;
    private final ObjectProvider<CpfServiceCallEngine> serviceCallEngineProvider;
    private final CpfApiClientRuntimePolicy runtimePolicy;
    private final CpfPinnedHttpConnectorFactory pinnedConnectorFactory;

    public CpfWebClient(WebClient.Builder webClientBuilder, CpfServiceEndpointRegistry endpointRegistry) {
        this(webClientBuilder, endpointRegistry, null, new CpfApiClientRuntimePolicy(),
                CpfPinnedHttpConnectorFactory.secureDefault());
    }

    public CpfWebClient(WebClient.Builder webClientBuilder, CpfServiceEndpointRegistry endpointRegistry,
                        ObjectProvider<CpfServiceCallEngine> serviceCallEngineProvider) {
        this(webClientBuilder, endpointRegistry, serviceCallEngineProvider, new CpfApiClientRuntimePolicy(),
                CpfPinnedHttpConnectorFactory.secureDefault());
    }

    public CpfWebClient(WebClient.Builder webClientBuilder, CpfServiceEndpointRegistry endpointRegistry,
                        ObjectProvider<CpfServiceCallEngine> serviceCallEngineProvider,
                        CpfApiClientRuntimePolicy runtimePolicy) {
        this(webClientBuilder, endpointRegistry, serviceCallEngineProvider, runtimePolicy,
                CpfPinnedHttpConnectorFactory.secureDefault());
    }

    public CpfWebClient(WebClient.Builder webClientBuilder, CpfServiceEndpointRegistry endpointRegistry,
                        ObjectProvider<CpfServiceCallEngine> serviceCallEngineProvider,
                        CpfApiClientRuntimePolicy runtimePolicy,
                        CpfPinnedHttpConnectorFactory pinnedConnectorFactory) {
        this.webClientBuilder = java.util.Objects.requireNonNull(webClientBuilder, "webClientBuilder");
        this.endpointRegistry = java.util.Objects.requireNonNull(endpointRegistry, "endpointRegistry");
        this.serviceCallEngineProvider = serviceCallEngineProvider;
        this.runtimePolicy = runtimePolicy == null ? new CpfApiClientRuntimePolicy() : runtimePolicy;
        this.pinnedConnectorFactory = java.util.Objects.requireNonNull(pinnedConnectorFactory, "pinnedConnectorFactory");
    }

    /**
     * 기존 호환용 raw WebClient를 생성합니다.
     *
     * <p>신규 업무 코드는 call history, circuit, retry 관제를 위해 {@link #get} 또는 {@link #post}를 사용해야 합니다.</p>
     */
    public WebClient service(String serviceId) {
        CpfServiceEndpointRegistry.ResolvedEndpoint endpoint = endpointRegistry.resolvedEndpoint(serviceId);
        return webClient(endpoint, serviceId);
    }

    /**
     * blocking GET 호출을 수행합니다.
     */
    public <T> T get(String serviceId, Function<UriBuilder, URI> uriFunction, Class<T> responseType) {
        URI relativeUri = relativeUri(uriFunction);
        ServiceCallRequest request = request(serviceId, "GET", relativeUri.toString());
        CpfServiceCallEngine engine = serviceCallEngine();
        if (engine != null && engine.isEnabled()) {
            ServiceCallResult<T> result = invokeThroughEngineOrFallback(
                    engine,
                    request,
                    target -> webClient(target)
                            .get()
                            .uri(relativeUri.toString())
                            .retrieve()
                            .bodyToMono(responseType)
                            .block(timeout(request, target)));
            if (result != null) {
                return requireSuccess(result);
            }
        }
        return service(serviceId)
                .get()
                .uri(uriFunction)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    /**
     * 표준 실행 ID, endpoint code, timeout과 retry를 명시한 내부 GET 호출을 수행합니다.
     */
    public <T> T get(ServiceCallRequest request, Class<T> responseType) {
        ServiceCallRequest effective = requireGetRequest(request);
        CpfServiceCallEngine engine = serviceCallEngine();
        if (engine != null && engine.isEnabled()) {
            ServiceCallResult<T> result = invokeThroughEngineOrFallback(
                    engine,
                    effective,
                    target -> webClient(target)
                            .get()
                            .uri(effective.requestPath())
                            .headers(headers -> effective.headers().forEach(headers::set))
                            .retrieve()
                            .bodyToMono(responseType)
                            .block(timeout(effective, target)));
            if (result != null) {
                return requireSuccess(result);
            }
        }
        return service(effective.serviceId())
                .get()
                .uri(effective.requestPath())
                .headers(headers -> effective.headers().forEach(headers::set))
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    /**
     * generic 응답 타입을 사용하는 blocking GET 호출을 수행합니다.
     */
    public <T> T get(
            String serviceId,
            Function<UriBuilder, URI> uriFunction,
            ParameterizedTypeReference<T> responseType) {
        URI relativeUri = relativeUri(uriFunction);
        ServiceCallRequest request = request(serviceId, "GET", relativeUri.toString());
        CpfServiceCallEngine engine = serviceCallEngine();
        if (engine != null && engine.isEnabled()) {
            ServiceCallResult<T> result = invokeThroughEngineOrFallback(
                    engine,
                    request,
                    target -> webClient(target)
                            .get()
                            .uri(relativeUri.toString())
                            .retrieve()
                            .bodyToMono(responseType)
                            .block(timeout(request, target)));
            if (result != null) {
                return requireSuccess(result);
            }
        }
        return service(serviceId)
                .get()
                .uri(uriFunction)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    /**
     * blocking POST 호출을 수행합니다.
     */
    public <T> T post(String serviceId, String path, Object requestBody, Class<T> responseType) {
        return post(request(serviceId, "POST", normalizePath(path)), requestBody, responseType);
    }

    /**
     * timeout, retry, 외부키 같은 호출 속성을 명시한 표준 요청으로 POST를 수행합니다.
     */
    public <T> T post(ServiceCallRequest request, Object requestBody, Class<T> responseType) {
        ServiceCallRequest effective = requirePostRequest(request);
        CpfServiceCallEngine engine = serviceCallEngine();
        if (engine != null && engine.isEnabled()) {
            ServiceCallResult<T> result = invokeThroughEngineOrFallback(
                    engine,
                    effective,
                    target -> webClient(target)
                            .post()
                            .uri(effective.requestPath())
                            .headers(headers -> effective.headers().forEach(headers::set))
                            .bodyValue(requestBody)
                            .retrieve()
                            .bodyToMono(responseType)
                            .block(timeout(effective, target)));
            if (result != null) {
                return requireSuccess(result);
            }
        }
        return service(effective.serviceId())
                .post()
                .uri(effective.requestPath())
                .headers(headers -> effective.headers().forEach(headers::set))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    /** Domain Remote Adapter가 ServiceCall 4상태/Recovery와 명시적 내부 protocol header를 보존한 채 POST 결과를 받습니다. */
    public <T> ServiceCallResult<T> postResult(String serviceId, String path, Object requestBody, Class<T> responseType) {
        return postResult(request(serviceId, "POST", normalizePath(path)), requestBody, responseType);
    }

    public <T> ServiceCallResult<T> postResult(ServiceCallRequest request, Object requestBody, Class<T> responseType) {
        ServiceCallRequest effective = requirePostRequest(request);
        CpfServiceCallEngine engine = serviceCallEngine();
        if (engine == null || !engine.isEnabled()) {
            try {
                T body = service(effective.serviceId()).post().uri(effective.requestPath())
                        .headers(headers -> effective.headers().forEach(headers::set))
                        .bodyValue(requestBody).retrieve().bodyToMono(responseType).block();
                return ServiceCallResult.success(null, body, 200, 0L, 1);
            } catch (RuntimeException ex) {
                return ServiceCallResult.failure(null, null, 0L, 1, "CPF-DOMAIN-TRANSPORT", ex.getMessage());
            }
        }
        return engine.invoke(effective, target -> webClient(target).post().uri(effective.requestPath())
                .headers(headers -> effective.headers().forEach(headers::set)).bodyValue(requestBody).retrieve()
                .bodyToMono(responseType).block(timeout(effective, target)));
    }

    /**
     * generic 응답 타입을 사용하는 blocking POST 호출을 수행합니다.
     */
    public <T> T post(
            String serviceId,
            String path,
            Object requestBody,
            ParameterizedTypeReference<T> responseType) {
        return post(request(serviceId, "POST", normalizePath(path)), requestBody, responseType);
    }

    /**
     * generic 응답과 호출 속성을 함께 사용하는 표준 POST를 수행합니다.
     */
    public <T> T post(
            ServiceCallRequest request,
            Object requestBody,
            ParameterizedTypeReference<T> responseType) {
        ServiceCallRequest effective = requirePostRequest(request);
        CpfServiceCallEngine engine = serviceCallEngine();
        if (engine != null && engine.isEnabled()) {
            ServiceCallResult<T> result = invokeThroughEngineOrFallback(
                    engine,
                    effective,
                    target -> webClient(target)
                            .post()
                            .uri(effective.requestPath())
                            .headers(headers -> effective.headers().forEach(headers::set))
                            .bodyValue(requestBody)
                            .retrieve()
                            .bodyToMono(responseType)
                            .block(timeout(effective, target)));
            if (result != null) {
                return requireSuccess(result);
            }
        }
        return service(effective.serviceId())
                .post()
                .uri(effective.requestPath())
                .headers(headers -> effective.headers().forEach(headers::set))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    @Override
    public <T> T put(String serviceId, String path, Object requestBody, Class<T> responseType) {
        return exchange(serviceId, "PUT", b -> b.path(normalizePath(path)).build(), requestBody, Map.of(), responseType);
    }

    @Override
    public <T> T patch(String serviceId, String path, Object requestBody, Class<T> responseType) {
        return exchange(serviceId, "PATCH", b -> b.path(normalizePath(path)).build(), requestBody, Map.of(), responseType);
    }

    @Override
    public <T> T delete(String serviceId, String path, Class<T> responseType) {
        return exchange(serviceId, "DELETE", b -> b.path(normalizePath(path)).build(), null, Map.of(), responseType);
    }

    @Override
    public <T> T exchange(
            String serviceId,
            String method,
            Function<UriBuilder, URI> uriFunction,
            Object requestBody,
            Map<String, String> customHeaders,
            Class<T> responseType) {
        URI relativeUri = relativeUri(java.util.Objects.requireNonNull(uriFunction, "uriFunction"));
        String httpMethod = normalizeMethod(method);
        Map<String, String> safeHeaders = allowedExternalHeaders(customHeaders);
        ServiceCallRequest.Builder builder = ServiceCallRequest.builder(serviceId)
                .httpMethod(httpMethod)
                .requestPath(relativeUri.toString());
        safeHeaders.forEach(builder::header);
        ServiceCallRequest effective = normalizeRequest(builder.build(), httpMethod);
        CpfServiceCallEngine engine = serviceCallEngine();
        if (engine != null && engine.isEnabled()) {
            ServiceCallResult<T> result = invokeThroughEngineOrFallback(engine, effective,
                    target -> executeWebClient(webClient(target), effective, requestBody, responseType, timeout(effective, target)));
            if (result != null) return requireSuccess(result);
        }
        return executeWebClient(service(effective.serviceId()), effective, requestBody, responseType, null);
    }

    @Override
    public <T> CpfResult<T> exchangeResult(
            String serviceId,
            String method,
            Function<UriBuilder, URI> uriFunction,
            Object requestBody,
            Map<String, String> customHeaders,
            Class<T> responseType) {
        URI relativeUri = relativeUri(java.util.Objects.requireNonNull(uriFunction, "uriFunction"));
        String httpMethod = normalizeMethod(method);
        Map<String, String> safeHeaders = allowedExternalHeaders(customHeaders);
        ServiceCallRequest.Builder builder = ServiceCallRequest.builder(serviceId)
                .httpMethod(httpMethod)
                .requestPath(relativeUri.toString());
        safeHeaders.forEach(builder::header);
        ServiceCallRequest effective = normalizeRequest(builder.build(), httpMethod);
        CpfServiceCallEngine engine = serviceCallEngine();
        if (engine != null && engine.isEnabled()) {
            ServiceCallResult<T> result = invokeThroughEngineOrFallback(engine, effective,
                    target -> executeWebClient(webClient(target), effective, requestBody, responseType, timeout(effective, target)));
            if (result != null) return toCpfResult(result);
        }
        try {
            return CpfResult.success(executeWebClient(service(effective.serviceId()), effective, requestBody, responseType, null));
        } catch (RuntimeException ex) {
            return CpfResult.technicalFailure("CPF-HTTP-TRANSPORT", safeMessage(ex));
        }
    }

    private <T> CpfResult<T> toCpfResult(ServiceCallResult<T> result) {
        if (result.successValue()) return CpfResult.success(result.responseBody());
        if (result.businessFailureValue()) return CpfResult.businessFailure(result.failureCode(), result.failureMessage());
        if (result.unknownValue()) {
            String recoveryId = result.recoveryId() == null || result.recoveryId().isBlank()
                    ? "service-call:" + java.util.UUID.randomUUID() : result.recoveryId();
            String action = result.recoveryAction() == null || result.recoveryAction().isBlank()
                    ? "PROBE_OR_RECONCILE" : result.recoveryAction();
            return CpfResult.unknown(result.failureCode(), result.failureMessage(), new CpfRecoveryInfo(recoveryId, action));
        }
        return CpfResult.technicalFailure(result.failureCode(), result.failureMessage());
    }

    private String safeMessage(Throwable failure) {
        String message = failure == null ? null : failure.getMessage();
        return message == null || message.isBlank() ? "External HTTP transport failed" : message;
    }

    private <T> T executeWebClient(
            WebClient client, ServiceCallRequest request, Object body, Class<T> responseType, Duration timeout) {
        WebClient.RequestBodySpec spec = client.method(HttpMethod.valueOf(request.httpMethod()))
                .uri(request.requestPath())
                .headers(headers -> request.headers().forEach(headers::set));
        WebClient.RequestHeadersSpec<?> ready = body == null ? spec : spec.bodyValue(body);
        var mono = ready.retrieve().bodyToMono(responseType);
        return timeout == null ? mono.block() : mono.block(timeout);
    }

    private ServiceCallRequest normalizeRequest(ServiceCallRequest request, String method) {
        if (request == null || request.serviceId() == null || request.serviceId().isBlank()) {
            throw new IllegalArgumentException("서비스 호출 serviceId는 필수입니다.");
        }
        return runtimePolicy.apply(withEndpointTimeout(new ServiceCallRequest(
                request.serviceId().trim(), request.endpointCode(), request.instanceId(), method,
                normalizePath(request.requestPath()), request.timeoutMillis(), request.retryCount(),
                request.headers(), request.attributes())));
    }

    private String normalizeMethod(String method) {
        if (method == null || method.isBlank()) throw new IllegalArgumentException("HTTP method는 필수입니다.");
        String normalized = method.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("GET", "POST", "PUT", "PATCH", "DELETE").contains(normalized)) {
            throw new IllegalArgumentException("지원하지 않는 HTTP method입니다: " + method);
        }
        return normalized;
    }

    private Map<String, String> allowedExternalHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) return Map.of();
        Map<String, String> copy = new java.util.LinkedHashMap<>();
        for (var entry : headers.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (name == null || name.isBlank() || value == null) continue;
            if (protectedExternalHeader(name)) {
                throw new IllegalArgumentException("CPF 보호/인증 Header는 CpfRestClient customHeaders로 직접 설정할 수 없습니다: " + name);
            }
            copy.put(name, value);
        }
        return Map.copyOf(copy);
    }

    private boolean protectedExternalHeader(String name) {
        String n = name.trim().toLowerCase(Locale.ROOT);
        return Set.of(
                CpfHeaderNames.TRANSACTION_ID,
                CpfHeaderNames.ORIGINAL_CHANNEL,
                CpfHeaderNames.CURRENT_CHANNEL,
                CpfHeaderNames.CALLER_CHANNEL,
                CpfHeaderNames.TARGET_CHANNEL,
                CpfHeaderNames.TARGET_OPERATION_ID,
                CpfHeaderNames.AUTHORIZATION,
                CpfHeaderNames.API_KEY,
                CpfHeaderNames.REQUEST_SIGNATURE)
                .stream().map(v -> v.toLowerCase(Locale.ROOT)).anyMatch(n::equals);
    }

    private <T> ServiceCallResult<T> invokeThroughEngineOrFallback(
            CpfServiceCallEngine engine,
            ServiceCallRequest request,
            Function<ServiceCallResolvedTarget, T> remoteCall) {
        try {
            return engine.invoke(request, remoteCall);
        } catch (RuntimeException ex) {
            if (engine.fallbackToConfiguredEndpoint() && registryUnavailable(ex)) {
                return null;
            }
            throw ex;
        }
    }

    private <T> T requireSuccess(ServiceCallResult<T> result) {
        if (result.successValue()) {
            return result.responseBody();
        }
        throw new CpfServiceCallException(result);
    }

    private CpfServiceCallEngine serviceCallEngine() {
        return serviceCallEngineProvider == null ? null : serviceCallEngineProvider.getIfAvailable();
    }

    private WebClient webClient(ServiceCallResolvedTarget target) {
        CpfServiceEndpointRegistry.ResolvedEndpoint endpoint = endpointRegistry.resolvedEndpoint(
                target.serviceId(), target.baseUrl(), target.endpoint());
        return webClient(endpoint, target.serviceId());
    }

    private WebClient webClient(CpfServiceEndpointRegistry.ResolvedEndpoint endpoint, String serviceId) {
        return webClientBuilder.clone()
                .clientConnector(pinnedConnectorFactory.connector(endpoint))
                .baseUrl(trimTrailingSlash(endpoint.baseUrl()))
                .defaultHeader("Host", endpoint.authority())
                .build();
    }

    private ServiceCallRequest request(String serviceId, String method, String path) {
        return runtimePolicy.apply(withEndpointTimeout(ServiceCallRequest.builder(serviceId)
                .httpMethod(method)
                .requestPath(path)
                .build()));
    }

    private ServiceCallRequest requirePostRequest(ServiceCallRequest request) {
        if (request == null || request.serviceId() == null || request.serviceId().isBlank()) {
            throw new IllegalArgumentException("서비스 호출 serviceId는 필수입니다.");
        }
        return runtimePolicy.apply(withEndpointTimeout(new ServiceCallRequest(
                request.serviceId().trim(),
                request.endpointCode(),
                request.instanceId(),
                "POST",
                normalizePath(request.requestPath()),
                request.timeoutMillis(),
                request.retryCount(),
                request.headers(),
                request.attributes())));
    }

    private ServiceCallRequest requireGetRequest(ServiceCallRequest request) {
        if (request == null || request.serviceId() == null || request.serviceId().isBlank()) {
            throw new IllegalArgumentException("서비스 호출 serviceId는 필수입니다.");
        }
        return runtimePolicy.apply(withEndpointTimeout(new ServiceCallRequest(
                request.serviceId().trim(), request.endpointCode(), request.instanceId(), "GET",
                normalizePath(request.requestPath()), request.timeoutMillis(), request.retryCount(),
                request.headers(), request.attributes())));
    }


    private ServiceCallRequest withEndpointTimeout(ServiceCallRequest request) {
        if (request.timeoutMillis() != null && request.timeoutMillis() > 0) return request;
        CpfServiceEndpointRegistry.RuntimeEndpoint endpoint = endpointRegistry.runtimeEndpoint(request.serviceId());
        if (endpoint == null) return request;
        return new ServiceCallRequest(request.serviceId(), request.endpointCode(), request.instanceId(),
                request.httpMethod(), request.requestPath(), endpoint.timeoutMillis(), request.retryCount(),
                request.headers(), request.attributes());
    }

    private URI relativeUri(Function<UriBuilder, URI> uriFunction) {
        URI uri = uriFunction.apply(UriComponentsBuilder.newInstance());
        return URI.create(normalizePath(uri.toString()));
    }

    private Duration timeout(ServiceCallRequest request, ServiceCallResolvedTarget target) {
        int timeoutMillis = request.timeoutMillis() != null && request.timeoutMillis() > 0
                ? request.timeoutMillis()
                : intValue(target.endpoint().get("defaultTimeoutMs"), 3000);
        return Duration.ofMillis(Math.max(1, timeoutMillis));
    }

    private int intValue(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private boolean registryUnavailable(RuntimeException ex) {
        String message = ex.getMessage();
        if (message == null) {
            return false;
        }
        String normalized = message.toLowerCase(Locale.ROOT);
        return normalized.contains("레지스트리")
                || normalized.contains("endpoint")
                || normalized.contains("service endpoint")
                || normalized.contains("cpf 서비스");
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("서비스 호출 대상 baseUrl이 비어 있습니다.");
        }
        String trimmed = baseUrl.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
