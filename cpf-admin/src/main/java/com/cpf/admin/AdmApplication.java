package com.cpf.admin;

import com.cpf.admin.config.AdmBootstrapProperties;
import com.cpf.admin.config.AdmPasswordPolicyProperties;
import com.cpf.admin.config.AdmSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CPF ADM 운영 애플리케이션입니다.
 *
 * <p>CPF/CMN 공통 모듈을 함께 스캔하고, ADM 운영 화면과 배치 스케줄러를 제공합니다.</p>
 */
@SpringBootApplication(scanBasePackages = {"com.cpf.core", "com.cpf.common", "com.cpf.admin"})
@EnableConfigurationProperties({AdmBootstrapProperties.class, AdmPasswordPolicyProperties.class, AdmSecurityProperties.class})
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
