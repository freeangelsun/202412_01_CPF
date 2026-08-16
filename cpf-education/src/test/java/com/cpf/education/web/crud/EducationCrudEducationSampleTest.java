package com.cpf.education.web.crud;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EducationCrudEducationSampleTest {

    @Test
    void createUpdateAndFindAreSeparated() {
        EducationCrudEducationSample sample = new EducationCrudEducationSample();
        sample.create("1", "first");
        sample.update("1", "second");

        assertThat(sample.find("1")).isEqualTo("second");
    }
}
