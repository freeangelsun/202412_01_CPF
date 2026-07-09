package cpf.cmn.edu.fixedlength;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CmnFixedLengthFormatterEducationSampleTest {

    @Test
    void formatterCreatesExpectedByteLengthMessage() {
        assertThat(new CmnFixedLengthFormatterEducationSample().formatMember("APP", "123", "A").byteLength())
                .isEqualTo(14);
    }
}
