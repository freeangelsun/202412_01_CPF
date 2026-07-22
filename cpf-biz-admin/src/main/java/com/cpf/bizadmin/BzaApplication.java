package com.cpf.bizadmin;

import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 고객 업무 운영 기능을 제공하는 BZA 실행 애플리케이션입니다.
 *
 * <p>CPF 기술 공통과 CMN 고객 공통, BZA 인증·권한·업무 운영 컴포넌트를 함께 스캔합니다.
 * BZA DB는 {@code cpf.bza.datasource.enabled=true}일 때 전용 설정이 생성하므로, 기본 datasource와
 * 사용하지 않는 MyBatis 자동설정은 제외해 DB 미사용 개발 환경도 명확하게 기동할 수 있습니다.</p>
 */
@SpringBootApplication(
        scanBasePackages = {"com.cpf.core", "com.cpf.common", "com.cpf.bizadmin"},
        exclude = {DataSourceAutoConfiguration.class, MybatisAutoConfiguration.class})
public class BzaApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(BzaApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(BzaApplication.class);
    }
}
