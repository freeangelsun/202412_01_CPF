package com.cpf.core.common.logging.file;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfFileLogRecoveryContractTest {
    @Test
    void durableSpoolMasksThenReplaysThroughInjectedHardenedAppender() throws Exception {
        Path base = Files.createTempDirectory("cpf-logfail-test-");
        Path spoolRoot = base.resolve("spool");
        MockEnvironment env = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", base.resolve("logs").toAbsolutePath().toString())
                .withProperty("cpf.logging.file.recovery-spool-root", spoolRoot.toString())
                .withProperty("cpf.logging.file.recovery-spool-backoff-millis", "100");
        Path target = base.resolve("logs/local/app/local-01/recovered.log");
        CpfFileLogRecoverySpool spool = new CpfFileLogRecoverySpool(env, Clock.systemUTC(),
                (path, record, checksum) -> {
                    Files.createDirectories(path.getParent());
                    Files.writeString(path, record + System.lineSeparator(), StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    return true;
                });
        try {
            assertThat(spool.enqueue(target, "{\"password\":\"secret-value\",\"event\":\"FAIL\"}")).isTrue();
            Path item;
            try (var files = Files.list(spoolRoot)) {
                item = files.filter(p -> p.getFileName().toString().endsWith(".spool")).findFirst().orElseThrow();
            }
            Files.setLastModifiedTime(item, FileTime.from(Instant.EPOCH));
            spool.replayAvailable();

            String recovered = Files.readString(target);
            assertThat(recovered).doesNotContain("secret-value").contains("\"cpfRecoveryChecksum\"");
            assertThat(spool.diagnostics().pending()).isZero();
            assertThat(spool.diagnostics().replayed()).isEqualTo(1L);
            assertThat(spool.diagnostics().terminalLoss()).isZero();
        } finally {
            spool.close();
        }
    }

    @Test
    void productionRequiresExplicitDurableRecoveryRoot() throws Exception {
        Path base = Files.createTempDirectory("cpf-logfail-prod-");
        MockEnvironment env = new MockEnvironment()
                .withProperty("cpf.environment", "prod")
                .withProperty("cpf.logging.file.base-path", base.resolve("logs").toAbsolutePath().toString())
                .withProperty("CPF_INSTANCE_ID", "adm-prod-01");

        assertThatThrownBy(() -> new CpfFileLogRecoverySpool(env, Clock.systemUTC(), (p, r, c) -> true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("recovery-spool-root");
    }
}
