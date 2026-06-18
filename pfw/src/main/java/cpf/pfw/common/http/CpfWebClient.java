package cpf.pfw.common.http;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.function.Function;

/**
 * Small WebClient facade for CPF service-to-service calls.
 *
 * <p>Transaction and workflow headers are added by {@link CpfWebClientConfig}.</p>
 */
public class CpfWebClient {

    private final WebClient.Builder webClientBuilder;
    private final CpfServiceEndpointRegistry endpointRegistry;

    public CpfWebClient(WebClient.Builder webClientBuilder, CpfServiceEndpointRegistry endpointRegistry) {
        this.webClientBuilder = webClientBuilder;
        this.endpointRegistry = endpointRegistry;
    }

    /**
     * Builds a WebClient with the base URL configured for the given service id.
     */
    public WebClient service(String serviceId) {
        return webClientBuilder.clone()
                .baseUrl(endpointRegistry.baseUrl(serviceId))
                .build();
    }

    /**
     * Executes a blocking GET call for servlet-based service code.
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
     * Executes a blocking GET call with a generic response type.
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
     * Executes a blocking POST call for servlet-based service code.
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
