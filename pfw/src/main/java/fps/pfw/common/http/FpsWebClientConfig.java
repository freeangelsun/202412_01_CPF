package fps.pfw.common.http;

import fps.pfw.common.logging.TransactionContext;
import fps.pfw.common.workflow.FpsWorkflowContext;
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
 * 주제영역 간 호출에 사용하는 WebClient 공통 설정입니다.
 * 공통 거래 헤더를 자동으로 전파하여 ACC -> MBR -> 기타 서비스 흐름을 같은 거래ID로 추적합니다.
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
     * 현재 요청의 거래 헤더를 하위 서비스 호출에 자동으로 복사합니다.
     * 이미 호출 코드에서 명시한 헤더가 있으면 그 값을 우선합니다.
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
