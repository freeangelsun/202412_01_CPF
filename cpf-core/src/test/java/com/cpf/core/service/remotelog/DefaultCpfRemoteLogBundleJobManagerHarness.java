package com.cpf.core.service.remotelog;

import com.cpf.core.api.remotelog.CpfRemoteLogArtifact;
import com.cpf.core.api.remotelog.CpfRemoteLogArtifactPort;
import com.cpf.core.api.remotelog.CpfRemoteLogArtifactSearch;
import com.cpf.core.api.remotelog.CpfRemoteLogBundle;
import com.cpf.core.api.remotelog.CpfRemoteLogBundleJob;
import com.cpf.core.api.remotelog.CpfRemoteLogDownloadGrant;
import com.cpf.core.api.remotelog.CpfRemoteLogPreview;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Owner isolation, bounded async jobs, one-time token and sanitized failure harness. */
public final class DefaultCpfRemoteLogBundleJobManagerHarness {
    private DefaultCpfRemoteLogBundleJobManagerHarness() { }

    public static void main(String[] args) throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        FakeArtifactPort artifactPort = new FakeArtifactPort(clock);
        DefaultCpfRemoteLogBundleJobManager.Settings settings =
                new DefaultCpfRemoteLogBundleJobManager.Settings(
                        1, 4, 10, 4, 10, 3, Duration.ofMinutes(5), Duration.ofMinutes(1));
        DefaultCpfRemoteLogBundleJobManager manager =
                new DefaultCpfRemoteLogBundleJobManager(artifactPort, clock, settings);
        try {
            CpfRemoteLogBundleJob submitted = manager.submit("owner-A", List.of("a", "a", "b"));
            check(submitted.requestedArtifactCount() == 2, "artifact ids must be deduplicated");
            CpfRemoteLogBundleJob completed = awaitTerminal(manager, submitted.jobId(), "owner-A");
            check("COMPLETED".equals(completed.status()), "bundle job must complete");
            check(completed.includedArtifactCount() == 2, "included count");
            check(artifactPort.createCalls.get() == 1, "artifact port must be the actual consumer");

            boolean ownerHidden = false;
            try {
                manager.find(submitted.jobId(), "owner-B");
            } catch (NoSuchElementException expected) {
                ownerHidden = true;
            }
            check(ownerHidden, "cross-owner job lookup must be indistinguishable from not-found");

            CpfRemoteLogDownloadGrant grant = manager.issueDownloadGrant(submitted.jobId(), "owner-A");
            check(!grant.toString().contains(grant.token()), "grant toString must redact the token");
            boolean wrongTokenRejected = false;
            try {
                manager.resolveDownload(submitted.jobId(), "owner-A", "x".repeat(32));
            } catch (SecurityException expected) {
                wrongTokenRejected = true;
            }
            check(wrongTokenRejected, "wrong token must fail closed without consuming the grant");
            CpfRemoteLogBundle bundle = manager.resolveDownload(
                    submitted.jobId(), "owner-A", grant.token());
            check(bundle.includedCount() == 2, "valid owner-scoped token resolves the bundle");
            boolean replayRejected = false;
            try {
                manager.resolveDownload(submitted.jobId(), "owner-A", grant.token());
            } catch (SecurityException expected) {
                replayRejected = true;
            }
            check(replayRejected, "download token must be single-use");

            CpfRemoteLogBundleJob expiring = manager.submit("owner-A", List.of("c"));
            awaitTerminal(manager, expiring.jobId(), "owner-A");
            CpfRemoteLogDownloadGrant expiringGrant =
                    manager.issueDownloadGrant(expiring.jobId(), "owner-A");
            clock.advance(Duration.ofMinutes(2));
            boolean expiredRejected = false;
            try {
                manager.resolveDownload(expiring.jobId(), "owner-A", expiringGrant.token());
            } catch (SecurityException | NoSuchElementException expected) {
                expiredRejected = true;
            }
            check(expiredRejected, "expired grants must fail closed");

            CpfRemoteLogBundleJob failed = manager.submit("owner-A", List.of("fail"));
            CpfRemoteLogBundleJob failedResult = awaitTerminal(manager, failed.jobId(), "owner-A");
            check("FAILED".equals(failedResult.status()), "provider failure must become FAILED state");
            check(failedResult.errorMessage().equals("BUNDLE_CREATION_FAILED:IllegalStateException"),
                    "provider error details and secrets must not be exposed");

            boolean invalidOwnerRejected = false;
            try {
                manager.submit("owner\nforged", List.of("a"));
            } catch (IllegalArgumentException expected) {
                invalidOwnerRejected = true;
            }
            check(invalidOwnerRejected, "owner header injection must be rejected");
            check(!manager.diagnostics().toString().contains("owner-A"),
                    "diagnostics must not expose owners or tokens");
        } finally {
            manager.close();
        }
        rateLimitIsOwnerScoped(clock);

