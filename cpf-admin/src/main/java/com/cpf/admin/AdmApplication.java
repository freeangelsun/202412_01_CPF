package com.cpf.admin;

import com.cpf.admin.config.AdmBootstrapProperties;
import com.cpf.admin.config.AdmPasswordPolicyProperties;
import com.cpf.admin.config.AdmSecurityProperties;
import com.cpf.admin.opr.parameter.AdmParameterReferenceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * CPF ADM 운영 애플리케이션입니다.
 *
 * <p>CPF/CMN 공통 모듈을 함께 스캔하고 ADM 운영 화면과 BAT/업무 Owner Control Plane 연동을 제공합니다.</p>
 */
@SecurityScheme(name = "admSessionCookie", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.COOKIE, paramName = "CPFSESSION", description = "ADM Browser BFF HttpOnly same-origin session cookie; state-changing requests also require X-XSRF-TOKEN")
@SecurityScheme(name = "admCsrfHeader", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = "X-XSRF-TOKEN", description = "CSRF token paired with the same-origin XSRF-TOKEN cookie for state-changing ADM requests")
// com.cpf.security.common 은 AdmSessionService/AdmApiAuthFilter 가 직접 요구하는 CMN 보안
// Service 들의 패키지다. 지금까지는 One-WAS 통합 Runtime(CpfLocalRuntimeModules)만 이 패키지를
// 스캔했기 때문에 ADM 단독 기동은 "No qualifying bean of type CmnCryptoService" 로 죽었다.
@SpringBootApplication(scanBasePackages = {"com.cpf.core", "com.cpf.common", "com.cpf.security.common", "com.cpf.admin"})
@EnableConfigurationProperties({AdmBootstrapProperties.class, AdmPasswordPolicyProperties.class, AdmSecurityProperties.class, AdmParameterReferenceProperties.class})
@EnableScheduling
public class AdmApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(AdmApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(AdmApplication.class);
    }
}
