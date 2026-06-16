package cpf.pfw.common.http;

import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.workflow.FpsWorkflowContext;
import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;

/**
 * 二쇱젣?곸뿭 媛??몄텧???ъ슜?섎뒗 WebClient 怨듯넻 ?ㅼ젙?낅땲??
 * 怨듯넻 嫄곕옒 ?ㅻ뜑瑜??먮룞?쇰줈 ?꾪뙆?섏뿬 ACC -> MBR -> 湲고? ?쒕퉬???먮쫫??媛숈? 嫄곕옒ID濡?異붿쟻?⑸땲??
 */
@Configuration
@EnableConfigurationProperties({
        FpsHttpClientProperties.class,
        FpsServiceEndpointProperties.class
})
public class FpsWebClientConfig {

    @Bean
    public FpsServiceEndpointRegistry fpsServiceEndpointRegistry(FpsServiceEndpointProperties properties) {
        return new FpsServiceEndpointRegistry(properties);
    }

    @Bean
    public FpsWebClient fpsWebClient(
            FpsHttpClientProperties httpClientProperties,
            FpsServiceEndpointRegistry endpointRegistry) {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, httpClientProperties.getConnectTimeoutMillis())
                .responseTimeout(Duration.ofMillis(httpClientProperties.getReadTimeoutMillis()));

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(httpClientProperties.getMaxInMemorySizeKb() * 1024))
                .build();

        WebClient.Builder builder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(exchangeStrategies)
                .filter(transactionHeaderPropagationFilter());

        return new FpsWebClient(builder, endpointRegistry);
    }

    /**
     * ?꾩옱 ?붿껌??嫄곕옒 ?ㅻ뜑瑜??섏쐞 ?쒕퉬???몄텧???먮룞?쇰줈 蹂듭궗?⑸땲??
     * ?대? ?몄텧 肄붾뱶?먯꽌 紐낆떆???ㅻ뜑媛 ?덉쑝硫?洹?媛믪쓣 ?곗꽑?⑸땲??
     */
    private ExchangeFilterFunction transactionHeaderPropagationFilter() {
        return (request, next) -> {
            ClientRequest.Builder requestBuilder = ClientRequest.from(request);

            for (Map.Entry<String, String> header : TransactionContext.propagationHeaders().entrySet()) {
                if (hasText(header.getValue()) && !request.headers().containsKey(header.getKey())) {
                    requestBuilder.header(header.getKey(), header.getValue());
                }
            }
            for (Map.Entry<String, String> header : FpsWorkflowContext.propagationHeaders().entrySet()) {
                if (hasText(header.getValue()) && !request.headers().containsKey(header.getKey())) {
                    requestBuilder.header(header.getKey(), header.getValue());
                }
            }

            return next.exchange(requestBuilder.build());
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

