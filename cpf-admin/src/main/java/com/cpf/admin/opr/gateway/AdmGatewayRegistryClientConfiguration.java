package com.cpf.admin.opr.gateway;

import com.cpf.admin.opr.context.AdmAuthenticatedOperatorContext;
import com.cpf.core.api.gateway.CpfGatewayRegistryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/** Gateway가 분리 WAS로 배치될 때 ADM에 Timeout·Audience·Body HMAC이 적용된 Remote Typed Port를 구성합니다. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "cpf.admin.gateway-control", name = "enabled", havingValue = "true")
public class AdmGatewayRegistryClientConfiguration {
    @Bean
    @ConditionalOnMissingBean(CpfGatewayRegistryPort.class)
    public CpfGatewayRegistryPort remoteCpfGatewayRegistryPort(
            WebClient.Builder builder,
            AdmAuthenticatedOperatorContext actorContext,
            ObjectMapper mapper,
            @Value("${cpf.admin.gateway-control.base-url}") String baseUrl,
            @Value("${cpf.admin.gateway-control.shared-secret}") String sharedSecret,
            @Value("${cpf.admin.gateway-control.key-id:current}") String keyId,
            @Value("${cpf.admin.gateway-control.audience}") String audience,
            @Value("${cpf.admin.gateway-control.connect-timeout-millis:3000}") int connectTimeoutMillis,
            @Value("${cpf.admin.gateway-control.response-timeout-millis:10000}") int responseTimeoutMillis,
            @Value("${cpf.admin.gateway-control.overall-timeout-millis:15000}") int overallTimeoutMillis) {
        if (connectTimeoutMillis < 1 || responseTimeoutMillis < 1 || overallTimeoutMillis < responseTimeoutMillis) {
            throw new IllegalStateException("Gateway Control timeout 설정이 올바르지 않습니다.");
        }
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
                .responseTimeout(Duration.ofMillis(responseTimeoutMillis));
        WebClient.Builder isolated = builder.clone().clientConnector(new ReactorClientHttpConnector(httpClient));
        return new RemoteCpfGatewayRegistryAdapter(
                isolated, actorContext, mapper, baseUrl, sharedSecret, keyId, audience,
                Duration.ofMillis(overallTimeoutMillis));
    }
}
