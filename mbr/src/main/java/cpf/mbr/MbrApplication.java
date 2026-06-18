package cpf.mbr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * CPF 기능 설명입니다.
 * 
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * 
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * 
 * CPF 기능 설명입니다.
 *   ./gradlew :mbr:bootRun --args='--spring.profiles.active=local'
 * 
 * @author CPF Team
 * @version 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {"cpf.pfw", "cpf.cmn", "cpf.mbr"})
public class MbrApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MbrApplication.class, args);
    }
}

