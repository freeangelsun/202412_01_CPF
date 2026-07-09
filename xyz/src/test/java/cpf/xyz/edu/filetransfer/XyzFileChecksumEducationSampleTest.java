package cpf.xyz.edu.filetransfer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzFileChecksumEducationSampleTest {

    @Test
    void checksumIsSha256Hex() {
        assertThat(new XyzFileChecksumEducationSample().sha256("file-body")).hasSize(64);
    }
}
