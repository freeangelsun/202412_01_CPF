package com.cpf.core.common.system;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** CPF 공식 프로젝트명과 시스템 코드 변환 계약을 검증합니다. */
class CpfSystemCodesTest {

    @Test
    void cpfCore와구형Cpf별칭은Cpf시스템코드로정규화한다() {
        assertThat(CpfSystemCodes.normalize("cpf-core", "APP")).isEqualTo("CPF");
        assertThat(CpfSystemCodes.normalize("core", "APP")).isEqualTo("CPF");
        assertThat(CpfSystemCodes.normalize("CPF", "APP")).isEqualTo("CPF");
        assertThat(CpfSystemCodes.normalize(null, "CPF")).isEqualTo("CPF");
    }

    @Test
    void 공식패키지명에서모든시스템코드를추론한다() {
        assertThat(CpfSystemCodes.inferFromTypeName("com.cpf.core.SampleController")).isEqualTo("CPF");
        assertThat(CpfSystemCodes.inferFromTypeName("com.cpf.gateway.SampleController")).isEqualTo("GWY");
        assertThat(CpfSystemCodes.inferFromTypeName("com.cpf.common.SampleService")).isEqualTo("CMN");
        assertThat(CpfSystemCodes.inferFromTypeName("com.cpf.bizadmin.SampleService")).isEqualTo("BZA");
        assertThat(CpfSystemCodes.inferFromTypeName("com.cpf.external.SampleService")).isEqualTo("EXS");
    }

    @Test
    void 신규업무코드는기존3자리호환규칙을유지한다() {
        assertThat(CpfSystemCodes.normalize("PAY", "CPF")).isEqualTo("PAY");
        assertThat(CpfSystemCodes.normalize("payment", "CPF")).isEqualTo("PAY");
        assertThat(CpfSystemCodes.normalize("A", "CPF")).isEqualTo("AXX");
    }
}
