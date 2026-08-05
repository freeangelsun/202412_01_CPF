package com.cpf.batch.worker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class ApprovedFileExecutorTest {
    @TempDir Path temp;

    @Test
    void waitsForStableFileAndValidatesMarkerAndChecksum() throws Exception {
        WorkerOperationalProperties properties = properties(temp);
        ApprovedFileExecutor executor = new ApprovedFileExecutor(properties);
        Path file = temp.resolve("inbox/data.dat");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "payload");
        Files.writeString(file.resolveSibling("data.dat.done"), "ok");
        String sha = executor.fingerprint(file).sha256();

        Path ready = executor.awaitReady(new ApprovedFileExecutor.FileWatchRequest(
                "inbox", "data.dat", Duration.ofSeconds(3), Duration.ofMillis(300),
                ".done", 7L, sha));

        assertEquals(file.toAbsolutePath().normalize(), ready);
    }

    @Test
    void preventsDuplicateClaimWithFencingToken() throws Exception {
        WorkerOperationalProperties properties = properties(temp);
        ApprovedFileExecutor executor = new ApprovedFileExecutor(properties);
        Path file = temp.resolve("inbox/data.dat");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "payload");

        ApprovedFileExecutor.Claim first = executor.claim("inbox", "data.dat", "worker-1", Duration.ofMinutes(1));
        assertThrows(java.nio.file.FileAlreadyExistsException.class,
                () -> executor.claim("inbox", "data.dat", "worker-2", Duration.ofMinutes(1)));
        executor.release(first);
        assertFalse(Files.exists(first.claimPath()));
    }


    @Test
    void restartScanUsesDirectoryAliasWithoutApplyingFileExtensionPolicy() throws Exception {
        ApprovedFileExecutor executor = new ApprovedFileExecutor(properties(temp));
        Path nested = temp.resolve("inbox/nested/data.dat");
        Files.createDirectories(nested.getParent());
        Files.writeString(nested, "payload");

        assertEquals(List.of(nested.toAbsolutePath().normalize()), executor.restartScan("inbox", "."));
    }

    @Test
    void expiredClaimReacquisitionAlwaysIncreasesFencingToken() throws Exception {
        ApprovedFileExecutor executor = new ApprovedFileExecutor(properties(temp));
        Path file = temp.resolve("inbox/data.dat");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "payload");
        Path claim = file.resolveSibling("data.dat.cpf-claim");
        long previousToken = Long.MAX_VALUE - 10;
        Files.writeString(claim, "worker-old\n" + previousToken + "\n2000-01-01T00:00:00Z\n");

        ApprovedFileExecutor.Claim reacquired = executor.claim(
                "inbox", "data.dat", "worker-new", Duration.ofMinutes(1));

        assertTrue(reacquired.fencingToken() > previousToken);
        assertEquals("worker-new", reacquired.ownerId());
    }


    @Test
    void concurrentExpiredClaimTakeoverHasExactlyOneWinner() throws Exception {
        ApprovedFileExecutor firstExecutor = new ApprovedFileExecutor(properties(temp));
        ApprovedFileExecutor secondExecutor = new ApprovedFileExecutor(properties(temp));
        Path file = temp.resolve("inbox/data.dat");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "payload");
        Path claim = file.resolveSibling("data.dat.cpf-claim");
        Files.writeString(claim, "worker-old\n41\n2000-01-01T00:00:00Z\n");

        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<ApprovedFileExecutor.Claim> first = pool.submit(() -> {
                start.await();
                return firstExecutor.claim("inbox", "data.dat", "worker-1", Duration.ofMinutes(1));
            });
            Future<ApprovedFileExecutor.Claim> second = pool.submit(() -> {
                start.await();
                return secondExecutor.claim("inbox", "data.dat", "worker-2", Duration.ofMinutes(1));
            });
            start.countDown();

            int winners = 0;
            ApprovedFileExecutor.Claim winner = null;
            for (Future<ApprovedFileExecutor.Claim> attempt : List.of(first, second)) {
                try {
                    winner = attempt.get();
                    winners++;
                } catch (java.util.concurrent.ExecutionException failure) {
                    assertInstanceOf(java.nio.file.FileAlreadyExistsException.class, failure.getCause());
                }
            }
            assertEquals(1, winners);
            assertNotNull(winner);
            assertTrue(winner.fencingToken() > 41L);
        }
    }

    @Test
    void fencingTokenRemainsMonotonicAfterRelease() throws Exception {
        ApprovedFileExecutor executor = new ApprovedFileExecutor(properties(temp));
        Path file = temp.resolve("inbox/data.dat");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "payload");

        ApprovedFileExecutor.Claim first = executor.claim(
                "inbox", "data.dat", "worker-1", Duration.ofMinutes(1));
        executor.release(first);
        ApprovedFileExecutor.Claim second = executor.claim(
                "inbox", "data.dat", "worker-2", Duration.ofMinutes(1));

        assertTrue(second.fencingToken() > first.fencingToken());
        assertFalse(executor.restartScan("inbox", ".").stream()
                .anyMatch(path -> path.getFileName().toString().contains("cpf-claim")));
    }

    @Test
    void blocksTraversalAndUnapprovedExtension() {
        ApprovedFileExecutor executor = new ApprovedFileExecutor(properties(temp));
        assertThrows(SecurityException.class, () -> executor.resolve("inbox", "../escape.dat"));
        assertThrows(SecurityException.class, () -> executor.resolve("inbox", "payload.exe"));
    }

    private static WorkerOperationalProperties properties(Path root) {
        WorkerOperationalProperties properties = new WorkerOperationalProperties();
        WorkerOperationalProperties.PathAlias inbox = new WorkerOperationalProperties.PathAlias();
        inbox.setRoot(root.resolve("inbox").toString());
        inbox.setAllowedExtensions(List.of("dat", "done"));
        inbox.setStableWindowSeconds(1);
        inbox.setMaxFileSizeBytes(1024 * 1024);
        properties.setPathAliases(Map.of("inbox", inbox));
        return properties;
    }
}
