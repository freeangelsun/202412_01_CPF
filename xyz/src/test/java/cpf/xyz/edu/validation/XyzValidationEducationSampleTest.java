package cpf.xyz.edu.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XyzValidationEducationSampleTest {

    @Test
    void statusValidationRejectsUnknownStatus() {
        XyzValidationEducationSample sample = new XyzValidationEducationSample();

        assertThat(sample.validateStatus("READY")).isEqualTo("READY");
        assertThatThrownBy(() -> sample.validateStatus("BAD")).isInstanceOf(IllegalArgumentException.class);
    }
}
