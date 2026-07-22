package cpf.pfw.common.remotelog;

import org.springframework.core.env.Environment;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 단일 ADM 인스턴스에서 비동기 로그 ZIP 작업과 1회성 다운로드 token을 관리하는 기본 adapter입니다.
 *
 * <p>운영 cluster에서는 같은 port를 공유 저장소·분산 rate limit·작업 queue adapter로 교체합니다.</p>
 */
public final class InMemoryCpfRemoteLogBundleJobAdapter implements CpfRemoteLogBundleJobPort, AutoCloseable {
    private static final int TOKEN_BYTES = 32;

    private final CpfRemoteLogArtifactPort artifactPort;
    private final int maxRequestsPerMinute;
    private final int maxActiveJobs;
    private final Duration tokenTtl;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();
    private final ExecutorService executor;
    private final ConcurrentMap<String, JobState> jobs = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, GrantState> grants = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Deque<Instant>> ownerRequests = new ConcurrentHashMap<>();
    private final AtomicInteger activeJobs = new AtomicInteger();

    public InMemoryCpfRemoteLogBundleJobAdapter(CpfRemoteLogArtifactPort artifactPort, Environment environment) {
        this(
                artifactPort,
                environment.getProperty("cpf.remote-log.bundle-job.max-requests-per-minute", Integer.class, 10),
                environment.getProperty("cpf.remote-log.bundle-job.max-active-jobs", Integer.class, 4),
                Duration.ofSeconds(environment.getProperty(
                        "cpf.remote-log.bundle-job.download-token-ttl-seconds", Long.class, 300L)),
                Clock.systemUTC());
    }

    InMemoryCpfRemoteLogBundleJobAdapter(
            CpfRemoteLogArtifactPort artifactPort,
            int maxRequestsPerMinute,
            int maxActiveJobs,
            Duration tokenTtl,
            Clock clock) {
        this.artifactPort = artifactPort;
        this.maxRequestsPerMinute = Math.max(1, Math.min(maxRequestsPerMinute, 1_000));
        this.maxActiveJobs = Math.max(1, Math.min(maxActiveJobs, 32));
        this.tokenTtl = tokenTtl.isNegative() || tokenTtl.isZero() ? Duration.ofMinutes(5) : tokenTtl;
        this.clock = clock;
        this.executor = Executors.newFixedThreadPool(Math.min(this.maxActiveJobs, 8));
    }

    @Override
    public CpfRemoteLogBundleJob submit(String ownerId, List<String> artifactIds) {
        String owner = required(ownerId, "ownerId");
        if (artifactIds == null || artifactIds.isEmpty()) {
            throw new IllegalArgumentException("비동기 로그 묶음 artifact ID는 한 건 이상이어야 합니다.");
        }
        checkRateLimit(owner);
        int active = activeJobs.incrementAndGet();
        if (active > maxActiveJobs) {
            activeJobs.decrementAndGet();
            throw new IllegalStateException("동시에 처리할 수 있는 로그 묶음 작업 수를 초과했습니다.");
        }
        cleanupExpired();
        String jobId = UUID.randomUUID().toString();
        Instant now = Instant.now(clock);
        JobState state = new JobState(
                jobId, owner, List.copyOf(artifactIds), "QUEUED", null, null, now, null, null);
        jobs.put(jobId, state);
        try {
            executor.submit(() -> execute(state));
        } catch (RuntimeException ex) {
            jobs.remove(jobId);
            activeJobs.decrementAndGet();
            throw new IllegalStateException("로그 묶음 작업을 실행 queue에 등록하지 못했습니다.", ex);
        }
        return state.view();
    }

    @Override
    public CpfRemoteLogBundleJob find(String jobId, String ownerId) {
        return requireOwnedJob(jobId, ownerId).view();
    }

    @Override
    public CpfRemoteLogDownloadGrant issueDownloadGrant(String jobId, String ownerId) {
        JobState state = requireOwnedJob(jobId, ownerId);
        if (!"COMPLETED".equals(state.status()) || state.bundle() == null) {
            throw new IllegalStateException("완료된 로그 묶음 작업만 다운로드 token을 발급할 수 있습니다.");
        }
        Instant now = Instant.now(clock);
        if (state.bundle().expiresAt().isBefore(now)) {
            throw new IllegalStateException("로그 묶음 파일이 만료되었습니다.");
        }
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = min(now.plus(tokenTtl), state.bundle().expiresAt());
        grants.put(hash(token), new GrantState(state.jobId(), state.ownerId(), expiresAt));
        return new CpfRemoteLogDownloadGrant(state.jobId(), token, expiresAt);
    }

