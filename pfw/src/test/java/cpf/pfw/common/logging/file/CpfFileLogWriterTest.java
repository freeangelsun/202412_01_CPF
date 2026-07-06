package cpf.pfw.common.logging.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CpfFileLogWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writeEventMasksSensitiveValuesByKeyAndContent() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", tempDir.toString())
                .withProperty("cpf.framework.module-id", "ACC")
                .withProperty("server.port", "8080");
        CpfFileLogWriter writer = new CpfFileLogWriter(environment);

        writer.writeEvent("ACC", "integration", Map.of(
                "eventType", "OUTBOUND_REQUEST",
                "password", "plainSecret",
                "Authorization", "Bearer token-raw-value",
                "X-Api-Key", "api-key-raw",
                "nested", Map.of("credential", "credential-raw")));

        String content = Files.readString(tempDir.resolve("acc").resolve("cpf-acc-integration.log"));

        assertThat(content)
                .contains("\"password\":\"***\"")
                .contains("\"Authorization\":\"***\"")
                .contains("\"X-Api-Key\":\"***\"")
                .contains("credential=***")
                .doesNotContain("plainSecret")
                .doesNotContain("token-raw-value")
                .doesNotContain("api-key-raw")
                .doesNotContain("credential-raw");
    }
}
