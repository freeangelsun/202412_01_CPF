package cpf.xyz.audit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzAuditEducationSampleTest {

    @Test
    void auditRecordRequiresActorActionAndReason() {
        XyzAuditEducationSample.AuditRecord record = new XyzAuditEducationSample()
                .changed("operator", "상태 변경");

        assertThat(record.action()).isEqualTo("UPDATE");
        assertThat(record.reason()).isEqualTo("상태 변경");
    }
}
