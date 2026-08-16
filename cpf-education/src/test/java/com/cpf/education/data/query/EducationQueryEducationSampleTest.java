package com.cpf.education.data.query;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EducationQueryEducationSampleTest {

    @Test
    void statusFilterKeepsMatchingRowsSorted() {
        assertThat(new EducationQueryEducationSample().filterByStatus(List.of("B:READY", "A:DONE", "A:READY"), "READY"))
                .containsExactly("A:READY", "B:READY");
    }
}
