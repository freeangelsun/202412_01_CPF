package cpf.mbr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * CPF 회원 주제영역 실행 애플리케이션입니다.
 *
 * <p>회원 데이터와 인증 거래를 소유하고, 타 주제영역 기능은 CMN Facade Contract와
 * PFW Service Call Engine을 통해 호출합니다.</p>
 */
@SpringBootApplication
@ComponentScan(basePackages = {"cpf.pfw", "cpf.cmn", "cpf.mbr"})
public class MbrApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MbrApplication.class, args);
    }
}
