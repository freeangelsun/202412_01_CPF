package com.cpf.reference.telegram;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceFixedLengthBusinessUseEducationSampleTest {

    @Test
    void businessLayoutUsesCmnFixedLengthSpec() {
        assertThat(new ReferenceFixedLengthBusinessUseEducationSample().layout().totalLength()).isEqualTo(12);
    }
}
