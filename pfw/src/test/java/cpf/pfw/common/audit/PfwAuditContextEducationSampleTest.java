package cpf.pfw.common.audit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PfwAuditContextEducationSampleTest {

    @Test
    void auditContextRequiresReasonAndKeepsDiff() {
        PfwAuditContextEducationSample sample = new PfwAuditContextEducationSample();

        assertThat(sample.changed("operator", "상태 변경").diff())
                .containsEntry("before.status", "READY")
                .containsEntry("after.status", "DONE");
        assertThatThrownBy(() -> sample.changed("operator", " "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
