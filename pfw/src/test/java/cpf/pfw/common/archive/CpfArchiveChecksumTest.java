package cpf.pfw.common.archive;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class CpfArchiveChecksumTest {

    @Test
    void sha256ReturnsStableHexValue() {
        String checksum = CpfArchiveChecksum.sha256("cpf".getBytes(StandardCharsets.UTF_8));

        assertThat(checksum).hasSize(64);
        assertThat(checksum).isEqualTo(CpfArchiveChecksum.sha256("cpf".getBytes(StandardCharsets.UTF_8)));
    }
}
