package cpf.xyz.archive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class XyzArchiveEducationSampleTest {
    @TempDir
    Path tempDir;

    @Test
    void archiveSampleUsesPfwArchiveCapability() {
        assertThat(new XyzArchiveEducationSample().createZip(tempDir).outputPath()).exists();
    }
}
