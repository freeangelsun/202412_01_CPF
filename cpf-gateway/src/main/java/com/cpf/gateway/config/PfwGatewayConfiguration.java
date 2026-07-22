package cpf.pfw.gateway.config;

import cpf.pfw.common.execution.CpfExecutionCatalogPort;
import cpf.pfw.common.gateway.CpfGatewayAuthorizationPort;
import cpf.pfw.common.gateway.CpfGatewayRouteCatalog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

/** PFW Gateway runtime의 data plane Bean을 구성합니다. */
@Configuration
@EnableScheduling
public class PfwGatewayConfiguration {

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
