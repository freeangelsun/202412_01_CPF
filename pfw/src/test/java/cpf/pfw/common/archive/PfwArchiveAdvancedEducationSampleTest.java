package cpf.pfw.common.archive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PfwArchiveAdvancedEducationSampleTest {
    @TempDir
    Path tempDir;

    private final PfwArchiveAdvancedEducationSample sample = new PfwArchiveAdvancedEducationSample();

    @Test
    void recursiveZipCreatesHistoryMetadata() {
        PfwArchiveAdvancedEducationSample.ArchiveHistoryRecord history = sample.createRecursiveZip(tempDir);

        assertThat(history.status()).isEqualTo("SUCCESS");
        assertThat(history.entryCount()).isEqualTo(3);
        assertThat(history.checksum()).hasSize(64);
        assertThat(history.detail()).contains("ZIP");
    }

    @Test
    void duplicateAndSizeGuardAreBlocked() {
        assertThat(sample.duplicatePrevention(tempDir).blocked()).isTrue();
        assertThat(sample.maxSizeGuard(tempDir.resolve("guard")).blocked()).isTrue();
    }

    @Test
    void corruptedArchiveAndTarPlanAreExplicitlySeparated() {
        assertThat(sample.corruptedArchive(tempDir.resolve("corrupt")).detected()).isTrue();
        assertThat(sample.tarPlan(tempDir.resolve("tar")).status()).isEqualTo("PARTIAL_IMPLEMENTATION");
    }
}
