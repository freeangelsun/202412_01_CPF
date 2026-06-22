package cpf.bat.operation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * BAT 운영 API가 공유하는 상태 보관 객체를 구성합니다.
 */
@Configuration
public class BatOperationConfig {

    @Bean
    public BatSmokeExecutionRegistry batSmokeExecutionRegistry() {
        return new BatSmokeExecutionRegistry();
    }
}
