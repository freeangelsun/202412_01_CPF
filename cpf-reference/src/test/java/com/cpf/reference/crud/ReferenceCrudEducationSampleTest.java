package com.cpf.reference.crud;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceCrudEducationSampleTest {

    @Test
    void createUpdateAndFindAreSeparated() {
        ReferenceCrudEducationSample sample = new ReferenceCrudEducationSample();
        sample.create("1", "first");
        sample.update("1", "second");

        assertThat(sample.find("1")).isEqualTo("second");
    }
}
