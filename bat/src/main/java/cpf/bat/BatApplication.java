package cpf.bat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CPF BAT 독립 실행 애플리케이션입니다.
 *
 * <p>BAT는 PFW가 제공하는 Batch 공통 API를 사용해 실제 Job과 Step을 실행하는 worker입니다.
 * ADM은 BAT의 실행 상태와 이력을 관제하고, PFW는 공통 실행 Facade와 운영 메타 표준을 제공합니다.</p>
 */
@SpringBootApplication(scanBasePackages = {"cpf.bat", "cpf.pfw"})
public class BatApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatApplication.class, args);
    }
}
