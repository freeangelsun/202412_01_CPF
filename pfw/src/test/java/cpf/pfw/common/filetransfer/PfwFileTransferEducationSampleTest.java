package cpf.pfw.common.filetransfer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PfwFileTransferEducationSampleTest {

    @Test
    void sftpPlanSeparatesContractFromExternalRuntime() {
        CpfFileTransferRequest request = new PfwFileTransferEducationSample()
                .planSftpUpload("20260708000000000BATlocal010000001", "BANK_A");

        assertThat(request.endpointCode()).isEqualTo("BANK_A");
        assertThat(request.attributes())
                .containsEntry("protocol", "SFTP")
                .containsEntry("runtime", "external-runtime-required");
    }
}
