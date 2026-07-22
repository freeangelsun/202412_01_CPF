package com.cpf.reference.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReferenceValidationEducationSampleTest {

    @Test
    void statusValidationRejectsUnknownStatus() {
        ReferenceValidationEducationSample sample = new ReferenceValidationEducationSample();

        assertThat(sample.validateStatus("READY")).isEqualTo("READY");
        assertThatThrownBy(() -> sample.validateStatus("BAD")).isInstanceOf(IllegalArgumentException.class);
    }
}
