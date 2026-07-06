package cpf.pfw.common.http;

import cpf.pfw.common.header.CpfHeaderPropagator;
import cpf.pfw.common.logging.file.CpfFileLogWriter;
import cpf.pfw.common.workflow.CpfWorkflowContext;
import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.ObjectProvider;
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
import java.util.Locale;
import java.util.Map;

/**
 * CPF 서비스 간 호출에 사용하는 WebClient 설정입니다.
 *
 * <p>현재 거래 컨텍스트와 워크플로 컨텍스트를 하위 서비스로 자동 전파합니다.</p>
 */
@Configuration
@EnableConfigurationProperties({
        CpfHttpClientProperties.class,
        CpfServiceEndpointProperties.class
})
public class CpfWebClientConfig {

    @Bean
    public CpfServiceEndpointRegistry cpfServiceEndpointRegistry(CpfServiceEndpointProperties properties) {
        return new CpfServiceEndpointRegistry(properties);
    }

    @Bean
    public CpfWebClient cpfWebClient(
            CpfHttpClientProperties httpClientProperties,
            CpfServiceEndpointRegistry endpointRegistry,
            ObjectProvider<CpfFileLogWriter> fileLogWriterProvider) {

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
                .filter(transactionHeaderPropagationFilter())
                .filter(integrationFileLogFilter(fileLogWriterProvider));

        return new CpfWebClient(builder, endpointRegistry);
    }

    /**
     * 하위 서비스 호출 전에 CPF 표준 거래 헤더와 워크플로 헤더를 추가합니다.
     */
    private ExchangeFilterFunction transactionHeaderPropagationFilter() {
        return (request, next) -> {
            ClientRequest.Builder requestBuilder = ClientRequest.from(request);

            for (Map.Entry<String, String> header : CpfHeaderPropagator.outboundHeaders().entrySet()) {
                if (hasText(header.getValue()) && !request.headers().containsKey(header.getKey())) {
                    requestBuilder.header(header.getKey(), header.getValue());
                }
            }
            for (Map.Entry<String, String> header : CpfWorkflowContext.propagationHeaders().entrySet()) {
                if (hasText(header.getValue()) && !request.headers().containsKey(header.getKey())) {
                    requestBuilder.header(header.getKey(), header.getValue());
                }
            }

            return next.exchange(requestBuilder.build());
        };
    }

    /**
     * WebClient 기반 하위 서비스 호출을 CPF integration 파일 로그로 기록합니다.
     */
    private ExchangeFilterFunction integrationFileLogFilter(ObjectProvider<CpfFileLogWriter> fileLogWriterProvider) {
        return (request, next) -> {
            long started = System.nanoTime();
            return next.exchange(request)
                    .doOnSuccess(response -> {
                        CpfFileLogWriter writer = fileLogWriterProvider.getIfAvailable();
                        if (writer == null || response == null) {
                            return;
                        }
                        writer.writeIntegration(
                                null,
                                inferTargetModule(request.url().getHost(), request.url().getPort(), request.url().getPath()),
                                "OUTBOUND",
                                request.method().name(),
                                request.url().getPath(),
                                response.statusCode().value(),
                                response.statusCode().isError() ? "FAILED" : "SUCCESS",
                                elapsedMillis(started),
                                response.statusCode().isError() ? "HTTP_" + response.statusCode().value() : null,
                                response.statusCode().isError() ? "하위 서비스 HTTP 오류" : null,
                                Map.of(
                                        "endpointCode", request.url().getHost() + ":" + request.url().getPort(),
                                        "timeoutMs", 0,
                                        "timeoutYn", "N",
                                        "retryCount", 0,
                                        "requestHeadersMasked", request.headers().toString()));
                    })
                    .doOnError(error -> {
                        CpfFileLogWriter writer = fileLogWriterProvider.getIfAvailable();
                        if (writer == null) {
                            return;
                        }
                        writer.writeIntegration(
                                null,
                                inferTargetModule(request.url().getHost(), request.url().getPort(), request.url().getPath()),
                                "OUTBOUND",
                                request.method().name(),
                                request.url().getPath(),
                                0,
                                "FAILED",
                                elapsedMillis(started),
                                error.getClass().getSimpleName(),
                                error.getMessage(),
                                Map.of(
                                        "endpointCode", request.url().getHost() + ":" + request.url().getPort(),
                                        "timeoutMs", 0,
                                        "timeoutYn", "N",
                                        "retryCount", 0));
                    });
        };
    }

    private long elapsedMillis(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }

    private String inferTargetModule(String host, int port, String path) {
        String normalizedPath = path == null ? "" : path.toLowerCase(Locale.ROOT);
        if (normalizedPath.contains("/mbr/")) {
            return "MBR";
        }
        if (normalizedPath.contains("/acc/")) {
            return "ACC";
        }
        if (normalizedPath.contains("/adm/")) {
            return "ADM";
        }
        if (normalizedPath.contains("/api/exs/") || port == 8092) {
            return "EXS";
        }
        if (port == 8081) {
            return "MBR";
        }
        if (port == 8080) {
            return "ACC";
        }
        return hasText(host) ? host.toUpperCase(Locale.ROOT) : "UNKNOWN";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
