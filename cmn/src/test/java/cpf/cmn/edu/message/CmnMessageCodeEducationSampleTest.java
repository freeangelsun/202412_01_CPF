package cpf.cmn.edu.message;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class CmnMessageCodeEducationSampleTest {

    @Test
    void messageUsesLocaleSpecificText() {
        assertThat(new CmnMessageCodeEducationSample().message(Locale.KOREAN, "CMN-0001"))
                .isEqualTo("정상 처리되었습니다.");
    }
}
