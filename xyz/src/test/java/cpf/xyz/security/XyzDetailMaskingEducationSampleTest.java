package cpf.xyz.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzDetailMaskingEducationSampleTest {

    @Test
    void nameMaskingKeepsOnlyFirstCharacter() {
        assertThat(new XyzDetailMaskingEducationSample().name("홍길동")).isEqualTo("홍**");
    }
}
