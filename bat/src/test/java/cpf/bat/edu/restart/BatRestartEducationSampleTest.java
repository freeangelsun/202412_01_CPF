package cpf.bat.edu.restart;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatRestartEducationSampleTest {

    @Test
    void restartUsesNextItemAfterCheckpoint() {
        assertThat(new BatRestartEducationSample().restartFrom(10).nextItemNo()).isEqualTo(11);
    }
}