        boolean closedRejected = false;
        try {
            manager.submit("owner-A", List.of("a"));
        } catch (IllegalStateException expected) {
            closedRejected = true;
        }
        check(closedRejected, "closed manager must reject new jobs");
        System.out.println("CPF_REMOTE_LOG_BUNDLE_JOB_MANAGER_HARNESS_PASS");
    }

    private static void rateLimitIsOwnerScoped(MutableClock clock) throws Exception {
        FakeArtifactPort port = new FakeArtifactPort(clock);
        DefaultCpfRemoteLogBundleJobManager manager = new DefaultCpfRemoteLogBundleJobManager(
                port,
                clock,
                new DefaultCpfRemoteLogBundleJobManager.Settings(
                        1, 2, 10, 2, 1, 2, Duration.ofMinutes(5), Duration.ofMinutes(1)));
        try {
            manager.submit("rate-owner-A", List.of("a"));
            boolean limited = false;
            try {
                manager.submit("rate-owner-A", List.of("b"));
            } catch (java.util.concurrent.RejectedExecutionException expected) {
                limited = true;
            }
            check(limited, "per-owner submission rate must be bounded");
            manager.submit("rate-owner-B", List.of("b"));
        } finally {
            manager.close();
        }
    }

    private static CpfRemoteLogBundleJob awaitTerminal(
            DefaultCpfRemoteLogBundleJobManager manager, String jobId, String ownerId) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        CpfRemoteLogBundleJob current;
        do {
            current = manager.find(jobId, ownerId);
            if ("COMPLETED".equals(current.status()) || "FAILED".equals(current.status())) {
                return current;
            }
            Thread.sleep(5L);
        } while (System.nanoTime() < deadline);
        throw new AssertionError("bundle job did not reach a terminal state");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class FakeArtifactPort implements CpfRemoteLogArtifactPort {
        private final MutableClock clock;
        private final AtomicInteger createCalls = new AtomicInteger();

        private FakeArtifactPort(MutableClock clock) {
            this.clock = clock;
        }

        @Override public List<CpfRemoteLogArtifact> search(CpfRemoteLogArtifactSearch search) {
            return List.of();
        }

        @Override public CpfRemoteLogPreview preview(String artifactId, int lastLines, String keyword) {
            throw new UnsupportedOperationException();
        }

        @Override public Path resolveDownload(String artifactId) {
            throw new UnsupportedOperationException();
        }

        @Override public CpfRemoteLogBundle createBundle(List<String> artifactIds) {
            createCalls.incrementAndGet();
            if (artifactIds.contains("fail")) {
                throw new IllegalStateException("storage secret=must-not-leak");
            }
            return new CpfRemoteLogBundle(
                    "bundle-" + createCalls.get(),
                    "bundle.zip",
                    Path.of("bundles/bundle.zip"),
                    artifactIds.size(),
                    List.of(),
                    clock.instant().plus(Duration.ofMinutes(10)));
        }

        @Override public Map<String, Object> diagnostics() {
            return Map.of("state", "UP");
        }
    }

    private static final class MutableClock extends Clock {
        private volatile Instant current;

        private MutableClock(Instant current) {
            this.current = current;
        }

        private void advance(Duration duration) {
            current = current.plus(duration);
        }

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return current; }
    }
}
