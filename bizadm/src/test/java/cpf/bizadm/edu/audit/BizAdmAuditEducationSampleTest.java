package cpf.bizadm.edu.audit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BizAdmAuditEducationSampleTest {

    @Test
    void auditRequiresReasonAndContainsDiff() {
        assertThat(new BizAdmAuditEducationSample().audit("operator", "권한 변경").diff())
                .containsEntry("before.status", "READY");
    }
}
