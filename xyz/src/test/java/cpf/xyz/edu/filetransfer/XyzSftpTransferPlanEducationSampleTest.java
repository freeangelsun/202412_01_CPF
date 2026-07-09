package cpf.xyz.edu.filetransfer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzSftpTransferPlanEducationSampleTest {

    @Test
    void sftpTransferPlanUsesPfwFileTransferRequest() {
        assertThat(new XyzSftpTransferPlanEducationSample().plan("T-1").endpointCode())
                .isEqualTo("XYZ_BANK_A");
    }
}
