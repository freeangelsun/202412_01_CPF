package cpf.bat.job;

import org.junit.jupiter.api.Test;
import org.springframework.batch.repeat.RepeatStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BatSmokeTaskletTest {

    @Test
    void smokeTaskletReturnsFinished() throws Exception {
        BatSmokeTasklet tasklet = new BatSmokeTasklet();

        RepeatStatus status = tasklet.execute(null, null);

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }

    @Test
    void failTaskletThrowsExpectedException() {
        BatFailTasklet tasklet = new BatFailTasklet();

        assertThatThrownBy(() -> tasklet.execute(null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("실패 흐름 검증");
    }
}
