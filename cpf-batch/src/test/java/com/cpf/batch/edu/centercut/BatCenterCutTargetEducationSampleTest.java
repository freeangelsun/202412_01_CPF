package com.cpf.batch.edu.centercut;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BatCenterCutTargetEducationSampleTest {

    @Test
    void targetSelectionHonorsLimit() {
        assertThat(new BatCenterCutTargetEducationSample().selectTargets(List.of("A", "B", "C"), 2))
                .containsExactly("A", "B");
    }
}
