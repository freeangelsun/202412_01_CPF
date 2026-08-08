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

    @Test
    void failedHeadDoesNotStarveHealthyTailFromDifferentTarget() throws Exception {
        Path base = Files.createTempDirectory("cpf-logfail-fairness-");
        Path spoolRoot = base.resolve("spool");
        MockEnvironment env = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", base.resolve("logs").toString())
                .withProperty("cpf.logging.file.recovery-spool-root", spoolRoot.toString())
                .withProperty("cpf.logging.file.recovery-spool-backoff-millis", "100");
        Path failing = base.resolve("logs/a/failing.log");
        Path healthy = base.resolve("logs/b/healthy.log");
        CpfFileLogRecoverySpool spool = new CpfFileLogRecoverySpool(env, Clock.systemUTC(),
                (path, record, checksum) -> {
                    if (path.equals(failing.toAbsolutePath().normalize())) return false;
                    Files.createDirectories(path.getParent());
                    Files.writeString(path, record + System.lineSeparator(), StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    return true;
                });
        try {
            assertThat(spool.enqueue(failing, "{\"event\":\"head\"}")).isTrue();
            assertThat(spool.enqueue(healthy, "{\"event\":\"tail\"}")).isTrue();
            try (var files = Files.list(spoolRoot)) {
                files.filter(p -> p.getFileName().toString().endsWith(".spool"))
                        .forEach(p -> { try { Files.setLastModifiedTime(p, FileTime.from(Instant.EPOCH)); } catch (Exception e) { throw new RuntimeException(e); } });
            }
            spool.replayAvailable();
            assertThat(Files.exists(healthy)).isTrue();
            assertThat(Files.readString(healthy)).contains("tail");
            assertThat(spool.diagnostics().pending()).isEqualTo(1L);
            assertThat(spool.diagnostics().replayed()).isEqualTo(1L);
        } finally {
            spool.close();
        }
    }
    @Test
    void failedTargetPreservesPerTargetOrderAcrossRepeatedReplay() throws Exception {
        Path base = Files.createTempDirectory("cpf-logfail-order-");
        Path spoolRoot = base.resolve("spool");
        MockEnvironment env = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", base.resolve("logs").toString())
                .withProperty("cpf.logging.file.recovery-spool-root", spoolRoot.toString())
                .withProperty("cpf.logging.file.recovery-spool-backoff-millis", "100");
        Path target = base.resolve("logs/a/ordered.log");
        java.util.concurrent.atomic.AtomicInteger attempts = new java.util.concurrent.atomic.AtomicInteger();
        java.util.List<String> applied = new java.util.ArrayList<>();
        CpfFileLogRecoverySpool spool = new CpfFileLogRecoverySpool(env, Clock.systemUTC(),
                (path, record, checksum) -> {
                    if (attempts.getAndIncrement() == 0) return false;
                    applied.add(record);
                    return true;
                });
        try {
            assertThat(spool.enqueue(target, "{\"event\":\"first\"}")).isTrue();
            assertThat(spool.enqueue(target, "{\"event\":\"second\"}")).isTrue();
            try (var files = Files.list(spoolRoot)) {
                files.filter(p -> p.getFileName().toString().endsWith(".spool"))
                        .forEach(p -> { try { Files.setLastModifiedTime(p, FileTime.from(Instant.EPOCH)); } catch (Exception e) { throw new RuntimeException(e); } });
            }
            spool.replayAvailable();
            assertThat(applied).isEmpty();
            assertThat(spool.diagnostics().pending()).isEqualTo(2L);
            try (var files = Files.list(spoolRoot)) {
                files.filter(p -> p.getFileName().toString().endsWith(".spool"))
                        .forEach(p -> { try { Files.setLastModifiedTime(p, FileTime.from(Instant.EPOCH)); } catch (Exception e) { throw new RuntimeException(e); } });
            }
            spool.replayAvailable();
            assertThat(applied).hasSize(2);
            assertThat(applied.get(0)).contains("first");
            assertThat(applied.get(1)).contains("second");
            assertThat(spool.diagnostics().pending()).isZero();
        } finally {
            spool.close();
        }
    }

    @Test
    void accessDeniedHeadStillAllowsHealthyTailAndRemainsRetryable() throws Exception {
        Path base = Files.createTempDirectory("cpf-logfail-access-");
        Path spoolRoot = base.resolve("spool");
        MockEnvironment env = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", base.resolve("logs").toString())
                .withProperty("cpf.logging.file.recovery-spool-root", spoolRoot.toString())
                .withProperty("cpf.logging.file.recovery-spool-backoff-millis", "100");
        Path denied = base.resolve("logs/denied/head.log");
        Path healthy = base.resolve("logs/ok/tail.log");
        CpfFileLogRecoverySpool spool = new CpfFileLogRecoverySpool(env, Clock.systemUTC(),
                (path, record, checksum) -> {
                    if (path.equals(denied.toAbsolutePath().normalize())) throw new java.nio.file.AccessDeniedException(path.toString());
                    Files.createDirectories(path.getParent());
                    Files.writeString(path, record + System.lineSeparator(), StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    return true;
                });
        try {
            assertThat(spool.enqueue(denied, "{\"event\":\"denied\"}")).isTrue();
            assertThat(spool.enqueue(healthy, "{\"event\":\"healthy\"}")).isTrue();
            ageAll(spoolRoot);
            spool.replayAvailable();
            assertThat(Files.readString(healthy)).contains("healthy");
            assertThat(spool.diagnostics().pending()).isEqualTo(1L);
            ageAll(spoolRoot);
            spool.replayAvailable();
            assertThat(spool.diagnostics().pending()).isEqualTo(1L);
        } finally { spool.close(); }
    }

    @Test
    void durablePendingEntrySurvivesRestartAndReplaysWithoutNewWrite() throws Exception {
        Path base = Files.createTempDirectory("cpf-logfail-restart-");
        Path spoolRoot = base.resolve("spool");
        MockEnvironment env = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", base.resolve("logs").toString())
                .withProperty("cpf.logging.file.recovery-spool-root", spoolRoot.toString())
                .withProperty("cpf.logging.file.recovery-spool-backoff-millis", "100");
        Path target = base.resolve("logs/restart/recovered.log");
        CpfFileLogRecoverySpool first = new CpfFileLogRecoverySpool(env, Clock.systemUTC(), (p, r, c) -> false);
        assertThat(first.enqueue(target, "{\"event\":\"survive-restart\"}")).isTrue();
        first.close();
        assertThat(Files.list(spoolRoot).filter(p -> p.getFileName().toString().endsWith(".spool")).count()).isEqualTo(1L);
        CpfFileLogRecoverySpool second = new CpfFileLogRecoverySpool(env, Clock.systemUTC(), (path, record, checksum) -> {
            Files.createDirectories(path.getParent());
            Files.writeString(path, record + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        });
        try {
            ageAll(spoolRoot);
            second.replayAvailable();
            assertThat(Files.readString(target)).contains("survive-restart");
            assertThat(second.diagnostics().pending()).isZero();
        } finally { second.close(); }
    }

    @Test
    void concurrentReplayersCannotDuplicateRecoveredRecord() throws Exception {
        Path base = Files.createTempDirectory("cpf-logfail-concurrent-");
        Path spoolRoot = base.resolve("spool");
        MockEnvironment env = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", base.resolve("logs").toString())
                .withProperty("cpf.logging.file.recovery-spool-root", spoolRoot.toString())
                .withProperty("cpf.logging.file.recovery-spool-backoff-millis", "100");
        Path target = base.resolve("logs/concurrent/recovered.log");
        java.util.concurrent.atomic.AtomicInteger appends = new java.util.concurrent.atomic.AtomicInteger();
        CpfFileLogRecoverySpool spool = new CpfFileLogRecoverySpool(env, Clock.systemUTC(), (path, record, checksum) -> {
            appends.incrementAndGet();
            Thread.sleep(20);
            return true;
        });
        try {
            assertThat(spool.enqueue(target, "{\"event\":\"once\"}")).isTrue();
            ageAll(spoolRoot);
            Thread a = new Thread(spool::replayAvailable); Thread b = new Thread(spool::replayAvailable);
            a.start(); b.start(); a.join(); b.join();
            assertThat(appends.get()).isEqualTo(1);
            assertThat(spool.diagnostics().pending()).isZero();
        } finally { spool.close(); }
    }

    @Test
    void maxEntriesPressureFailsClosedWithoutDeletingExistingPendingEntries() throws Exception {
        Path base = Files.createTempDirectory("cpf-logfail-pressure-");
        Path spoolRoot = base.resolve("spool");
        MockEnvironment env = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", base.resolve("logs").toString())
                .withProperty("cpf.logging.file.recovery-spool-root", spoolRoot.toString())
                .withProperty("cpf.logging.file.recovery-spool-max-entries", "32")
                .withProperty("cpf.logging.file.recovery-spool-backoff-millis", "100");
        CpfFileLogRecoverySpool spool = new CpfFileLogRecoverySpool(env, Clock.systemUTC(), (p, r, c) -> false);
        try {
            for (int i = 0; i < 32; i++) {
                assertThat(spool.enqueue(base.resolve("logs/t" + i + ".log"), "{\"event\":\"" + i + "\"}")).isTrue();
            }
            assertThat(spool.enqueue(base.resolve("logs/overflow.log"), "{\"event\":\"overflow\"}")).isFalse();
            assertThat(spool.diagnostics().pending()).isEqualTo(32L);
            assertThat(spool.diagnostics().terminalLoss()).isEqualTo(1L);
        } finally { spool.close(); }
    }

    private static void ageAll(Path spoolRoot) throws Exception {
        try (var files = Files.list(spoolRoot)) {
            files.filter(p -> p.getFileName().toString().endsWith(".spool"))
                    .forEach(p -> { try { Files.setLastModifiedTime(p, FileTime.from(Instant.EPOCH)); } catch (Exception e) { throw new RuntimeException(e); } });
        }
    }

}
