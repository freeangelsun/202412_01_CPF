package com.cpf.education.file.archive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class EducationArchiveEducationSampleTest {
    @TempDir
    Path tempDir;

    @Test
    void archiveSampleUsesCpfArchiveCapability() {
        assertThat(new EducationArchiveEducationSample().createZip(tempDir).outputPath()).exists();
    }
}
