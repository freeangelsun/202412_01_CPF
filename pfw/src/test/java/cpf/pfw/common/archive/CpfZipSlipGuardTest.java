package cpf.pfw.common.archive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfZipSlipGuardTest {
    @TempDir
    Path tempDir;

    @Test
    void safeResolveRejectsPathTraversal() {
        assertThatThrownBy(() -> CpfZipSlipGuard.safeResolve(tempDir, "../secret.txt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용 범위");
    }

    @Test
    void safeResolveKeepsEntryUnderBaseDirectory() {
        assertThat(CpfZipSlipGuard.safeResolve(tempDir, "safe/result.txt").toString())
                .startsWith(tempDir.toAbsolutePath().normalize().toString());
    }
}
