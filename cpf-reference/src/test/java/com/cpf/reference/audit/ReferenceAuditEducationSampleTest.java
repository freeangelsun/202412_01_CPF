package com.cpf.reference.audit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceAuditEducationSampleTest {

    @Test
    void auditRecordRequiresActorActionAndReason() {
        ReferenceAuditEducationSample.AuditRecord record = new ReferenceAuditEducationSample()
                .changed("operator", "상태 변경");

        assertThat(record.action()).isEqualTo("UPDATE");
        assertThat(record.reason()).isEqualTo("상태 변경");
    }
}
