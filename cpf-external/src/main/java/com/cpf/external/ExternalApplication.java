package com.cpf.external;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/** 대외기관 REST·전문·파일 연계와 결과 불명 복구를 담당하는 EXS 실행 애플리케이션입니다. */
@SpringBootApplication(scanBasePackages = {"com.cpf.core", "com.cpf.common", "com.cpf.external"})
public class ExternalApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ExternalApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ExternalApplication.class);
    }
}
