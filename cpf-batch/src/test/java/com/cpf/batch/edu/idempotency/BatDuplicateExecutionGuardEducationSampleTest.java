package cpf.bat.edu.idempotency;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatDuplicateExecutionGuardEducationSampleTest {

    @Test
    void duplicateExecutionIsRejectedUntilReleased() {
        BatDuplicateExecutionGuardEducationSample guard = new BatDuplicateExecutionGuardEducationSample();

        assertThat(guard.acquire("JOB", "20260708")).isTrue();
        assertThat(guard.acquire("JOB", "20260708")).isFalse();
        guard.release("JOB", "20260708");
        assertThat(guard.acquire("JOB", "20260708")).isTrue();
    }
}
