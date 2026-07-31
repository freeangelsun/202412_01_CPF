package com.cpf.gateway.scg;

import com.cpf.gateway.config.CpfGatewaySafetyProperties;
import java.time.Duration;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.impl.NoConnectionReuseStrategy;
import org.apache.hc.core5.util.Timeout;
import org.springframework.cloud.gateway.server.mvc.config.GatewayMvcProperties;
import org.springframework.cloud.gateway.server.mvc.handler.ProxyExchange;
import org.springframework.cloud.gateway.server.mvc.handler.RestClientProxyExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** Gateway 전용 HTTP client가 검증된 주소와 원 TLS hostname을 하나의 연결 identity로 사용하게 합니다. */
@Configuration(proxyBeanMethods = false)
public class CpfGatewayPinnedHttpClientConfiguration {
    @Bean
    ProxyExchange cpfGatewayPinnedProxyExchange(
            GatewayMvcProperties gatewayMvcProperties,
            CpfGatewaySafetyProperties safety) {
        safety.validate();
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(timeout(safety.getConnectTimeoutCap()))
                .setSocketTimeout(timeout(safety.getResponseTimeoutCap()))
                .build();
        PoolingHttpClientConnectionManager manager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDnsResolver(CpfGatewayPinnedAddressContext::resolve)
                .setDefaultConnectionConfig(connectionConfig)
                .setMaxConnTotal(200)
                .setMaxConnPerRoute(50)
                .build();
        CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(manager)
                .setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE)
                .disableRedirectHandling()
                .disableAutomaticRetries()
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(client);
        RestClient restClient = RestClient.builder().requestFactory(requestFactory).build();
        return new RestClientProxyExchange(restClient, gatewayMvcProperties);
    }

    private static Timeout timeout(Duration value) {
        return Timeout.ofMilliseconds(value.toMillis());
    }
}
