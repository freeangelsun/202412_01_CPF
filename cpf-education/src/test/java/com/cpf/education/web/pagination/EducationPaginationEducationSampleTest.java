package com.cpf.education.web.pagination;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EducationPaginationEducationSampleTest {

    @Test
    void offsetAndKeysetPageReturnExpectedRows() {
        EducationPaginationEducationSample sample = new EducationPaginationEducationSample();

        assertThat(sample.offsetPage(List.of(1, 2, 3, 4), 1, 2)).containsExactly(2, 3);
        assertThat(sample.keysetPage(List.of(1, 2, 3, 4), 2, 2)).containsExactly(3, 4);
    }
}
