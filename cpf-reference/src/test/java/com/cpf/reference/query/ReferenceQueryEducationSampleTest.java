package com.cpf.reference.query;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceQueryEducationSampleTest {

    @Test
    void statusFilterKeepsMatchingRowsSorted() {
        assertThat(new ReferenceQueryEducationSample().filterByStatus(List.of("B:READY", "A:DONE", "A:READY"), "READY"))
                .containsExactly("A:READY", "B:READY");
    }
}
