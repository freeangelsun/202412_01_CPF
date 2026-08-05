package com.cpf.core.common.logging.file;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

class CpfFileLogWriterConcurrencyTest {
    @TempDir Path tempDir;

    @Test
    void releasesPerPathLockEntriesAfterConcurrentWrites() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", tempDir.toString())
                .withProperty("cpf.logging.file.retention-check-interval-ms", "0")
                .withProperty("cpf.framework.module-id", "CORE");
        CpfFileLogWriter writer = new CpfFileLogWriter(environment);
        try (var pool = Executors.newFixedThreadPool(12)) {
            List<Future<?>> writes = new ArrayList<>();
            for (int index = 0; index < 200; index++) {
                int sequence = index;
                writes.add(pool.submit(() -> writer.writeEventAtRelativePath(
                        Path.of("concurrency/shared.log"), Map.of("sequence", sequence))));
            }
            for (Future<?> write : writes) write.get();
        }
        assertThat(writer.retainedLockEntryCount()).isZero();
    }
    @Test
    void plainAndCompressedPathsShareTheSameLogicalLock() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", tempDir.toString())
                .withProperty("cpf.logging.file.retention-check-interval-ms", "0")
                .withProperty("cpf.framework.module-id", "CORE");
        CpfFileLogWriter writer = new CpfFileLogWriter(environment);
        Method logicalLockKey = CpfFileLogWriter.class.getDeclaredMethod("logicalLockKey", Path.class);
        logicalLockKey.setAccessible(true);

        Path plain = tempDir.resolve("application.log");
        Path compressed = tempDir.resolve("application.log.gz");

        assertThat(logicalLockKey.invoke(writer, compressed))
                .isEqualTo(logicalLockKey.invoke(writer, plain));
    }

}
