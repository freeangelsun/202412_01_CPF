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
 * WebClient configuration for calls between CPF services.
 *
 * <p>It propagates transaction and workflow headers across service boundaries.</p>
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
     * Propagates transaction and workflow context headers to downstream services.
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
