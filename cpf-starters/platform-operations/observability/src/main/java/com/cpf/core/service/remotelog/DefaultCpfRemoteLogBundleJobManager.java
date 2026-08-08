package com.cpf.core.service.remotelog;

import com.cpf.core.api.remotelog.CpfRemoteLogArtifactPort;
import com.cpf.core.api.remotelog.CpfRemoteLogBundle;
import com.cpf.core.api.remotelog.CpfRemoteLogBundleJob;
import com.cpf.core.api.remotelog.CpfRemoteLogBundleJobPort;
import com.cpf.core.api.remotelog.CpfRemoteLogDownloadGrant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bounded default implementation of remote-log bundle jobs.
 *
 * <p>Jobs are owner-scoped, bundle creation is isolated on a bounded executor, raw download tokens
 * are never retained, and each grant is short-lived and single-use.</p>
 */
public final class DefaultCpfRemoteLogBundleJobManager
        implements CpfRemoteLogBundleJobPort, AutoCloseable {
    private static final int TOKEN_BYTES = 32;

    private final CpfRemoteLogArtifactPort artifactPort;
    private final Clock clock;
    private final SecureRandom secureRandom;
    private final Settings settings;
    private final ThreadPoolExecutor executor;
    private final ConcurrentHashMap<String, JobState> jobs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RateWindow> ownerRateWindows = new ConcurrentHashMap<>();
    private final AtomicBoolean closed = new AtomicBoolean();

    public DefaultCpfRemoteLogBundleJobManager(
            CpfRemoteLogArtifactPort artifactPort,
            Clock clock,
            Settings settings) {
        this(artifactPort, clock, settings, new SecureRandom());
    }

    DefaultCpfRemoteLogBundleJobManager(
            CpfRemoteLogArtifactPort artifactPort,
            Clock clock,
            Settings settings,
            SecureRandom secureRandom) {
        this.artifactPort = Objects.requireNonNull(artifactPort, "artifactPort");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.secureRandom = Objects.requireNonNull(secureRandom, "secureRandom");
        this.executor = new ThreadPoolExecutor(
                settings.workerCount(),
                settings.workerCount(),
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(settings.queueCapacity()),
                daemonThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    @Override
    public CpfRemoteLogBundleJob submit(String ownerId, List<String> artifactIds) {
        requireOpen();
        String owner = required(ownerId, "ownerId", 200);
        List<String> artifacts = artifactIds(artifactIds, settings.maxArtifactsPerJob());
        Instant submittedAt = clock.instant();
        enforceOwnerRate(owner, submittedAt);
        removeExpiredJobs();
        if (activeJobCount() >= settings.maxActiveJobs()) {
            throw new RejectedExecutionException("CPF_REMOTE_LOG_ACTIVE_JOB_CAPACITY_EXCEEDED");
        }
        if (jobs.size() >= settings.maxJobs()) {
            throw new RejectedExecutionException("CPF_REMOTE_LOG_JOB_CAPACITY_EXCEEDED");
        }
        String jobId = UUID.randomUUID().toString();
        JobState state = new JobState(
                jobId,
                owner,
                artifacts,
                submittedAt,
                safePlus(submittedAt, settings.jobTtl()));
        if (jobs.putIfAbsent(jobId, state) != null) {
            throw new IllegalStateException("CPF_REMOTE_LOG_JOB_ID_COLLISION");
        }
        try {
            executor.execute(() -> createBundle(state));
        } catch (RejectedExecutionException rejected) {
            jobs.remove(jobId, state);
            throw new RejectedExecutionException("CPF_REMOTE_LOG_JOB_QUEUE_FULL", rejected);
        }
        return state.snapshot(clock.instant());
    }

    @Override
    public CpfRemoteLogBundleJob find(String jobId, String ownerId) {
        return ownedState(jobId, ownerId).snapshot(clock.instant());
    }

    @Override
    public CpfRemoteLogDownloadGrant issueDownloadGrant(String jobId, String ownerId) {
        JobState state = ownedState(jobId, ownerId);
        Instant now = clock.instant();
        synchronized (state) {
            state.expireIfNecessary(now);
            if (!"COMPLETED".equals(state.status) || state.bundle == null) {
                throw new IllegalStateException("CPF_REMOTE_LOG_BUNDLE_NOT_READY");
            }
            Instant grantExpiry = min(state.expiresAt, safePlus(now, settings.grantTtl()));
            if (!grantExpiry.isAfter(now)) {
                state.expire(now);
                throw new IllegalStateException("CPF_REMOTE_LOG_JOB_EXPIRED");
            }
            byte[] raw = new byte[TOKEN_BYTES];
            secureRandom.nextBytes(raw);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
            state.grantHash = sha256(token);
            state.grantExpiresAt = grantExpiry;
            state.grantUsed = false;
            return new CpfRemoteLogDownloadGrant(state.jobId, token, grantExpiry);
        }
    }

    @Override
    public CpfRemoteLogBundle resolveDownload(String jobId, String ownerId, String token) {
        JobState state = ownedState(jobId, ownerId);
        String presentedToken = required(token, "token", 2048);
        Instant now = clock.instant();
        synchronized (state) {
            state.expireIfNecessary(now);
            if (!"COMPLETED".equals(state.status) || state.bundle == null) {
                throw new IllegalStateException("CPF_REMOTE_LOG_BUNDLE_NOT_READY");
            }
            if (state.grantHash == null || state.grantExpiresAt == null || state.grantUsed) {
                throw new SecurityException("CPF_REMOTE_LOG_DOWNLOAD_GRANT_REQUIRED");
            }
            if (!state.grantExpiresAt.isAfter(now)) {
                state.clearGrant();
                throw new SecurityException("CPF_REMOTE_LOG_DOWNLOAD_GRANT_EXPIRED");
            }
            if (!MessageDigest.isEqual(state.grantHash, sha256(presentedToken))) {
                throw new SecurityException("CPF_REMOTE_LOG_DOWNLOAD_GRANT_INVALID");
            }
            state.grantUsed = true;
            state.grantHash = null;
            return state.bundle;
        }
    }

    @Override
    public Map<String, Object> diagnostics() {
        removeExpiredJobs();
        long submitted = 0L;
        long running = 0L;
        long completed = 0L;
        long failed = 0L;
        for (JobState state : jobs.values()) {
            switch (state.status) {
                case "SUBMITTED" -> submitted++;
                case "RUNNING" -> running++;
                case "COMPLETED" -> completed++;
                case "FAILED" -> failed++;
                default -> { }
            }
        }
        return Map.ofEntries(
                Map.entry("state", closed.get() ? "CLOSED" : "RUNNING"),
                Map.entry("jobCount", jobs.size()),
                Map.entry("submittedCount", submitted),
                Map.entry("runningCount", running),
                Map.entry("completedCount", completed),
                Map.entry("failedCount", failed),
                Map.entry("queueDepth", executor.getQueue().size()),
                Map.entry("queueCapacity", settings.queueCapacity()),
                Map.entry("workerCount", settings.workerCount()),
                Map.entry("maxActiveJobs", settings.maxActiveJobs()),
                Map.entry("maxRequestsPerMinute", settings.maxRequestsPerMinute()));
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        executor.shutdownNow();
        for (JobState state : jobs.values()) {
            synchronized (state) {
                state.clearGrant();
            }
        }
    }

    private void createBundle(JobState state) {
        synchronized (state) {
            if (state.expireIfNecessary(clock.instant())) {
                return;
            }
            state.status = "RUNNING";
        }
        try {
            CpfRemoteLogBundle created = Objects.requireNonNull(
                    artifactPort.createBundle(state.artifactIds), "created bundle");
            synchronized (state) {
                Instant now = clock.instant();
                if (state.expireIfNecessary(now)) {
                    return;
                }
                validateBundle(created, state.artifactIds.size());
                state.bundle = created;
                state.failedArtifactIds = created.failedArtifactIds();
                state.includedArtifactCount = created.includedCount();
                state.completedAt = now;
                state.expiresAt = min(state.expiresAt, created.expiresAt());
                state.status = "COMPLETED";
            }
        } catch (RuntimeException failure) {
            synchronized (state) {
                state.bundle = null;
                state.completedAt = clock.instant();
                state.status = "FAILED";
                state.errorMessage = "BUNDLE_CREATION_FAILED:" + failure.getClass().getSimpleName();
                state.clearGrant();
            }
        }
    }

    private JobState ownedState(String jobId, String ownerId) {
        removeExpiredJobs();
        String id = required(jobId, "jobId", 200);
        String owner = required(ownerId, "ownerId", 200);
        JobState state = jobs.get(id);
        if (state == null || !constantTimeTextEquals(state.ownerId, owner)) {
            throw new NoSuchElementException("CPF_REMOTE_LOG_JOB_NOT_FOUND");
        }
        state.expireIfNecessary(clock.instant());
        return state;
    }

    private void removeExpiredJobs() {
        Instant now = clock.instant();
        jobs.entrySet().removeIf(entry -> {
            JobState state = entry.getValue();
            synchronized (state) {
                if (!state.expiresAt.isAfter(now)) {
                    state.expire(now);
                    return true;
                }
                return false;
            }
        });
    }

    private int activeJobCount() {
        int active = 0;
        for (JobState state : jobs.values()) {
            if ("SUBMITTED".equals(state.status) || "RUNNING".equals(state.status)) {
                active++;
            }
        }
        return active;
    }

    private void enforceOwnerRate(String ownerId, Instant now) {
        long minute = Math.floorDiv(now.getEpochSecond(), 60L);
        ownerRateWindows.entrySet().removeIf(entry -> entry.getValue().minute < minute - 1L);
        String ownerKey = hex(sha256(ownerId));
        RateWindow window = ownerRateWindows.computeIfAbsent(ownerKey, ignored -> new RateWindow(minute));
        synchronized (window) {
            if (window.minute != minute) {
                window.minute = minute;
                window.count = 0;
            }
            if (window.count >= settings.maxRequestsPerMinute()) {
                throw new RejectedExecutionException("CPF_REMOTE_LOG_OWNER_RATE_LIMIT_EXCEEDED");
            }
            window.count++;
        }
    }

    private void requireOpen() {
        if (closed.get()) {
            throw new IllegalStateException("CPF_REMOTE_LOG_JOB_MANAGER_CLOSED");
        }
    }

    private static List<String> artifactIds(List<String> source, int maximum) {
        if (source == null || source.isEmpty()) {
            throw new IllegalArgumentException("artifactIds are required");
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String id : source) {
            unique.add(required(id, "artifactId", 200));
            if (unique.size() > maximum) {
                throw new IllegalArgumentException("too many remote-log artifacts");
            }
        }
        return List.copyOf(unique);
    }

    private static void validateBundle(CpfRemoteLogBundle bundle, int requestedCount) {
        if (bundle.includedCount() < 0 || bundle.includedCount() > requestedCount) {
            throw new IllegalStateException("CPF_REMOTE_LOG_BUNDLE_INCLUDED_COUNT_INVALID");
        }
        if (bundle.failedArtifactIds().size() > requestedCount) {
            throw new IllegalStateException("CPF_REMOTE_LOG_BUNDLE_FAILURE_COUNT_INVALID");
        }
    }

    private static String required(String value, String name, int maximum) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        String normalized = value.trim();
        if (normalized.length() > maximum || normalized.indexOf('\r') >= 0 || normalized.indexOf('\n') >= 0) {
            throw new IllegalArgumentException(name + " is invalid");
        }
        return normalized;
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException unavailable) {
            throw new IllegalStateException("SHA-256 unavailable", unavailable);
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder value = new StringBuilder(bytes.length * 2);
        for (byte item : bytes) {
            value.append(Character.forDigit((item >>> 4) & 0x0f, 16));
            value.append(Character.forDigit(item & 0x0f, 16));
        }
        return value.toString();
    }

    private static boolean constantTimeTextEquals(String left, String right) {
        return MessageDigest.isEqual(
                sha256(left),
                sha256(right));
    }

    private static Instant safePlus(Instant instant, Duration duration) {
        try {
            return instant.plus(duration);
        } catch (RuntimeException overflow) {
            return Instant.MAX;
        }
    }

    private static Instant min(Instant left, Instant right) {
        return left.isBefore(right) ? left : right;
    }

    private static ThreadFactory daemonThreadFactory() {
        AtomicInteger sequence = new AtomicInteger();
        return task -> {
            Thread thread = new Thread(task, "cpf-remote-log-bundle-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    public record Settings(
            int workerCount,
            int queueCapacity,
            int maxJobs,
            int maxActiveJobs,
            int maxRequestsPerMinute,
            int maxArtifactsPerJob,
            Duration jobTtl,
            Duration grantTtl) {
        public Settings {
            if (workerCount < 1 || workerCount > 8) {
                throw new IllegalArgumentException("workerCount must be between 1 and 8");
            }
            if (queueCapacity < 1 || queueCapacity > 10_000) {
                throw new IllegalArgumentException("queueCapacity must be between 1 and 10000");
            }
            if (maxJobs < 1 || maxJobs > 100_000) {
                throw new IllegalArgumentException("maxJobs must be between 1 and 100000");
            }
            if (maxActiveJobs < 1 || maxActiveJobs > maxJobs) {
                throw new IllegalArgumentException("maxActiveJobs must be between 1 and maxJobs");
            }
            if (maxRequestsPerMinute < 1 || maxRequestsPerMinute > 10_000) {
                throw new IllegalArgumentException("maxRequestsPerMinute must be between 1 and 10000");
            }
            if (maxArtifactsPerJob < 1 || maxArtifactsPerJob > 1_000) {
                throw new IllegalArgumentException("maxArtifactsPerJob must be between 1 and 1000");
            }
            jobTtl = positiveBounded(jobTtl, Duration.ofMinutes(1), Duration.ofHours(24), "jobTtl");
            grantTtl = positiveBounded(grantTtl, Duration.ofSeconds(10), Duration.ofMinutes(30), "grantTtl");
            if (grantTtl.compareTo(jobTtl) > 0) {
                throw new IllegalArgumentException("grantTtl cannot exceed jobTtl");
            }
        }

        public static Settings safeDefaults() {
            return new Settings(1, 100, 1_000, 4, 10, 100,
                    Duration.ofMinutes(15), Duration.ofMinutes(2));
        }

        private static Duration positiveBounded(
                Duration value, Duration minimum, Duration maximum, String name) {
            Objects.requireNonNull(value, name);
            if (value.compareTo(minimum) < 0 || value.compareTo(maximum) > 0) {
                throw new IllegalArgumentException(name + " is outside safety bounds");
            }
            return value;
        }
    }

    private static final class RateWindow {
        private long minute;
        private int count;

        private RateWindow(long minute) {
            this.minute = minute;
        }
    }

    private static final class JobState {
        private final String jobId;
        private final String ownerId;
        private final List<String> artifactIds;
        private final Instant submittedAt;
        private volatile String status = "SUBMITTED";
        private volatile int includedArtifactCount;
        private volatile List<String> failedArtifactIds = List.of();
        private volatile String errorMessage;
        private volatile Instant completedAt;
        private volatile Instant expiresAt;
        private volatile CpfRemoteLogBundle bundle;
        private volatile byte[] grantHash;
        private volatile Instant grantExpiresAt;
        private volatile boolean grantUsed;

        private JobState(
                String jobId,
                String ownerId,
                List<String> artifactIds,
                Instant submittedAt,
                Instant expiresAt) {
            this.jobId = jobId;
            this.ownerId = ownerId;
            this.artifactIds = artifactIds;
            this.submittedAt = submittedAt;
            this.expiresAt = expiresAt;
        }

        private CpfRemoteLogBundleJob snapshot(Instant now) {
            synchronized (this) {
                expireIfNecessary(now);
                return new CpfRemoteLogBundleJob(
                        jobId,
                        ownerId,
                        status,
                        artifactIds.size(),
                        includedArtifactCount,
                        failedArtifactIds,
                        errorMessage,
                        submittedAt,
                        completedAt,
                        expiresAt);
            }
        }

        private boolean expireIfNecessary(Instant now) {
            if (!expiresAt.isAfter(now)) {
                expire(now);
                return true;
            }
            return false;
        }

        private void expire(Instant now) {
            status = "EXPIRED";
            bundle = null;
            completedAt = completedAt == null ? now : completedAt;
            clearGrant();
        }

        private void clearGrant() {
            if (grantHash != null) {
                java.util.Arrays.fill(grantHash, (byte) 0);
            }
            grantHash = null;
            grantExpiresAt = null;
            grantUsed = false;
        }
    }
}
