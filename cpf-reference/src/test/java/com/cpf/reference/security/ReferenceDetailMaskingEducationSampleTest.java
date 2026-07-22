package com.cpf.reference.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceDetailMaskingEducationSampleTest {

    @Test
    void nameMaskingKeepsOnlyFirstCharacter() {
        assertThat(new ReferenceDetailMaskingEducationSample().name("홍길동")).isEqualTo("홍**");
    }
}
