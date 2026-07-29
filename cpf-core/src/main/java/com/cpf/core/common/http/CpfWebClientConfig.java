package com.cpf.core.common.http;

import com.cpf.core.common.header.CpfHeaderPropagator;
import com.cpf.core.api.http.CpfWebhookSignaturePort;
import com.cpf.core.common.logging.file.CpfFileLogWriter;
import com.cpf.core.common.servicecall.CpfServiceCallEngine;
import com.cpf.core.common.workflow.CpfWorkflowContext;
import io.netty.channel.ChannelOption;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.boot.restclient.RestTemplateCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;

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
    @ConditionalOnMissingBean
    public CpfWebhookRuntimePolicy cpfWebhookRuntimePolicy() { return new CpfWebhookRuntimePolicy(); }

    @Bean
    @ConditionalOnMissingBean
    public CpfWebhookCallbackClient cpfWebhookCallbackClient(
            CpfWebClient webClient,
            CpfWebhookRuntimePolicy policy,
            ObjectProvider<CpfWebhookSignaturePort> signatureProvider) {
        return new CpfWebhookCallbackClient(webClient, policy, signatureProvider.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfApiClientRuntimePolicy cpfApiClientRuntimePolicy() { return new CpfApiClientRuntimePolicy(); }

    @Bean
    public CpfServiceEndpointRegistry cpfServiceEndpointRegistry(CpfServiceEndpointProperties properties) {
        return new CpfServiceEndpointRegistry(properties);
    }

    @Bean
    public CpfWebClient cpfWebClient(
            CpfHttpClientProperties httpClientProperties,
            CpfServiceEndpointRegistry endpointRegistry,
            CpfApiClientRuntimePolicy runtimePolicy,
            ObjectProvider<CpfFileLogWriter> fileLogWriterProvider,
            ObjectProvider<CpfServiceCallEngine> serviceCallEngineProvider,
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
                .filter(transactionHeaderPropagationFilter(CpfLocalServiceIdentity.from(environment)))
                .filter(integrationFileLogFilter(fileLogWriterProvider));

        return new CpfWebClient(builder, endpointRegistry, serviceCallEngineProvider, runtimePolicy);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfRestClientInterceptor cpfRestClientInterceptor(
            ObjectProvider<CpfFileLogWriter> fileLogWriterProvider,
            Environment environment) {
        return new CpfRestClientInterceptor(
                fileLogWriterProvider.getIfAvailable(),
                CpfLocalServiceIdentity.from(environment));
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
     * 하위 서비스 호출 전에 CPF 표준 거래 헤더와 워크플로 헤더를 추가합니다.
     */
    private ExchangeFilterFunction transactionHeaderPropagationFilter(CpfLocalServiceIdentity localServiceIdentity) {
        return (request, next) -> {
            ClientRequest.Builder requestBuilder = ClientRequest.from(request);

            for (Map.Entry<String, String> header : CpfHeaderPropagator.outboundHeaders().entrySet()) {
                if (hasText(header.getValue()) && !request.headers().containsHeader(header.getKey())) {
                    requestBuilder.header(header.getKey(), header.getValue());
                }
            }
            for (Map.Entry<String, String> header : CpfWorkflowContext.propagationHeaders().entrySet()) {
                if (hasText(header.getValue()) && !request.headers().containsHeader(header.getKey())) {
                    requestBuilder.header(header.getKey(), header.getValue());
                }
            }
            // 외부 입력의 호출자 값을 다음 hop으로 넘기지 않고 실제 현재 서비스 신원으로 재생성합니다.
            requestBuilder.headers(headers -> {
                headers.set(com.cpf.core.common.header.CpfHeaderNames.CALLER_SERVICE, localServiceIdentity.serviceId());
                headers.set(com.cpf.core.common.header.CpfHeaderNames.CALLER_INSTANCE_ID, localServiceIdentity.instanceId());
            });

            return next.exchange(requestBuilder.build());
        };
    }

    /**
     * WebClient 기반 하위 서비스 호출을 CPF integration 파일 로그로 기록합니다.
     */
    private ExchangeFilterFunction integrationFileLogFilter(ObjectProvider<CpfFileLogWriter> fileLogWriterProvider) {
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
                                started);
                    });
        };
    }

    private void writeOutboundEvent(
            ObjectProvider<CpfFileLogWriter> fileLogWriterProvider,
            ClientRequest request,
            String eventType,
            Integer httpStatus,
            String status,
            String failureCode,
            String failureMessage,
            String timeoutYn,
            long started) {

        CpfFileLogWriter writer = fileLogWriterProvider.getIfAvailable();
        if (writer == null) {
            return;
        }
        writer.writeIntegration(
                null,
                CpfTargetServiceResolver.resolve(request.headers(), request.url()),
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
