package com.cpf.integration.http.internal;

import com.cpf.platform.operations.observability.api.logging.CpfIntegrationLogPort;
import com.cpf.integration.http.internal.servicecall.CpfServiceCallEngine;
import com.cpf.web.context.CpfHeaderPolicyRegistry;
import com.cpf.web.context.CpfHttpHeaderLogSanitizer;
import io.netty.channel.ChannelOption;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.boot.restclient.RestTemplateCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * CPF HTTP Client 공통 설정입니다.
 *
 * <p>Generic RestClient/WebClient는 외부기관 호출에도 사용되므로 CPF 내부 거래 Header를 자동 복사하지 않습니다.
 * 내부 Domain-to-Domain 호출의 Canonical Header는 typed Domain transport가 신뢰 경계에서 명시적으로 구성합니다.</p>
 */
@AutoConfiguration
@EnableConfigurationProperties({
        CpfHttpClientProperties.class,
        CpfServiceEndpointProperties.class
})
public class CpfWebClientConfig {


    @Bean
    @ConditionalOnMissingBean
    public CpfApiClientRuntimePolicy cpfApiClientRuntimePolicy() { return new CpfApiClientRuntimePolicy(); }

    @Bean
    @ConditionalOnMissingBean
    public CpfPinnedHttpConnectorFactory cpfPinnedHttpConnectorFactory(CpfHttpClientProperties properties) {
        return new CpfPinnedHttpConnectorFactory(properties);
    }

    @Bean
    public CpfServiceEndpointRegistry cpfServiceEndpointRegistry(CpfServiceEndpointProperties properties) {
        return new CpfServiceEndpointRegistry(properties);
    }

    @Bean
    public CpfWebClient cpfWebClient(
            CpfHttpClientProperties httpClientProperties,
            CpfServiceEndpointRegistry endpointRegistry,
            CpfApiClientRuntimePolicy runtimePolicy,
            CpfPinnedHttpConnectorFactory pinnedConnectorFactory,
            ObjectProvider<CpfIntegrationLogPort> fileLogWriterProvider,
            ObjectProvider<CpfServiceCallEngine> serviceCallEngineProvider,
            ObjectProvider<CpfHeaderPolicyRegistry> headerPolicyProvider,
            Environment environment) {

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
                .filter(integrationFileLogFilter(fileLogWriterProvider,
                        new CpfHttpHeaderLogSanitizer(headerPolicyProvider.getIfAvailable())));

        return new CpfWebClient(
                builder, endpointRegistry, serviceCallEngineProvider, runtimePolicy, pinnedConnectorFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfRestClientInterceptor cpfRestClientInterceptor(
            ObjectProvider<CpfIntegrationLogPort> fileLogWriterProvider,
            ObjectProvider<CpfHeaderPolicyRegistry> headerPolicyProvider,
            Environment environment) {
        return new CpfRestClientInterceptor(
                fileLogWriterProvider.getIfAvailable(),
                CpfLocalServiceIdentity.from(environment),
                new CpfHttpHeaderLogSanitizer(headerPolicyProvider.getIfAvailable()));
    }

    @Bean
    @ConditionalOnClass(RestClient.class)
    public RestClientCustomizer cpfRestClientCustomizer(CpfRestClientInterceptor interceptor) {
        return builder -> builder.requestInterceptor(interceptor);
    }

    @Bean
    @ConditionalOnClass(RestTemplate.class)
    public RestTemplateCustomizer cpfRestTemplateCustomizer(CpfRestClientInterceptor interceptor) {
        return restTemplate -> {
            List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
            boolean alreadyRegistered = interceptors.stream()
                    .anyMatch(existing -> existing instanceof CpfRestClientInterceptor);
            if (!alreadyRegistered) {
                interceptors.add(0, interceptor);
                restTemplate.setInterceptors(interceptors);
            }
        };
    }

    /**
     * WebClient 기반 하위 서비스 호출을 CPF integration 파일 로그로 기록합니다.
     */
    private ExchangeFilterFunction integrationFileLogFilter(
            ObjectProvider<CpfIntegrationLogPort> fileLogWriterProvider,
            CpfHttpHeaderLogSanitizer headerSanitizer) {
        return (request, next) -> {
            long started = System.nanoTime();
            writeOutboundEvent(
                    fileLogWriterProvider,
                    request,
                    "OUTBOUND_REQUEST",
                    null,
                    "REQUESTED",
                    null,
                    null,
                    null,
                    headerSanitizer,
                    started);
            return next.exchange(request)
                    .doOnSuccess(response -> {
                        if (response != null) {
                            writeOutboundEvent(
                                    fileLogWriterProvider,
                                    request,
                                    response.statusCode().isError() ? "OUTBOUND_RESPONSE_ERROR" : "OUTBOUND_RESPONSE",
                                    response.statusCode().value(),
                                    response.statusCode().isError() ? "FAILED" : "SUCCESS",
                                    response.statusCode().isError() ? "HTTP_" + response.statusCode().value() : null,
                                    response.statusCode().isError() ? "하위 서비스 HTTP 오류" : null,
                                    null,
                                    headerSanitizer,
                                    started);
                        }
                    })
                    .doOnError(error -> {
                        String eventType = error instanceof TimeoutException ? "OUTBOUND_TIMEOUT" : "OUTBOUND_EXCEPTION";
                        writeOutboundEvent(
                                fileLogWriterProvider,
                                request,
                                eventType,
                                0,
                                "FAILED",
                                error.getClass().getSimpleName(),
                                error.getMessage(),
                                error instanceof TimeoutException ? "Y" : "N",
                                headerSanitizer,
                                started);
                    });
        };
    }

    private void writeOutboundEvent(
            ObjectProvider<CpfIntegrationLogPort> fileLogWriterProvider,
            ClientRequest request,
            String eventType,
            Integer httpStatus,
            String status,
            String failureCode,
            String failureMessage,
            String timeoutYn,
            CpfHttpHeaderLogSanitizer headerSanitizer,
            long started) {

        CpfIntegrationLogPort writer = fileLogWriterProvider.getIfAvailable();
        if (writer == null) {
            return;
        }
        writer.writeIntegration(
                null,
                CpfTargetSystemResolver.resolve(request.headers(), request.url()),
                "OUTBOUND",
                request.method().name(),
                request.url().getPath(),
                httpStatus,
                status,
                elapsedMillis(started),
                failureCode,
                failureMessage,
                Map.of(
                        "eventType", eventType,
                        "endpointCode", request.url().getHost() + ":" + request.url().getPort(),
                        "timeoutMs", 0,
                        "timeoutYn", timeoutYn == null ? "N" : timeoutYn,
                        "retryCount", 0,
                        "requestHeadersMasked", request.headers().toString()));
    }

    private long elapsedMillis(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
