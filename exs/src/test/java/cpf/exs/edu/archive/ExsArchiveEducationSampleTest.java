package cpf.exs.edu.archive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ExsArchiveEducationSampleTest {
    @TempDir
    Path tempDir;

    @Test
    void outboundZipUsesPfwArchiveCapability() {
        assertThat(new ExsArchiveEducationSample().outboundZip(tempDir).status()).isEqualTo("SUCCESS");
    }
}
