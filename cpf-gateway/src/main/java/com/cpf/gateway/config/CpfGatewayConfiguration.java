package com.cpf.gateway.config;

import com.cpf.core.common.execution.CpfExecutionCatalogPort;
import com.cpf.core.common.gateway.CpfGatewayAuthorizationPort;
import com.cpf.core.common.gateway.CpfGatewayRouteCatalog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

/** CPF Gateway runtime의 data plane Bean을 구성합니다. */
@Configuration
@EnableScheduling
public class CpfGatewayConfiguration {

    @Bean
    public CpfGatewayRouteCatalog cpfGatewayRouteCatalog(CpfExecutionCatalogPort executionCatalog) {
        return new CpfGatewayRouteCatalog(executionCatalog);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfGatewayAuthorizationPort cpfGatewayAuthorizationPort() {
        // 인증 adapter가 없는 설치에서는 권한이 선언된 route를 기본 거부합니다.
        return (route, trustedHeaders) -> route.requiredPermission() == null
                || route.requiredPermission().isBlank();
    }

    @Bean
    public RestClient cpfGatewayRestClient(RestClient.Builder builder) {
        return builder.build();
    }
}
