package com.cpf.admin.opr.gateway;

import com.cpf.admin.opr.context.AdmAuthenticatedOperatorContext;
import com.cpf.core.api.gateway.CpfGatewayRegistryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/** Gateway가 분리 WAS로 배치될 때 ADM에 Remote Typed Port를 구성합니다. */
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
            @Value("${cpf.admin.gateway-control.shared-secret}") String sharedSecret) {
        return new RemoteCpfGatewayRegistryAdapter(builder, actorContext, mapper, baseUrl, sharedSecret);
    }
}
