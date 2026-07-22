package com.cpf.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * ACC 주제영역 실행 애플리케이션입니다.
 *
 * <p>모듈 부트스트랩만 소유하며 업무 기능은 {@code com.cpf.account.<feature>} 슬라이스에 둡니다.</p>
 */
@SpringBootApplication(scanBasePackages = {"com.cpf.core", "com.cpf.common", "com.cpf.account"})
public class AccApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(AccApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(AccApplication.class);
    }
}
