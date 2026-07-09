package cpf.cmn.edu.fixedlength;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CmnFixedLengthLayoutEducationSampleTest {

    @Test
    void memberLayoutDefinesTotalByteLength() {
        assertThat(new CmnFixedLengthLayoutEducationSample().memberLayout().totalLength()).isEqualTo(14);
    }
}
