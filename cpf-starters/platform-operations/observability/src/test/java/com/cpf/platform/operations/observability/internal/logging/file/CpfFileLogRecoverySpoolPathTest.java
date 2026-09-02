package com.cpf.platform.operations.observability.internal.logging.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfFileLogRecoverySpoolPathTest {

    @Test
    void localDurableFallbackStaysUnderCanonicalLogRoot(@TempDir Path repositoryRoot) throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.environment", "local")
                .withProperty("cpf.repository-root", repositoryRoot.toString())
                .withProperty("cpf.framework.module-id", "BAT");
        CpfLogPathPolicy policy = new CpfLogPathPolicy(environment);
        Path expectedSpoolRoot = policy.recoveryPath(Path.of("file-log-recovery-spool"));

        try (CpfFileLogRecoverySpool spool = new CpfFileLogRecoverySpool(
                environment, Clock.systemUTC(), (target, record, checksum) -> false)) {
            assertTrue(spool.enqueue(repositoryRoot.resolve("target.log"), "{\"transactionId\":\"TX-PATH\"}"));
            assertTrue(Files.isDirectory(expectedSpoolRoot));
            try (var entries = Files.list(expectedSpoolRoot)) {
                assertTrue(entries.anyMatch(path -> path.getFileName().toString().endsWith(".spool")));
            }
        }

        assertTrue(expectedSpoolRoot.startsWith(policy.logRoot()));
        assertFalse(Files.exists(repositoryRoot.resolve(".cpf-file-log-recovery")));
    }
}
