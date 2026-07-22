package cpf.pfw.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/** 선택 설치하는 PFW Servlet Gateway 실행 애플리케이션입니다. */
@SpringBootApplication(scanBasePackages = {"cpf.pfw", "cpf.pfw.gateway"})
public class PfwGatewayApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(PfwGatewayApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PfwGatewayApplication.class);
    }
}
