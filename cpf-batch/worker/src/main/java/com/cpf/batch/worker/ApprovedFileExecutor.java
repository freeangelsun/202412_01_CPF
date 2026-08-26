package com.cpf.batch.worker;

import com.cpf.file.api.filetransfer.CpfCredentialReference;
import com.cpf.file.api.filetransfer.CpfFileEndpoint;
import com.cpf.file.api.filetransfer.CpfFileRequest;
import com.cpf.file.api.filetransfer.CpfFileResult;
import com.cpf.file.api.filetransfer.CpfFileTransferClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
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
    private static final int CLAIM_LOCK_STRIPES = 64;
    private static final ReentrantLock[] LOCAL_CLAIM_LOCKS = createClaimLockStripes();

    private final WorkerOperationalProperties properties;
    private volatile CpfFileTransferClient fileTransferClient;

    public ApprovedFileExecutor(WorkerOperationalProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    /** Remote Provider는 CPF 공개 File Transfer Client가 설치된 경우에만 활성화됩니다. */
    @Autowired(required = false)
    void setFileTransferClient(CpfFileTransferClient fileTransferClient) {
        this.fileTransferClient = fileTransferClient;
    }

    public Path resolve(String alias, String relative) {
        return resolveWithinAlias(alias, relative, true);
    }

    private Path resolveDirectory(String alias, String relative) {
        return resolveWithinAlias(alias, relative, false);
    }

    private Path resolveWithinAlias(String alias, String relative, boolean enforceExtension) {
        WorkerOperationalProperties.PathAlias cfg = requireAlias(alias);
        if (remote(cfg)) {
            throw new IllegalStateException("Remote alias cannot be resolved as a local Path: " + alias);
        }
        Path root = Path.of(cfg.getRoot().trim()).toAbsolutePath().normalize();
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

    /** 운영 UI와 Publish 검증에서 실제 Provider 기능을 과장하지 않도록 Capability를 제공합니다. */
    public FileProviderCapabilities capabilities(String alias) {
        WorkerOperationalProperties.PathAlias cfg = requireAlias(alias);
        boolean remote = remote(cfg);
        boolean transferInstalled = !remote || fileTransferClient != null;
        return new FileProviderCapabilities(
                alias,
                first(cfg.getProvider(), "LOCAL").toUpperCase(Locale.ROOT),
                !remote,
                !remote,
                !remote,
                !remote,
                transferInstalled,
                cfg.isSharedDurable(),
                remote && !transferInstalled
                        ? "CpfFileTransferClient가 설치되지 않았습니다."
                        : remote
                                ? "Remote Provider는 Transfer만 지원하며 Watch/Scan/Claim은 지원하지 않습니다."
                                : "AVAILABLE");
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
        return transfer(sourceAlias, sourceRelative, targetAlias, targetRelative, overwrite,
                null, null, "cpf-batch-worker");
    }

    /**
     * 승인 Alias 사이의 Local/Remote 전송을 수행합니다.
     * Host·Credential은 Runtime Parameter가 아니라 Path Alias Catalog에서만 해석합니다.
     */
    public Path transfer(
            String sourceAlias,
            String sourceRelative,
            String targetAlias,
            String targetRelative,
            boolean overwrite,
            String transactionId,
            String segmentId,
            String requestUser) throws IOException {
        WorkerOperationalProperties.PathAlias sourceCfg = requireAlias(sourceAlias);
        WorkerOperationalProperties.PathAlias targetCfg = requireAlias(targetAlias);
        boolean sourceRemote = remote(sourceCfg);
        boolean targetRemote = remote(targetCfg);
        if (!sourceRemote && !targetRemote) {
            return transferLocal(sourceAlias, sourceRelative, targetAlias, targetRelative, overwrite);
        }
        if (sourceRemote && targetRemote) {
            throw new IOException("Remote-to-remote transfer requires an explicit product transfer workflow");
        }
        CpfFileTransferClient client = fileTransferClient;
        if (client == null) {
            throw new IOException("CpfFileTransferClient capability is not installed for remote alias");
        }

        WorkerOperationalProperties.PathAlias remoteCfg = sourceRemote ? sourceCfg : targetCfg;
        CpfFileEndpoint endpoint = endpoint(sourceRemote ? sourceAlias : targetAlias, remoteCfg);
        Path localPath = sourceRemote
                ? resolve(targetAlias, targetRelative)
                : resolve(sourceAlias, sourceRelative);
        if (sourceRemote) {
            Files.createDirectories(localPath.toAbsolutePath().normalize().getParent());
            if (Files.exists(localPath) && !overwrite) {
                throw new FileAlreadyExistsException(localPath.toString());
            }
        } else {
            validateSize(localPath, sourceCfg);
        }

        FileFingerprint localFingerprint = sourceRemote || !Files.exists(localPath)
                ? null : fingerprint(localPath);
        String operation = sourceRemote ? "DOWNLOAD" : "UPLOAD";
        String remotePath = remotePath(remoteCfg, sourceRemote ? sourceRelative : targetRelative);
        Map<String, String> attributes = new LinkedHashMap<>(remoteCfg.getAttributes());
        attributes.put("sourceAlias", sourceAlias);
        attributes.put("targetAlias", targetAlias);
        attributes.put("requestUser", requireToken(requestUser, "requestUser"));
        attributes.put("overwrite", Boolean.toString(overwrite));
        CpfFileRequest request = new CpfFileRequest(
                endpoint.endpointCode(), operation,
                localPath.toString(), remotePath,
                localFingerprint == null ? null : localFingerprint.sha256(),
                localFingerprint == null ? 0L : localFingerprint.size(), attributes);
        CpfFileResult result = client.execute(endpoint, request);
        requireTransferSuccess(result, endpoint.endpointCode(), operation);

        if (sourceRemote) {
            if (!Files.isRegularFile(localPath, LinkOption.NOFOLLOW_LINKS)) {
                throw new IOException("Remote download did not create the approved local file");
            }
            validateSize(localPath, targetCfg);
            FileFingerprint downloaded = fingerprint(localPath);
            if (result.checksum() != null && !result.checksum().isBlank()
                    && !result.checksum().equalsIgnoreCase(downloaded.sha256())) {
                Files.deleteIfExists(localPath);
                throw new IOException("Remote download checksum mismatch");
            }
            return localPath;
        }
        FileFingerprint uploadFingerprint = java.util.Objects.requireNonNull(localFingerprint, "local upload fingerprint");
        if (result.checksum() != null && !result.checksum().isBlank()
                && !result.checksum().equalsIgnoreCase(uploadFingerprint.sha256())) {
            throw new IOException("Remote upload checksum mismatch");
        }
        return localPath;
    }

    private Path transferLocal(
            String sourceAlias, String sourceRelative, String targetAlias,
            String targetRelative, boolean overwrite) throws IOException {
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
        Duration safeLease = requirePositive(leaseDuration, "leaseDuration");
        Path claimPath = target.resolveSibling(target.getFileName() + ".cpf-claim");
        Path lockPath = target.resolveSibling(target.getFileName() + ".cpf-claim.lock");
        Files.createDirectories(lockPath.getParent());

        return withClaimLock(lockPath, channel -> {
            Instant now = Instant.now();
            long previousToken = readFenceToken(channel);
            if (Files.exists(claimPath, LinkOption.NOFOLLOW_LINKS)) {
                if (Files.isSymbolicLink(claimPath)) {
                    throw new SecurityException("BATCH_FILE_CLAIM_FENCE_CONFLICT: claim metadata is symbolic link");
                }
                Claim current = readClaim(claimPath, target);
                previousToken = Math.max(previousToken, current.fencingToken());
                if (current.expiresAt().isAfter(now)) {
                    throw new FileAlreadyExistsException("File is already claimed by " + current.ownerId());
                }
            }

            long token = nextFenceToken(previousToken);
            Instant expiresAt = now.plus(safeLease);
            writeFenceToken(channel, token);
            writeClaimAtomically(claimPath, safeOwner, token, expiresAt);
            return new Claim(target, claimPath, safeOwner, token, expiresAt);
        });
    }

    public void release(Claim claim) throws IOException {
        Objects.requireNonNull(claim, "claim");
        Path lockPath = claim.path().resolveSibling(claim.path().getFileName() + ".cpf-claim.lock");
        withClaimLock(lockPath, channel -> {
            if (!Files.exists(claim.claimPath(), LinkOption.NOFOLLOW_LINKS)) {
                throw new SecurityException("BATCH_FILE_CLAIM_FENCE_CONFLICT: claim metadata is missing");
            }
            Claim current = readClaim(claim.claimPath(), claim.path());
            if (!current.ownerId().equals(claim.ownerId()) || current.fencingToken() != claim.fencingToken()) {
                throw new SecurityException("BATCH_FILE_CLAIM_FENCE_CONFLICT: stale file claim cannot be released");
            }
            Files.delete(claim.claimPath());
            return null;
        });
    }

    public List<Path> restartScan(String alias, String relativeDirectory) throws IOException {
        Path root = resolveDirectory(alias, relativeDirectory);
        if (!Files.exists(root)) return List.of();
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                    .filter(path -> !path.getFileName().toString().endsWith(".cpf-claim"))
                    .filter(path -> !path.getFileName().toString().endsWith(".cpf-claim.lock"))
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

    private static boolean remote(WorkerOperationalProperties.PathAlias cfg) {
        String provider = Objects.toString(cfg.getProvider(), "LOCAL").trim().toUpperCase(Locale.ROOT);
        return !Set.of("LOCAL", "SHARED_FS", "NFS", "SMB").contains(provider);
    }

    private static CpfFileEndpoint endpoint(String alias, WorkerOperationalProperties.PathAlias cfg) {
        String endpointCode = first(cfg.getEndpointCode(), alias);
        String protocol = first(cfg.getProtocol(), cfg.getProvider()).toUpperCase(Locale.ROOT);
        String host = requireToken(cfg.getHost(), "pathAliases." + alias + ".host");
        int port = cfg.getPort();
        if (port <= 0 || port > 65535) {
            port = switch (protocol) {
                case "SFTP", "SCP", "SSH" -> 22;
                case "FTP" -> 21;
                case "FTPS" -> 990;
                default -> throw new IllegalArgumentException("Unsupported remote file protocol: " + protocol);
            };
        }
        String credentialId = requireToken(cfg.getCredentialId(),
                "pathAliases." + alias + ".credentialId");
        CpfCredentialReference credential = new CpfCredentialReference(
                first(cfg.getCredentialScope(), "default"), credentialId,
                first(cfg.getCredentialVersion(), "latest"), credentialId);
        return new CpfFileEndpoint(endpointCode, protocol, host, port,
                first(cfg.getRemoteBasePath(), "/"), credential,
                Duration.ofSeconds(Math.max(1, cfg.getTimeoutSeconds())), cfg.getAttributes());
    }

    private static String remotePath(WorkerOperationalProperties.PathAlias cfg, String relative) {
        String cleanRelative = Objects.requireNonNull(relative, "relative").replace('\\', '/').trim();
        if (cleanRelative.isBlank() || cleanRelative.startsWith("/")
                || Arrays.stream(cleanRelative.split("/")).anyMatch(".."::equals)) {
            throw new SecurityException("Remote path must be a relative path inside the approved base");
        }
        String base = first(cfg.getRemoteBasePath(), "/").replace('\\', '/');
        while (base.endsWith("/") && base.length() > 1) base = base.substring(0, base.length() - 1);
        return ("/".equals(base) ? "" : base) + "/" + cleanRelative;
    }

    private static void requireTransferSuccess(CpfFileResult result, String endpointCode, String operation)
            throws IOException {
        if (result == null) throw new IOException("Remote file transfer returned no result");
        String status = Objects.toString(result.status(), "UNKNOWN").toUpperCase(Locale.ROOT);
        if (!Set.of("SUCCESS", "COMPLETED", "UPLOADED", "DOWNLOADED").contains(status)) {
            throw new IOException("Remote file transfer " + operation + " failed: endpoint="
                    + endpointCode + ", status=" + status + ", detail="
                    + Objects.toString(result.detail(), ""));
        }
    }

    private static String first(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
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
        String safeAlias = requireToken(alias, "alias");
        WorkerOperationalProperties.PathAlias cfg = properties.getPathAliases().get(safeAlias);
        if (cfg == null) {
            throw new SecurityException("Path alias not approved: " + safeAlias);
        }
        if (remote(cfg)) {
            requireToken(cfg.getHost(), "pathAliases." + safeAlias + ".host");
            requireToken(cfg.getCredentialId(), "pathAliases." + safeAlias + ".credentialId");
        } else if (cfg.getRoot() == null || cfg.getRoot().isBlank()) {
            throw new SecurityException("Path alias root is not configured: " + safeAlias);
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


    private static ReentrantLock[] createClaimLockStripes() {
        ReentrantLock[] locks = new ReentrantLock[CLAIM_LOCK_STRIPES];
        Arrays.setAll(locks, ignored -> new ReentrantLock());
        return locks;
    }

    private static <T> T withClaimLock(Path lockPath, ClaimIoOperation<T> operation) throws IOException {
        Path normalized = lockPath.toAbsolutePath().normalize();
        ReentrantLock localLock = LOCAL_CLAIM_LOCKS[Math.floorMod(normalized.hashCode(), CLAIM_LOCK_STRIPES)];
        localLock.lock();
        try {
            if (Files.exists(normalized, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(normalized)) {
                throw new SecurityException("BATCH_FILE_CLAIM_FENCE_CONFLICT: claim lock is symbolic link");
            }
            Files.createDirectories(normalized.getParent());
            try (FileChannel channel = FileChannel.open(normalized,
                    StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
                 FileLock _ = channel.lock()) {
                return operation.run(channel);
            }
        } finally {
            localLock.unlock();
        }
    }

    @FunctionalInterface
    private interface ClaimIoOperation<T> {
        T run(FileChannel channel) throws IOException;
    }

    private static void writeClaimAtomically(
            Path claimPath, String ownerId, long fencingToken, Instant expiresAt) throws IOException {
        String payload = ownerId + "\n" + fencingToken + "\n" + expiresAt + "\n";
        Path staging = Files.createTempFile(claimPath.getParent(), claimPath.getFileName() + ".", ".tmp");
        try {
            Files.writeString(staging, payload, StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            try {
                Files.move(staging, claimPath,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(staging, claimPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(staging);
        }
    }

    private static long readFenceToken(FileChannel channel) throws IOException {
        channel.position(0L);
        long size = channel.size();
        if (size == 0L) return 0L;
        if (size > 64L) throw new IOException("BATCH_FILE_CLAIM_FENCE_CONFLICT: invalid fence ledger");
        ByteBuffer buffer = ByteBuffer.allocate((int) size);
        while (buffer.hasRemaining() && channel.read(buffer) >= 0) {
            // Continue until the durable token has been read.
        }
        String value = StandardCharsets.UTF_8.decode((ByteBuffer) buffer.flip()).toString().trim();
        if (value.isEmpty()) return 0L;
        try {
            long token = Long.parseLong(value);
            if (token < 0L) throw new NumberFormatException("negative");
            return token;
        } catch (NumberFormatException malformed) {
            throw new IOException("BATCH_FILE_CLAIM_FENCE_CONFLICT: invalid fence token", malformed);
        }
    }

    private static void writeFenceToken(FileChannel channel, long token) throws IOException {
        byte[] payload = (Long.toString(token) + "\n").getBytes(StandardCharsets.UTF_8);
        channel.truncate(0L);
        channel.position(0L);
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        while (buffer.hasRemaining()) channel.write(buffer);
        channel.force(true);
    }

    private static long nextFenceToken(long previousToken) throws IOException {
        if (previousToken == Long.MAX_VALUE) {
            throw new IOException("BATCH_FILE_CLAIM_FENCE_CONFLICT: fencing token exhausted");
        }
        return Math.max(System.currentTimeMillis(), previousToken + 1L);
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

    public record FileProviderCapabilities(
            String alias,
            String provider,
            boolean resolveSupported,
            boolean watchSupported,
            boolean restartScanSupported,
            boolean claimSupported,
            boolean transferSupported,
            boolean sharedDurable,
            String detail) {}

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
