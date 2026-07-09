package cpf.cmn.edu.converter;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CmnConverterEducationSampleTest {

    @Test
    void amountConvertsToTwoScaleDecimal() {
        assertThat(new CmnConverterEducationSample().amount("100.00")).isEqualByComparingTo(new BigDecimal("100.00"));
    }
}
