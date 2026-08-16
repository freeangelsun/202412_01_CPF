package com.cpf.education.operations.audit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EducationAuditEducationSampleTest {

    @Test
    void auditRecordRequiresActorActionAndReason() {
        EducationAuditEducationSample.AuditRecord record = new EducationAuditEducationSample()
                .changed("operator", "상태 변경");

        assertThat(record.action()).isEqualTo("UPDATE");
        assertThat(record.reason()).isEqualTo("상태 변경");
    }
}
