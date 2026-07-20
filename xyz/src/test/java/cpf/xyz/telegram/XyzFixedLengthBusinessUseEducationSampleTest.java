package cpf.xyz.telegram;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzFixedLengthBusinessUseEducationSampleTest {

    @Test
    void businessLayoutUsesCmnFixedLengthSpec() {
        assertThat(new XyzFixedLengthBusinessUseEducationSample().layout().totalLength()).isEqualTo(12);
    }
}
