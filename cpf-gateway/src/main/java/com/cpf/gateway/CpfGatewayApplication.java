package com.cpf.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/** 외부 진입점과 공통 정책 집행을 담당하는 CPF Gateway 실행 애플리케이션입니다. */
@SpringBootApplication(scanBasePackages = {"com.cpf.core", "com.cpf.gateway"})
public class CpfGatewayApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(CpfGatewayApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(CpfGatewayApplication.class);
    }
}