    @Override
    public CpfRemoteLogBundle resolveDownload(String jobId, String ownerId, String token) {
        JobState state = requireOwnedJob(jobId, ownerId);
        String tokenHash = hash(required(token, "token"));
        GrantState grant = grants.remove(tokenHash);
        Instant now = Instant.now(clock);
        if (grant == null
                || !grant.jobId().equals(state.jobId())
                || !grant.ownerId().equals(state.ownerId())
                || grant.expiresAt().isBefore(now)) {
            throw new IllegalArgumentException("로그 묶음 다운로드 token이 유효하지 않거나 이미 사용되었습니다.");
        }
        CpfRemoteLogBundle bundle = state.bundle();
        if (bundle == null || bundle.expiresAt().isBefore(now)
                || !Files.isRegularFile(bundle.path(), LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(bundle.path())) {
            throw new IllegalStateException("다운로드할 로그 묶음 파일이 없거나 만료되었습니다.");
        }
        return bundle;
    }

    @Override
    public Map<String, Object> diagnostics() {
        cleanupExpired();
        long completed = jobs.values().stream().filter(state -> "COMPLETED".equals(state.status())).count();
        long failed = jobs.values().stream().filter(state -> "FAILED".equals(state.status())).count();
        return Map.of(
                "adapter", getClass().getSimpleName(),
                "activeJobs", activeJobs.get(),
                "trackedJobs", jobs.size(),
                "completedJobs", completed,
                "failedJobs", failed,
                "activeDownloadGrants", grants.size(),
                "maxRequestsPerMinute", maxRequestsPerMinute,
                "maxActiveJobs", maxActiveJobs,
                "downloadTokenTtlSeconds", tokenTtl.toSeconds());
    }

    private void execute(JobState queued) {
        JobState running = queued.withStatus("RUNNING");
        jobs.put(queued.jobId(), running);
        try {
            CpfRemoteLogBundle bundle = artifactPort.createBundle(queued.artifactIds());
            jobs.put(queued.jobId(), running.completed(bundle, Instant.now(clock)));
        } catch (RuntimeException ex) {
            jobs.put(queued.jobId(), running.failed(rootMessage(ex), Instant.now(clock)));
        } finally {
            activeJobs.decrementAndGet();
        }
    }

    private void checkRateLimit(String ownerId) {
        Instant now = Instant.now(clock);
        Deque<Instant> requests = ownerRequests.computeIfAbsent(ownerId, ignored -> new ArrayDeque<>());
        synchronized (requests) {
            Instant threshold = now.minusSeconds(60);
            while (!requests.isEmpty() && requests.peekFirst().isBefore(threshold)) {
                requests.removeFirst();
            }
            if (requests.size() >= maxRequestsPerMinute) {
                throw new IllegalStateException("소유자별 로그 묶음 요청 한도를 초과했습니다.");
            }
            requests.addLast(now);
        }
    }

    private JobState requireOwnedJob(String jobId, String ownerId) {
        JobState state = jobs.get(required(jobId, "jobId"));
        String owner = required(ownerId, "ownerId");
        if (state == null || !state.ownerId().equals(owner)) {
            throw new IllegalArgumentException("로그 묶음 작업을 찾을 수 없습니다.");
        }
        return state;
    }

    private void cleanupExpired() {
        Instant now = Instant.now(clock);
        grants.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
        jobs.entrySet().removeIf(entry -> {
            JobState state = entry.getValue();
            return state.expiresAt() != null
                    && ("COMPLETED".equals(state.status()) || "FAILED".equals(state.status()))
                    && state.expiresAt().isBefore(now);
        });
    }

    private Instant min(Instant left, Instant right) {
        return left.isBefore(right) ? left : right;
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " 값은 필수입니다.");
        }
        return value.trim();
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", ex);
        }
    }

    private String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }

    private record GrantState(String jobId, String ownerId, Instant expiresAt) {
    }

    private record JobState(
            String jobId,
            String ownerId,
            List<String> artifactIds,
            String status,
            CpfRemoteLogBundle bundle,
            String errorMessage,
            Instant submittedAt,
            Instant completedAt,
            Instant expiresAt) {

        private JobState withStatus(String value) {
            return new JobState(
                    jobId, ownerId, artifactIds, value, bundle, errorMessage,
                    submittedAt, completedAt, expiresAt);
        }

        private JobState completed(CpfRemoteLogBundle value, Instant at) {
            return new JobState(
                    jobId, ownerId, artifactIds, "COMPLETED", value, null,
                    submittedAt, at, value.expiresAt());
        }

        private JobState failed(String error, Instant at) {
            return new JobState(
                    jobId, ownerId, artifactIds, "FAILED", null, error,
                    submittedAt, at, at.plus(Duration.ofHours(1)));
        }

        private CpfRemoteLogBundleJob view() {
            return new CpfRemoteLogBundleJob(
                    jobId,
                    ownerId,
                    status,
                    artifactIds.size(),
                    bundle == null ? 0 : bundle.includedCount(),
                    bundle == null ? List.of() : bundle.failedArtifactIds(),
                    errorMessage,
                    submittedAt,
                    completedAt,
                    expiresAt);
        }
    }
}
