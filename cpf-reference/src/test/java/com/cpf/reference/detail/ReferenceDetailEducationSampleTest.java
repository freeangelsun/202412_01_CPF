package com.cpf.reference.detail;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceDetailEducationSampleTest {

    @Test
    void detailMasksEmailAndMarksAuditAction() {
        assertThat(new ReferenceDetailEducationSample().detail(true, "user@example.com"))
                .containsEntry("email", "u***@example.com")
                .containsEntry("audit", "READ_DETAIL");
    }
}
