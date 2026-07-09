package cpf.bizadm.edu.download;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BizAdmDownloadAuditEducationSampleTest {

    @Test
    void downloadRequiresPermissionAndReason() {
        assertThat(new BizAdmDownloadAuditEducationSample().decide(true, "업무 확인").downloadable()).isTrue();
        assertThat(new BizAdmDownloadAuditEducationSample().decide(true, "").downloadable()).isFalse();
    }
}
