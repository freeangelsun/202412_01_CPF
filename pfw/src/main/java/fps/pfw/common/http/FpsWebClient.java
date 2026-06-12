package fps.pfw.common.http;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.function.Function;

/**
 * FPS 프레임워크 표준 WebClient 래퍼입니다.
 * 거래ID, TraceId, 채널, 회원번호 같은 공통 헤더는 설정 필터에서 자동 전파됩니다.
 */
public class FpsWebClient {

    private final WebClient.Builder webClientBuilder;
    private final FpsServiceEndpointRegistry endpointRegistry;

    public FpsWebClient(WebClient.Builder webClientBuilder, FpsServiceEndpointRegistry endpointRegistry) {
        this.webClientBuilder = webClientBuilder;
        this.endpointRegistry = endpointRegistry;
    }

    /**
     * 서비스 ID에 해당하는 base-url이 적용된 WebClient를 반환합니다.
     * 복잡한 호출은 이 메서드로 WebClient 체인을 직접 사용하면 됩니다.
     */
    public WebClient service(String serviceId) {
        return webClientBuilder.clone()
                .baseUrl(endpointRegistry.baseUrl(serviceId))
                .build();
    }

    /**
     * 단순 GET 호출용 편의 메서드입니다.
     * Servlet 기반 서비스에서 동기식 샘플을 쉽게 작성할 수 있도록 block()까지 수행합니다.
     */
    public <T> T get(String serviceId, Function<UriBuilder, URI> uriFunction, Class<T> responseType) {
        return service(serviceId)
                .get()
                .uri(uriFunction)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    /**
     * 제네릭 응답 타입을 받는 GET 호출용 편의 메서드입니다.
     */
    public <T> T get(
            String serviceId,
            Function<UriBuilder, URI> uriFunction,
            ParameterizedTypeReference<T> responseType) {

        return service(serviceId)
                .get()
                .uri(uriFunction)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    /**
     * 단순 POST 호출용 편의 메서드입니다.
     */
    public <T> T post(String serviceId, String path, Object requestBody, Class<T> responseType) {
        return service(serviceId)
                .post()
                .uri(path)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }
}
