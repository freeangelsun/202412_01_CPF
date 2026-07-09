package cpf.xyz.edu.detail;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzDetailEducationSampleTest {

    @Test
    void detailMasksEmailAndMarksAuditAction() {
        assertThat(new XyzDetailEducationSample().detail(true, "user@example.com"))
                .containsEntry("email", "u***@example.com")
                .containsEntry("audit", "READ_DETAIL");
    }
}
