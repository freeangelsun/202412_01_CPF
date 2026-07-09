package cpf.bat.edu.archive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class BatArchiveCompressionEducationSampleTest {
    @TempDir
    Path tempDir;

    @Test
    void batchResultFileCanBeZippedByPfwArchiveCapability() {
        assertThat(new BatArchiveCompressionEducationSample().zipResultFile(tempDir).status())
                .isEqualTo("SUCCESS");
    }
}
