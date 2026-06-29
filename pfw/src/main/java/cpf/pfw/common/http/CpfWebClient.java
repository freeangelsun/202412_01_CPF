package cpf.pfw.common.http;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.function.Function;

/**
 * CPF 서비스 간 호출에 사용하는 작은 WebClient 파사드입니다.
 *
 * <p>거래 헤더와 워크플로 헤더는 {@link CpfWebClientConfig}가 자동으로 추가합니다.</p>
 */
public class CpfWebClient {

    private final WebClient.Builder webClientBuilder;
    private final CpfServiceEndpointRegistry endpointRegistry;

    public CpfWebClient(WebClient.Builder webClientBuilder, CpfServiceEndpointRegistry endpointRegistry) {
        this.webClientBuilder = webClientBuilder;
        this.endpointRegistry = endpointRegistry;
    }

    /**
     * 서비스 ID에 연결된 base URL을 적용한 WebClient를 생성합니다.
     */
    public WebClient service(String serviceId) {
        return webClientBuilder.clone()
                .baseUrl(endpointRegistry.baseUrl(serviceId))
                .build();
    }

    /**
     * 서블릿 기반 서비스 코드에서 사용할 수 있는 blocking GET 호출입니다.
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
     * 제네릭 응답 타입을 사용하는 blocking GET 호출입니다.
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
     * 서블릿 기반 서비스 코드에서 사용할 수 있는 blocking POST 호출입니다.
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
