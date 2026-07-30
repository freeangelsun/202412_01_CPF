package com.cpf.batch.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 승인 Path Alias 경계에서 File Watch/Process/Transfer를 수행합니다.
 *
 * <p>단순 ENTRY_CREATE 이벤트를 완료 신호로 사용하지 않고, Stable Window, Marker, Size,
 * Checksum, 확장자, Symlink/Traversal, Claim/Fencing을 검증한 뒤 Ready 상태를 반환합니다.</p>
 */
@Component
public class ApprovedFileExecutor {
    private static final Logger log = LoggerFactory.getLogger(ApprovedFileExecutor.class);
    private static final long POLL_MILLIS = 250L;

    private final WorkerOperationalProperties properties;

    public ApprovedFileExecutor(WorkerOperationalProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public Path resolve(String alias, String relative) {
        return resolveWithinAlias(alias, relative, true);
    }

    private Path resolveDirectory(String alias, String relative) {
        return resolveWithinAlias(alias, relative, false);
    }

    private Path resolveWithinAlias(String alias, String relative, boolean enforceExtension) {
        WorkerOperationalProperties.PathAlias cfg = requireAlias(alias);
        Path root = Path.of(cfg.getRoot()).toAbsolutePath().normalize();
        Path target = root.resolve(Objects.requireNonNull(relative, "relative")).normalize();
        if (!target.startsWith(root)) {
            throw new SecurityException("Path escaped alias root");
        }
        rejectSymlinkEscape(root, target, cfg.isSymlinkAllowed());
        if (enforceExtension) {
            validateExtension(target, cfg);
        }
        return target;
    }

    public boolean sharedDurable(String alias) {
        WorkerOperationalProperties.PathAlias cfg = properties.getPathAliases().get(alias);
        return cfg != null && cfg.isSharedDurable();
    }

    /** 기존 API는 Alias 기본 정책을 적용하는 Ready 대기로 승격합니다. */
    public Path await(String alias, String relative, Duration timeout)
            throws IOException, InterruptedException, TimeoutException {
        WorkerOperationalProperties.PathAlias cfg = requireAlias(alias);
        return awaitReady(new FileWatchRequest(
                alias,
                relative,
                timeout,
                Duration.ofSeconds(Math.max(1, cfg.getStableWindowSeconds())),
                cfg.getCompletionMarkerSuffix(),
                null,
                null));
    }

    public Path awaitReady(FileWatchRequest request)
            throws IOException, InterruptedException, TimeoutException {
        Objects.requireNonNull(request, "request");
        Path target = resolve(request.alias(), request.relative());
        long deadline = System.nanoTime() + requirePositive(request.timeout(), "timeout").toNanos();
        Duration stableWindow = requirePositive(request.stableWindow(), "stableWindow");
        FileObservation previous = null;
        long stableSince = -1L;

        while (System.nanoTime() < deadline) {
            if (!Files.exists(target)) {
                Thread.sleep(POLL_MILLIS);
                continue;
            }
            validateReadyCandidate(target, request);
            FileObservation current = observe(target);
            if (current.equals(previous)) {
                if (stableSince < 0) stableSince = System.nanoTime();
                if (System.nanoTime() - stableSince >= stableWindow.toNanos()) {
                    if (request.expectedSha256() != null && !request.expectedSha256().isBlank()) {
                        FileFingerprint fingerprint = fingerprint(target);
                        if (!request.expectedSha256().equalsIgnoreCase(fingerprint.sha256())) {
                            throw new IOException("File checksum mismatch: " + request.alias() + "/" + request.relative());
                        }
                    }
                    return target;
                }
            } else {
                previous = current;
                stableSince = System.nanoTime();
            }
            Thread.sleep(POLL_MILLIS);
        }
        throw new TimeoutException("File ready timeout: " + request.alias() + "/" + request.relative());
    }

    public FileFingerprint fingerprint(Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Fingerprint target is not a regular file");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[64 * 1024];
                for (int read; (read = input.read(buffer)) >= 0;) {
                    if (read > 0) digest.update(buffer, 0, read);
                }
            }
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            return new FileFingerprint(
                    path.getFileName().toString(),
                    attributes.size(),
                    HexFormat.of().formatHex(digest.digest()),
                    attributes.lastModifiedTime().toInstant());
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    public Path transfer(
            String sourceAlias,
            String sourceRelative,
            String targetAlias,
            String targetRelative,
            boolean overwrite) throws IOException {
        Path source = resolve(sourceAlias, sourceRelative);
        Path target = resolve(targetAlias, targetRelative);
        validateSize(source, requireAlias(sourceAlias));
        Files.createDirectories(target.getParent());

        Path staging = Files.createTempFile(target.getParent(), ".cpf-transfer-", ".part");
        try {
            Files.copy(source, staging, StandardCopyOption.REPLACE_EXISTING);
            if (!fingerprint(source).sha256().equals(fingerprint(staging).sha256())) {
                throw new IOException("File transfer checksum mismatch");
            }
            CopyOption[] options = overwrite
                    ? new CopyOption[]{StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE}
                    : new CopyOption[]{StandardCopyOption.ATOMIC_MOVE};
            try {
                return Files.move(staging, target, options);
            } catch (AtomicMoveNotSupportedException unsupported) {
                return overwrite
                        ? Files.move(staging, target, StandardCopyOption.REPLACE_EXISTING)
                        : Files.move(staging, target);
            }
        } finally {
            Files.deleteIfExists(staging);
        }
    }

    /** inbox 파일을 processing alias로 원자 이동해 중복 Process를 방지합니다. */
    public Path claimForProcess(String sourceAlias, String relative, String processingAlias) throws IOException {
        Path source = resolve(sourceAlias, relative);
        Path target = resolve(processingAlias, relative);
        Files.createDirectories(target.getParent());
        try {
            return Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException unsupported) {
            return Files.move(source, target);
        }
    }

    /** 공유 저장소에서 이동이 불가능한 Provider를 위한 Lease/Fencing Claim입니다. */
    public Claim claim(String alias, String relative, String ownerId, Duration leaseDuration) throws IOException {
        Path target = resolve(alias, relative);
        if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) {
            throw new NoSuchFileException(target.toString());
        }
        String safeOwner = requireToken(ownerId, "ownerId");
        long token = System.currentTimeMillis();
        Instant expiresAt = Instant.now().plus(requirePositive(leaseDuration, "leaseDuration"));
        Path claimPath = target.resolveSibling(target.getFileName() + ".cpf-claim");
        try {
            writeClaim(claimPath, safeOwner, token, expiresAt);
        } catch (FileAlreadyExistsException existing) {
            Claim current = readClaim(claimPath, target);
            if (current.expiresAt().isAfter(Instant.now())) {
                throw new FileAlreadyExistsException("File is already claimed by " + current.ownerId());
            }
            token = Math.max(System.currentTimeMillis(), current.fencingToken() + 1);
            expiresAt = Instant.now().plus(requirePositive(leaseDuration, "leaseDuration"));
            Files.deleteIfExists(claimPath);
            writeClaim(claimPath, safeOwner, token, expiresAt);
        }
        return new Claim(target, claimPath, safeOwner, token, expiresAt);
    }

