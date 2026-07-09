package cpf.pfw.common.archive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PfwArchiveEducationSampleTest {
    @TempDir
    Path tempDir;

    @Test
    void educationSampleCreatesBusinessZip() {
        CpfArchiveResult result = new PfwArchiveEducationSample().createBusinessZip(tempDir);

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.outputPath()).exists();
    }
}
