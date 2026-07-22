package cpf.bat.edu.transaction;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BatPartialRollbackEducationSampleTest {

    @Test
    void splitKeepsCommittedAndRollbackIdsSeparated() {
        BatPartialRollbackEducationSample.PartialRollbackPlan plan = new BatPartialRollbackEducationSample()
                .split(List.of("S1"), List.of("F1", "F2"));

        assertThat(plan.hasRollback()).isTrue();
        assertThat(plan.rollbackIds()).containsExactly("F1", "F2");
    }
}
