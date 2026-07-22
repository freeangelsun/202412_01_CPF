package cpf.bat.operation;

import cpf.pfw.common.batch.centercut.CpfCenterCutService;
import cpf.pfw.common.logging.TransactionIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * BAT 운영 API와 smoke Job이 공유하는 실행 지원 객체를 구성합니다.
 */
@Configuration
public class BatOperationConfig {

    @Bean
    public BatSmokeExecutionRegistry batSmokeExecutionRegistry() {
        return new BatSmokeExecutionRegistry();
    }

    @Bean
    public CpfCenterCutService cpfCenterCutService(TransactionIdGenerator transactionIdGenerator) {
        return new CpfCenterCutService(transactionIdGenerator::generate);
    }
}
