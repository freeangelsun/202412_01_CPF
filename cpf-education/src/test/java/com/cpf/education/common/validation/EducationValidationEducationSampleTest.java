package com.cpf.education.common.validation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EducationValidationEducationSampleTest {

    @Test
    void statusValidationRejectsUnknownStatus() {
        EducationValidationEducationSample sample = new EducationValidationEducationSample();

        assertThat(sample.validateStatus("READY")).isEqualTo("READY");
        assertThatThrownBy(() -> sample.validateStatus("BAD")).isInstanceOf(IllegalArgumentException.class);
    }
}
