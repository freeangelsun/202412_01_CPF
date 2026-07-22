package com.cpf.reference.archive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceArchiveEducationSampleTest {
    @TempDir
    Path tempDir;

    @Test
    void archiveSampleUsesCpfArchiveCapability() {
        assertThat(new ReferenceArchiveEducationSample().createZip(tempDir).outputPath()).exists();
    }
}
