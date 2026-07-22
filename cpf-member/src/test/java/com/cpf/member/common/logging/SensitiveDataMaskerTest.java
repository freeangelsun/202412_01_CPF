package cpf.mbr.common.logging;

import cpf.pfw.common.logging.SensitiveDataMasker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 */
class SensitiveDataMaskerTest {

    /**
     * CPF 기능 설명입니다.
     */
    @Test
    void masksJsonStringValuesWithSpaces() {
        // CPF 기능 설명입니다.
        String masked = SensitiveDataMasker.mask("{\"password\":\"abc def\", \"name\":\"tester\"}");

        // CPF 기능 설명입니다.
        assertThat(masked).contains("\"password\":\"***\"");
        // CPF 기능 설명입니다.
        assertThat(masked).doesNotContain("abc def");
        // CPF 기능 설명입니다.
        assertThat(masked).contains("\"name\":\"tester\"");
    }

    /**
     * CPF 기능 설명입니다.
     */
    @Test
    void masksQueryParametersAndBearerTokens() {
        // CPF 기능 설명입니다.
        String masked = SensitiveDataMasker.mask("accountNo=1234567890&Authorization=Bearer abc.def.ghi");

        // CPF 기능 설명입니다.
        assertThat(masked).contains("accountNo=***");
        // CPF 기능 설명입니다.
        assertThat(masked).contains("Authorization=***");
        // CPF 기능 설명입니다.
        assertThat(masked).doesNotContain("1234567890");
        // CPF 기능 설명입니다.
        assertThat(masked).doesNotContain("abc.def.ghi");
    }
}

