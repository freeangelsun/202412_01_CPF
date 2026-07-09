package cpf.cmn.edu.masking;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CmnMaskingEducationSampleTest {

    @Test
    void phoneMaskingKeepsPrefixAndSuffix() {
        assertThat(new CmnMaskingEducationSample().phone("01012345678")).isEqualTo("010****5678");
    }
}
