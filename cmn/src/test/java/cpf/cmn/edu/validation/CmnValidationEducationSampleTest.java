package cpf.cmn.edu.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CmnValidationEducationSampleTest {

    @Test
    void businessKeyAllowsOnlyStandardCharacters() {
        CmnValidationEducationSample sample = new CmnValidationEducationSample();

        assertThat(sample.validateBusinessKey("MBR_MEMBER")).isEqualTo("MBR_MEMBER");
        assertThatThrownBy(() -> sample.validateBusinessKey("member")).isInstanceOf(IllegalArgumentException.class);
    }
}
