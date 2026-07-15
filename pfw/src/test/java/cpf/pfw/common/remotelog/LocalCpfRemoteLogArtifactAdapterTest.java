package cpf.pfw.common.remotelog;

import cpf.pfw.common.logging.file.CpfFileLogWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalCpfRemoteLogArtifactAdapterTest {

    @TempDir
    Path tempDir;

    @Test
    void 허용된로그를검색하고미리보기에서민감정보를마스킹한다() throws Exception {
        MockEnvironment environment = environment();
        CpfFileLogWriter writer = new CpfFileLogWriter(environment);
        LocalCpfRemoteLogArtifactAdapter adapter = new LocalCpfRemoteLogArtifactAdapter(writer, environment);
        Path logFile = tempDir.resolve("local/adm/admAP01/application/cpf-adm-application.log");
        Files.createDirectories(logFile.getParent());
        Files.writeString(logFile, "{\"transactionGlobalId\":\"TX-001\",\"password\":\"secret\"}\n", StandardCharsets.UTF_8);

        var artifacts = adapter.search(new CpfRemoteLogArtifactSearch(
                "local", "ADM", "admAP01", "application", null, "TX-001", null, 10));

        assertThat(artifacts).hasSize(1);
        assertThat(artifacts.getFirst().relativePath()).isEqualTo("local/adm/admAP01/application/cpf-adm-application.log");
        CpfRemoteLogPreview preview = adapter.preview(artifacts.getFirst().artifactId(), 10, "TX-001");
        assertThat(preview.lines()).singleElement().asString().contains("\"password\":\"***\"");
    }

    @Test
    void 상위경로와허용되지않은확장자를차단한다() throws Exception {
        MockEnvironment environment = environment();
        CpfFileLogWriter writer = new CpfFileLogWriter(environment);
        LocalCpfRemoteLogArtifactAdapter adapter = new LocalCpfRemoteLogArtifactAdapter(writer, environment);
        Path forbidden = tempDir.resolve("local/adm/admAP01/application/secret.txt");
        Files.createDirectories(forbidden.getParent());
        Files.writeString(forbidden, "secret", StandardCharsets.UTF_8);
        String traversal = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("../secret.txt".getBytes(StandardCharsets.UTF_8));

        assertThat(adapter.search(new CpfRemoteLogArtifactSearch(
                null, null, null, null, null, null, null, 10))).isEmpty();
        assertThatThrownBy(() -> adapter.resolveDownload(traversal))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 표준실행식별자와파일메타데이터를조합해검색한다() throws Exception {
        MockEnvironment environment = environment();
        CpfFileLogWriter writer = new CpfFileLogWriter(environment);
        LocalCpfRemoteLogArtifactAdapter adapter = new LocalCpfRemoteLogArtifactAdapter(writer, environment);
        Path logFile = tempDir.resolve("local/adm/admAP01/batch/cpf-adm-batch.log");
        Files.createDirectories(logFile.getParent());
        Files.writeString(logFile, """
                {"standardBatchId":"BADM-RLG-EX-0001","jobInstanceId":"1001","schedulerId":"SCH-01"}
                """, StandardCharsets.UTF_8);

        var artifacts = adapter.search(new CpfRemoteLogArtifactSearch(
                "local", "ADM", "ADM", "admAP01", "batch", "cpf-adm",
                null, "BADM-RLG-EX-0001", null, null, null,
                "1001", null, null, "SCH-01",
                Instant.now().minusSeconds(60), Instant.now().plusSeconds(60),
                1L, 10_000L, false, null, 10));

        assertThat(artifacts).singleElement().satisfies(artifact -> {
            assertThat(artifact.service()).isEqualTo("ADM");
            assertThat(artifact.onlineStatus()).isEqualTo("ONLINE");
            assertThat(artifact.retentionExpiresAt()).isAfter(artifact.modifiedAt());
        });
    }

    private MockEnvironment environment() {
        return new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", tempDir.toAbsolutePath().toString())
                .withProperty("cpf.environment", "local")
                .withProperty("cpf.framework.module-id", "ADM")
                .withProperty("cpf.framework.instance-id", "admAP01");
    }
}
