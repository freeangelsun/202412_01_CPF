package com.cpf.local.runtime;

import com.cpf.admin.AdmApplication;
import com.cpf.admin.config.AdmBootstrapProperties;
import com.cpf.admin.config.AdmPasswordPolicyProperties;
import com.cpf.admin.config.AdmSecurityProperties;
import com.cpf.backoffice.online.BackofficeOnlineApplication;
import com.cpf.gateway.CpfGatewayApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * 개발자가 필요한 CPF 기능을 같은 JVM/한 Port에 조립하는 Local Module Catalog입니다.
 * Gateway는 외부 진입점 선택 Capability이므로 명시적으로 켠 경우에만 포함합니다.
 */
@Configuration(proxyBeanMethods = false)
public class CpfLocalRuntimeModules {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "cpf.local.modules", name = "core", havingValue = "true", matchIfMissing = true)
    @ComponentScan(basePackages = "com.cpf.core")
    static class CoreModule {
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "cpf.local.modules", name = "common", havingValue = "true", matchIfMissing = true)
    @ComponentScan(basePackages = "com.cpf.common")
    static class CommonModule {
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "cpf.local.modules", name = "gateway", havingValue = "true", matchIfMissing = false)
    @ComponentScan(
            basePackages = "com.cpf.gateway",
            excludeFilters = @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = CpfGatewayApplication.class))
    static class GatewayModule {
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "cpf.local.modules", name = "admin", havingValue = "true", matchIfMissing = true)
    @EnableConfigurationProperties({
            AdmBootstrapProperties.class,
            AdmPasswordPolicyProperties.class,
            AdmSecurityProperties.class
    })
    @ComponentScan(
            basePackages = "com.cpf.admin",
            excludeFilters = @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = AdmApplication.class))
    static class AdminModule {
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "cpf.local.modules", name = "backoffice", havingValue = "true")
    @ComponentScan(
            basePackages = {"com.cpf.backoffice.online", "com.cpf.security.common"},
            excludeFilters = @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = BackofficeOnlineApplication.class))
    static class BackofficeModule {
        // Backoffice 인증은 CPF 공통 토큰/암호 서비스(CmnJwtService 등)를 사용한다. 그 서비스들은
        // auto-configuration 이 아니라 @Service 스캔으로만 등록되므로, 1-WAS 조립에서도 같은
        // 패키지를 함께 스캔해야 Backoffice 가 기동한다.
    }
}
