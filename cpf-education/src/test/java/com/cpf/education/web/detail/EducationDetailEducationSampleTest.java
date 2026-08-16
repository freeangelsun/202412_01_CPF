package com.cpf.education.web.detail;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EducationDetailEducationSampleTest {

    @Test
    void detailMasksEmailAndMarksAuditAction() {
        assertThat(new EducationDetailEducationSample().detail(true, "user@example.com"))
                .containsEntry("email", "u***@example.com")
                .containsEntry("audit", "READ_DETAIL");
    }
}
