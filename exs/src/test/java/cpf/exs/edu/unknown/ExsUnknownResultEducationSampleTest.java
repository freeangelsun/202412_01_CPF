package cpf.exs.edu.unknown;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExsUnknownResultEducationSampleTest {

    @Test
    void blankResponseIsUnknown() {
        assertThat(new ExsUnknownResultEducationSample().classify("").unknown()).isTrue();
    }
}