    public void release(Claim claim) throws IOException {
        Objects.requireNonNull(claim, "claim");
        Claim current = readClaim(claim.claimPath(), claim.path());
        if (!current.ownerId().equals(claim.ownerId()) || current.fencingToken() != claim.fencingToken()) {
            throw new SecurityException("Stale file claim cannot be released");
        }
        Files.deleteIfExists(claim.claimPath());
    }

    public List<Path> restartScan(String alias, String relativeDirectory) throws IOException {
        Path root = resolveDirectory(alias, relativeDirectory);
        if (!Files.exists(root)) return List.of();
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                    .filter(path -> !path.getFileName().toString().endsWith(".cpf-claim"))
                    .sorted()
                    .toList();
        }
    }

    public WatchHandle watch(String alias, Consumer<Path> consumer) throws IOException {
        WorkerOperationalProperties.PathAlias cfg = requireAlias(alias);
        return watch(new WatchRegistration(
                alias,
                ".",
                Duration.ofSeconds(Math.max(1, cfg.getStableWindowSeconds())),
                cfg.getCompletionMarkerSuffix()), consumer);
    }

    public WatchHandle watch(WatchRegistration registration, Consumer<Path> consumer) throws IOException {
        Objects.requireNonNull(registration, "registration");
        Objects.requireNonNull(consumer, "consumer");
        Path root = resolveDirectory(registration.alias(), registration.relativeDirectory());
        Files.createDirectories(root);
        WatchService service = root.getFileSystem().newWatchService();
        root.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
        Thread thread = Thread.ofVirtual().name("cpf-file-watch-" + registration.alias()).start(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key = service.take();
                    Set<Path> candidates = new LinkedHashSet<>();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.OVERFLOW) continue;
                        candidates.add(root.resolve((Path) event.context()).normalize());
                    }
                    for (Path candidate : candidates) {
                        try {
                            Path ready = awaitReady(new FileWatchRequest(
                                    registration.alias(),
                                    root.relativize(candidate).toString(),
                                    registration.stableWindow().plusSeconds(30),
                                    registration.stableWindow(),
                                    registration.markerSuffix(),
                                    null,
                                    null));
                            consumer.accept(ready);
                        } catch (Exception failure) {
                            log.warn("File candidate was not ready. alias={}, file={}, cause={}",
                                    registration.alias(), candidate.getFileName(), failure.getClass().getSimpleName());
                        }
                    }
                    if (!key.reset()) break;
                }
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            } finally {
                closeWatchService(service, registration.alias());
            }
        });
        return () -> {
            thread.interrupt();
            closeWatchService(service, registration.alias());
        };
    }

    private void validateReadyCandidate(Path target, FileWatchRequest request) throws IOException {
        WorkerOperationalProperties.PathAlias cfg = requireAlias(request.alias());
        if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("File candidate is not a regular file");
        }
        validateSize(target, cfg);
        String markerSuffix = request.markerSuffix();
        if (markerSuffix != null && !markerSuffix.isBlank()) {
            Path marker = target.resolveSibling(target.getFileName() + markerSuffix);
            if (!Files.isRegularFile(marker, LinkOption.NOFOLLOW_LINKS)) {
                throw new IOException("Completion marker not found");
            }
        }
        if (request.expectedSize() != null && Files.size(target) != request.expectedSize()) {
            throw new IOException("File size does not match expected size");
        }
    }

    private void validateSize(Path path, WorkerOperationalProperties.PathAlias cfg) throws IOException {
        long size = Files.size(path);
        if (size < 0 || size > cfg.getMaxFileSizeBytes()) {
            throw new IOException("File exceeds approved alias size limit");
        }
    }

    private static FileObservation observe(Path target) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(target, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        return new FileObservation(attributes.size(), attributes.lastModifiedTime().toMillis());
    }

    private WorkerOperationalProperties.PathAlias requireAlias(String alias) {
        WorkerOperationalProperties.PathAlias cfg = properties.getPathAliases().get(alias);
        if (cfg == null || cfg.getRoot() == null || cfg.getRoot().isBlank()) {
            throw new SecurityException("Path alias not approved: " + alias);
        }
        return cfg;
    }

    private static void rejectSymlinkEscape(Path root, Path target, boolean symlinkAllowed) {
        if (symlinkAllowed) return;
        Path current = root;
        Path relative = root.relativize(target);
        for (Path part : relative) {
            current = current.resolve(part);
            if (Files.exists(current, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(current)) {
                throw new SecurityException("Symbolic link is not allowed in approved path");
            }
        }
    }

    private static void validateExtension(Path target, WorkerOperationalProperties.PathAlias cfg) {
        if (cfg.getAllowedExtensions().isEmpty() || target.getFileName() == null) return;
        String name = target.getFileName().toString().toLowerCase(Locale.ROOT);
        boolean allowed = cfg.getAllowedExtensions().stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .map(value -> value.startsWith(".") ? value : "." + value)
                .anyMatch(name::endsWith);
        if (!allowed) throw new SecurityException("File extension is not approved for alias");
    }


    private static void writeClaim(Path claimPath, String ownerId, long fencingToken, Instant expiresAt)
            throws IOException {
        String payload = ownerId + "\n" + fencingToken + "\n" + expiresAt + "\n";
        Files.writeString(claimPath, payload, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    private static Claim readClaim(Path claimPath, Path target) throws IOException {
        List<String> lines = Files.readAllLines(claimPath, StandardCharsets.UTF_8);
        if (lines.size() < 3) throw new IOException("Invalid file claim metadata");
        return new Claim(target, claimPath, lines.get(0), Long.parseLong(lines.get(1)), Instant.parse(lines.get(2)));
    }

    private static Duration requirePositive(Duration duration, String field) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return duration;
    }

    private static String requireToken(String value, String field) {
        if (value == null || !value.matches("[A-Za-z0-9._:-]{1,120}")) {
            throw new IllegalArgumentException(field + " has invalid format");
        }
        return value;
    }

    private static void closeWatchService(WatchService service, String alias) {
        try {
            service.close();
        } catch (IOException failure) {
            log.warn("File watch close failed. alias={}, cause={}", alias, failure.getClass().getSimpleName());
        }
    }

    private record FileObservation(long size, long modifiedAtMillis) {}

    public record FileWatchRequest(
            String alias,
            String relative,
            Duration timeout,
            Duration stableWindow,
            String markerSuffix,
            Long expectedSize,
            String expectedSha256) {}

    public record FileFingerprint(String fileName, long size, String sha256, Instant modifiedAt) {}

    public record Claim(Path path, Path claimPath, String ownerId, long fencingToken, Instant expiresAt) {}

    public record WatchRegistration(
            String alias,
            String relativeDirectory,
            Duration stableWindow,
            String markerSuffix) {}

    public interface WatchHandle extends AutoCloseable {
        @Override
        void close();
    }
}
