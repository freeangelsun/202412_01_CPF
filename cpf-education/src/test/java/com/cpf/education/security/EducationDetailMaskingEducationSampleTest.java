package com.cpf.education.security;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EducationDetailMaskingEducationSampleTest {

    @Test
    void nameMaskingKeepsOnlyFirstCharacter() {
        assertThat(new EducationDetailMaskingEducationSample().name("홍길동")).isEqualTo("홍**");
    }
}
