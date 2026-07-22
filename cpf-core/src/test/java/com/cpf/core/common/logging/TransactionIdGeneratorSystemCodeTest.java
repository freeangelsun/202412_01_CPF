package com.cpf.core.common.logging;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/** 거래 ID의 cpf-core 시스템 코드가 CPF로 발급되는지 검증합니다. */
class TransactionIdGeneratorSystemCodeTest {

    @Test
    void cpfCore애플리케이션명은Cpf시스템코드를사용한다() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.application.name", "cpf-core")
                .withProperty("cpf.framework.was-id", "coreAP1");

        TransactionIdGenerator generator = new TransactionIdGenerator(environment);

        assertThat(generator.getModuleId()).isEqualTo("CPF");
        assertThat(generator.generate()).matches("\\d{17}CPFcoreAP1\\d{7}");
    }

    @Test
    void 구형Cpf설정도Cpf시스템코드로호환변환한다() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.framework.module-id", "CPF")
                .withProperty("cpf.framework.was-id", "coreAP1");

        TransactionIdGenerator generator = new TransactionIdGenerator(environment);

        assertThat(generator.getModuleId()).isEqualTo("CPF");
    }
}
