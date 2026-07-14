package cpf.pfw.common.http;

import cpf.pfw.common.servicecall.CpfServiceCallEngine;
import cpf.pfw.common.servicecall.CpfServiceCallException;
import cpf.pfw.common.servicecall.ServiceCallRequest;
import cpf.pfw.common.servicecall.ServiceCallResolvedTarget;
import cpf.pfw.common.servicecall.ServiceCallResult;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.function.Function;

/**
 * CPF 서비스 간 호출에 사용하는 WebClient 파사드입니다.
 *
 * <p>{@link #get(String, Function, Class)}와 {@link #post(String, String, Object, Class)} 계열 메서드는
 * PFW Service Call Engine을 우선 경유합니다. 레지스트리 DB가 아직 준비되지 않은 개발 환경에서는
 * 기존 {@code cpf.services.*.base-url} 설정으로 fallback하여 로컬 기동성을 유지합니다.</p>
 */
public class CpfWebClient {

    private final WebClient.Builder webClientBuilder;
    private final CpfServiceEndpointRegistry endpointRegistry;
    private final ObjectProvider<CpfServiceCallEngine> serviceCallEngineProvider;

    public CpfWebClient(WebClient.Builder webClientBuilder, CpfServiceEndpointRegistry endpointRegistry) {
        this(webClientBuilder, endpointRegistry, null);
    }

    public CpfWebClient(
            WebClient.Builder webClientBuilder,
            CpfServiceEndpointRegistry endpointRegistry,
            ObjectProvider<CpfServiceCallEngine> serviceCallEngineProvider) {
        this.webClientBuilder = webClientBuilder;
        this.endpointRegistry = endpointRegistry;
        this.serviceCallEngineProvider = serviceCallEngineProvider;
    }

    /**
     * 기존 호환용 raw WebClient를 생성합니다.
     *
     * <p>신규 업무 코드는 call history, circuit, retry 관제를 위해 {@link #get} 또는 {@link #post}를 사용해야 합니다.</p>
     */
    public WebClient service(String serviceId) {
        return webClientBuilder.clone()
                .baseUrl(endpointRegistry.baseUrl(serviceId))
                .build();
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
        if ("SUCCESS".equals(result.status())) {
            return result.responseBody();
        }
        throw new CpfServiceCallException(result);
    }

    private CpfServiceCallEngine serviceCallEngine() {
        return serviceCallEngineProvider == null ? null : serviceCallEngineProvider.getIfAvailable();
    }

    private WebClient webClient(ServiceCallResolvedTarget target) {
        return webClientBuilder.clone()
                .baseUrl(trimTrailingSlash(target.baseUrl()))
                .build();
    }

    private ServiceCallRequest request(String serviceId, String method, String path) {
        return ServiceCallRequest.builder(serviceId)
                .httpMethod(method)
                .requestPath(path)
                .build();
    }

    private ServiceCallRequest requirePostRequest(ServiceCallRequest request) {
        if (request == null || request.serviceId() == null || request.serviceId().isBlank()) {
            throw new IllegalArgumentException("서비스 호출 serviceId는 필수입니다.");
        }
        return new ServiceCallRequest(
                request.serviceId().trim(),
                request.endpointCode(),
                request.instanceId(),
                "POST",
                normalizePath(request.requestPath()),
                request.timeoutMillis(),
                request.retryCount(),
                request.headers(),
                request.attributes());
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
                || normalized.contains("pfw 서비스");
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
