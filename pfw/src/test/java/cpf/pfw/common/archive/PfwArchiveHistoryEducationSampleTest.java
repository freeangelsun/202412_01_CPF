package cpf.pfw.common.archive;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PfwArchiveHistoryEducationSampleTest {

    @Test
    void archiveHistoryKeepsOperationMetadataWithoutRawPayload() {
        CpfArchiveResult result = new CpfArchiveResult(
                "SUCCESS",
                CpfArchiveFormat.ZIP,
                Path.of("daily-result.zip"),
                2,
                128L,
                "0".repeat(64),
                Instant.parse("2026-07-09T03:00:00Z"),
                List.of());

        PfwArchiveHistoryEducationSample.ArchiveOperationHistory history =
                new PfwArchiveHistoryEducationSample().history("ARCHIVE-JOB-001", result);

        assertThat(history.archiveJobId()).isEqualTo("ARCHIVE-JOB-001");
        assertThat(history.checksum()).hasSize(64);
        assertThat(history.attributes())
                .containsEntry("rawPayloadStored", "false")
                .containsEntry("downloadPermissionRequired", "true");
    }
}
