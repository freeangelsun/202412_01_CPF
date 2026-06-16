package cpf.pfw.common.http;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.function.Function;

/**
 * FPS ?꾨젅?꾩썙???쒖? WebClient ?섑띁?낅땲??
 * 嫄곕옒ID, TraceId, 梨꾨꼸, ?뚯썝踰덊샇 媛숈? 怨듯넻 ?ㅻ뜑???ㅼ젙 ?꾪꽣?먯꽌 ?먮룞 ?꾪뙆?⑸땲??
 */
public class FpsWebClient {

    private final WebClient.Builder webClientBuilder;
    private final FpsServiceEndpointRegistry endpointRegistry;

    public FpsWebClient(WebClient.Builder webClientBuilder, FpsServiceEndpointRegistry endpointRegistry) {
        this.webClientBuilder = webClientBuilder;
        this.endpointRegistry = endpointRegistry;
    }

    /**
     * ?쒕퉬??ID???대떦?섎뒗 base-url???곸슜??WebClient瑜?諛섑솚?⑸땲??
     * 蹂듭옟???몄텧? ??硫붿꽌?쒕줈 WebClient 泥댁씤??吏곸젒 ?ъ슜?섎㈃ ?⑸땲??
     */
    public WebClient service(String serviceId) {
        return webClientBuilder.clone()
                .baseUrl(endpointRegistry.baseUrl(serviceId))
                .build();
    }

    /**
     * ?⑥닚 GET ?몄텧???몄쓽 硫붿꽌?쒖엯?덈떎.
     * Servlet 湲곕컲 ?쒕퉬?ㅼ뿉???숆린???섑뵆???쎄쾶 ?묒꽦?????덈룄濡?block()源뚯? ?섑뻾?⑸땲??
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
     * ?쒕꽕由??묐떟 ??낆쓣 諛쏅뒗 GET ?몄텧???몄쓽 硫붿꽌?쒖엯?덈떎.
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
     * ?⑥닚 POST ?몄텧???몄쓽 硫붿꽌?쒖엯?덈떎.
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

