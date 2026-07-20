package cpf.acc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Account 주제영역 실행 애플리케이션입니다.
 */
@SpringBootApplication(scanBasePackages = {"cpf.pfw", "cpf.cmn", "cpf.acc"})
public class AccountApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(AccountApplication.class);
    }
}