package external;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** external Generated API 실행 진입점입니다. */
@SpringBootApplication(scanBasePackages="external")
@MapperScan("external.sample.repository")
public class ExternalOnlineApplication {
    public static void main(String[] args) { SpringApplication.run(ExternalOnlineApplication.class,args); }
}
