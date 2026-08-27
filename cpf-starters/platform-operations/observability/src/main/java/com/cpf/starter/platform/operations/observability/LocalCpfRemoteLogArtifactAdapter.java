package com.cpf.starter.platform.operations.observability;

import com.cpf.platform.operations.observability.api.remotelog.CpfRemoteLogArtifact;
import com.cpf.platform.operations.observability.api.remotelog.CpfRemoteLogArtifactPort;
import com.cpf.platform.operations.observability.api.remotelog.CpfRemoteLogArtifactSearch;
import com.cpf.platform.operations.observability.api.remotelog.CpfRemoteLogBundle;
import com.cpf.platform.operations.observability.api.remotelog.CpfRemoteLogPreview;
import com.cpf.security.api.CpfSensitiveData;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Local filesystem implementation of the remote-log artifact port.
 *
 * <p>Every operation revalidates the canonical log root and rejects symbolic-link traversal.
 * Search, preview, download and bundle creation are bounded so an ADM request cannot turn into
 * an unbounded filesystem scan or decompression task.</p>
 */
public final class LocalCpfRemoteLogArtifactAdapter implements CpfRemoteLogArtifactPort {
    private static final String BUNDLE_DIRECTORY = ".cpf-bundles";

    private final Path root;
    private final Path bundleRoot;
    private final Clock clock;
    private final Settings settings;
    private final AtomicLong searchFailures = new AtomicLong();
    private final AtomicLong previewFailures = new AtomicLong();
    private final AtomicLong bundleFailures = new AtomicLong();
    private final AtomicLong bundleCleanupFailures = new AtomicLong();
    private final AtomicLong expiredBundlesDeleted = new AtomicLong();

    public LocalCpfRemoteLogArtifactAdapter(Path root, Clock clock, Settings settings) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.root = prepareRoot(root);
        this.bundleRoot = this.root.resolve(BUNDLE_DIRECTORY);
        prepareBundleRoot();
    }

    @Override
    public List<CpfRemoteLogArtifact> search(CpfRemoteLogArtifactSearch search) {
        Objects.requireNonNull(search, "search");
        try {
            List<CpfRemoteLogArtifact> result = new ArrayList<>();
            int scanned = 0;
            try (var paths = Files.walk(root, settings.maximumDepth())) {
                var iterator = paths.iterator();
                while (iterator.hasNext() && result.size() < search.limit()) {
                    Path candidate = iterator.next();
                    if (++scanned > settings.maximumScannedFiles()) {
                        throw new IllegalStateException("remote log scan capacity exceeded");
                    }
                    if (candidate.startsWith(bundleRoot) || !isSafeRegularFile(candidate)) continue;
                    CpfRemoteLogArtifact artifact = describe(candidate);
                    if (matches(artifact, candidate, search)) result.add(artifact);
                }
            }
            result.sort(Comparator.comparing((CpfRemoteLogArtifact value) -> value.modifiedAt()).reversed()
                    .thenComparing(value -> value.relativePath()));
            return List.copyOf(result);
        } catch (IOException | RuntimeException failure) {
            searchFailures.incrementAndGet();
            if (failure instanceof IllegalArgumentException illegal) throw illegal;
            throw new IllegalStateException("remote log search failed: " + failure.getClass().getSimpleName());
        }
    }

    @Override
    public CpfRemoteLogPreview preview(String artifactId, int lastLines, String keyword) {
        String id = required(artifactId, "artifactId", 200);
        if (lastLines < 1 || lastLines > settings.maximumPreviewLines()) {
            throw new IllegalArgumentException("lastLines exceeds the configured preview limit");
        }
        String normalizedKeyword = optionalKeyword(keyword);
        try {
            LocatedArtifact located = locate(id);
            PreviewResult preview = readPreview(located.path(), lastLines, normalizedKeyword);
            return new CpfRemoteLogPreview(
                    located.artifact(), preview.lines(), preview.lines().size(), preview.truncated(), normalizedKeyword);
        } catch (IOException | RuntimeException failure) {
            previewFailures.incrementAndGet();
            if (failure instanceof RuntimeException runtime) throw runtime;
            throw new IllegalStateException("remote log preview failed: " + failure.getClass().getSimpleName());
        }
    }

    @Override
    public Path resolveDownload(String artifactId) {
        LocatedArtifact located = locate(required(artifactId, "artifactId", 200));
        CpfRemoteLogArtifact artifact = located.artifact();
        if (!artifact.downloadable() || artifact.active()) {
            throw new SecurityException("artifact is not downloadable");
        }
        if (artifact.retentionExpiresAt() != null && !artifact.retentionExpiresAt().isAfter(clock.instant())) {
            throw new NoSuchElementException("artifact is outside retention");
        }
        return located.path();
    }

    @Override
    public CpfRemoteLogBundle createBundle(List<String> artifactIds) {
        List<String> requested = normalizeArtifactIds(artifactIds);
        String bundleId = UUID.randomUUID().toString();
        String fileName = "cpf-log-bundle-" + bundleId + ".zip";
        Path target = bundleRoot.resolve(fileName);
        Path temporary = bundleRoot.resolve(fileName + ".tmp");
        List<String> failures = new ArrayList<>();
        int included = 0;
        long totalBytes = 0L;
        try {
            if (!cleanupExpiredBundlesSafely()) {
                throw new SecurityException("bundle cleanup failed");
            }
            ensureBundleRootSafe();
            try (ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(
                    Files.newOutputStream(temporary, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)),
                    StandardCharsets.UTF_8)) {
                StringBuilder manifest = new StringBuilder();
                for (String artifactId : requested) {
                    LocatedArtifact located;
                    long nextTotal;
                    try {
                        located = locate(artifactId);
                        if (located.artifact().active()
                                || (located.artifact().retentionExpiresAt() != null
                                && !located.artifact().retentionExpiresAt().isAfter(clock.instant()))
                                || located.artifact().size() > settings.maximumDownloadBytes()) {
                            failures.add(artifactId);
                            continue;
                        }
                        nextTotal = totalBytes;
                    } catch (NoSuchElementException | SecurityException | ArithmeticException failure) {
                        failures.add(artifactId);
                        continue;
                    }
                    String entryName = "logs/" + located.artifact().relativePath() + ".masked.log";
                    ZipEntry entry = new ZipEntry(entryName);
                    entry.setTime(located.artifact().modifiedAt().toEpochMilli());
                    zip.putNextEntry(entry);
                    // Bundle content is always a sanitized derivative, even when the source claims to be masked.
                    CopyResult copied = copyMaskedAndDigest(
                            located.path(), located.artifact().compressed(), zip,
                            settings.maximumDownloadBytes(), settings.maximumBundleBytes() - totalBytes,
                            settings.maximumBundleBytes(), settings.maximumRawLineCharacters());
                    zip.closeEntry();
                    manifest.append(copied.checksum()).append("  ").append(entryName).append('\n');
                    nextTotal = Math.addExact(totalBytes, copied.outputBytes());
                    totalBytes = nextTotal;
                    included++;
                }
                ZipEntry manifestEntry = new ZipEntry("manifest.sha256");
                zip.putNextEntry(manifestEntry);
                zip.write(manifest.toString().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
            moveAtomically(temporary, target);
            Files.setLastModifiedTime(target, FileTime.from(clock.instant()));
            Path relative = root.relativize(target);
            return new CpfRemoteLogBundle(
                    bundleId, fileName, relative, included, failures,
                    clock.instant().plus(settings.bundleTimeToLive()));
        } catch (IOException | RuntimeException failure) {
            bundleFailures.incrementAndGet();
            deleteQuietly(temporary);
            deleteQuietly(target);
            throw new IllegalStateException("remote log bundle failed: " + failure.getClass().getSimpleName());
        }
    }

    @Override
    public Map<String, Object> diagnostics() {
        boolean rootHealthy = Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS);
        boolean bundleHealthy = rootHealthy && cleanupExpiredBundlesSafely();
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("adapter", getClass().getSimpleName());
        values.put("rootHash", sha256(root.toString()));
        values.put("maximumScannedFiles", settings.maximumScannedFiles());
        values.put("maximumPreviewBytes", settings.maximumPreviewBytes());
        values.put("maximumDownloadBytes", settings.maximumDownloadBytes());
        values.put("searchFailures", searchFailures.get());
        values.put("previewFailures", previewFailures.get());
        values.put("bundleFailures", bundleFailures.get());
        values.put("bundleCleanupFailures", bundleCleanupFailures.get());
        values.put("expiredBundlesDeleted", expiredBundlesDeleted.get());
        values.put("state", rootHealthy && bundleHealthy ? "UP" : "DOWN");
        return Map.copyOf(values);
    }

    private boolean matches(CpfRemoteLogArtifact artifact, Path path, CpfRemoteLogArtifactSearch search)
            throws IOException {
        if (!matchesText(artifact.environment(), search.environment())) return false;
        if (!matchesText(artifact.module(), search.module())) return false;
        if (!matchesText(artifact.service(), search.service())) return false;
        if (!matchesText(artifact.instance(), search.instance())) return false;
        if (!matchesText(artifact.logType(), search.logType())) return false;
        if (search.fileName() != null
                && !artifact.fileName().toLowerCase(Locale.ROOT)
                .contains(search.fileName().toLowerCase(Locale.ROOT))) return false;
        if (search.modifiedFrom() != null && artifact.modifiedAt().isBefore(search.modifiedFrom())) return false;
        if (search.modifiedTo() != null && artifact.modifiedAt().isAfter(search.modifiedTo())) return false;
        if (search.minSize() != null && artifact.size() < search.minSize()) return false;
        if (search.maxSize() != null && artifact.size() > search.maxSize()) return false;
        if (search.compressed() != null && artifact.compressed() != search.compressed()) return false;
        if (search.active() != null && artifact.active() != search.active()) return false;
        return search.contentIdentifiers().isEmpty() || containsIdentifier(path, artifact, search.contentIdentifiers());
    }

    private boolean containsIdentifier(Path path, CpfRemoteLogArtifact artifact, List<String> identifiers)
            throws IOException {
        if (artifact.size() > settings.maximumSearchBytes()) return false;
        if (artifact.compressed() && !artifact.fileName().toLowerCase(Locale.ROOT).endsWith(".gz")) return false;
        try (InputStream stream = openContent(path, artifact.compressed())) {
            byte[] buffer = new byte[8_192];
            StringBuilder text = new StringBuilder();
            long bytes = 0L;
            int read;
            while ((read = stream.read(buffer)) >= 0) {
                bytes += read;
                if (bytes > settings.maximumSearchBytes()) return false;
                text.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
            }
            String content = text.toString();
            return identifiers.stream().anyMatch(content::contains);
        }
    }

    private CpfRemoteLogArtifact describe(Path candidate) throws IOException {
        Path safe = requireSafeFile(candidate);
        BasicFileAttributes attributes = Files.readAttributes(
                safe, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        Path relative = root.relativize(safe);
        String relativeText = relative.toString().replace('\\', '/');
        String fileName = safe.getFileName().toString();
        Instant modifiedAt = attributes.lastModifiedTime().toInstant();
        boolean compressed = fileName.toLowerCase(Locale.ROOT).endsWith(".gz");
        boolean active = !compressed && modifiedAt.isAfter(clock.instant().minus(settings.activeWindow()));
        Instant retentionExpiresAt = modifiedAt.plus(settings.retention());
        boolean retained = retentionExpiresAt.isAfter(clock.instant());
        boolean downloadEligible = !active && retained && attributes.size() <= settings.maximumDownloadBytes();
        boolean downloadable = downloadEligible && settings.sourceAlreadyMasked();
        String checksum = downloadable ? digestFile(safe, settings.maximumDownloadBytes()) : null;
        String maskingPolicy = settings.sourceAlreadyMasked()
                ? "SOURCE_DECLARED_MASKED_V3" : "PREVIEW_AND_BUNDLE_MASKED_V3";
        return new CpfRemoteLogArtifact(
                sha256(relativeText), settings.environment(), settings.module(), settings.service(),
                settings.instance(), inferLogType(fileName), fileName, relativeText, attributes.size(),
                modifiedAt, compressed, checksum, active, maskingPolicy, downloadable,
                retentionExpiresAt, "ONLINE");
    }

    private LocatedArtifact locate(String artifactId) {
        try {
            int scanned = 0;
            try (var paths = Files.walk(root, settings.maximumDepth())) {
                var iterator = paths.iterator();
                while (iterator.hasNext()) {
                    Path candidate = iterator.next();
                    if (++scanned > settings.maximumScannedFiles()) {
                        throw new IllegalStateException("remote log scan capacity exceeded");
                    }
                    if (candidate.startsWith(bundleRoot) || !isSafeRegularFile(candidate)) continue;
                    String relative = root.relativize(candidate).toString().replace('\\', '/');
                    if (MessageDigest.isEqual(
                            sha256(relative).getBytes(StandardCharsets.US_ASCII),
                            artifactId.getBytes(StandardCharsets.US_ASCII))) {
                        return new LocatedArtifact(requireSafeFile(candidate), describe(candidate));
                    }
                }
            }
        } catch (IOException failure) {
            throw new IllegalStateException("artifact lookup failed: " + failure.getClass().getSimpleName());
        }
        throw new NoSuchElementException("artifact not found");
    }

    private PreviewResult readPreview(Path path, int maximumLines, String keyword) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".gz")) {
            if (attributes.size() > settings.maximumPreviewBytes()) {
                throw new IllegalArgumentException("compressed artifact exceeds preview byte limit");
            }
            try (InputStream stream = new GZIPInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
                return readLines(stream, maximumLines, keyword, false);
            }
        }
        if (attributes.size() <= settings.maximumPreviewBytes()) {
            try (InputStream stream = new BufferedInputStream(Files.newInputStream(path))) {
                return readLines(stream, maximumLines, keyword, false);
            }
        }
        long offset = attributes.size() - settings.maximumPreviewBytes();
        byte[] bytes = new byte[Math.toIntExact(settings.maximumPreviewBytes())];
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            channel.position(offset);
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            while (buffer.hasRemaining() && channel.read(buffer) >= 0) { }
        }
        int firstNewline = -1;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == '\n') { firstNewline = i; break; }
        }
        int start = firstNewline < 0 ? bytes.length : firstNewline + 1;
        try (InputStream stream = new java.io.ByteArrayInputStream(bytes, start, bytes.length - start)) {
            return readLines(stream, maximumLines, keyword, true);
        }
    }

    private PreviewResult readLines(InputStream input, int maximumLines, String keyword, boolean sourceTruncated)
            throws IOException {
        Deque<String> tail = new ArrayDeque<>(maximumLines);
        boolean truncated = sourceTruncated;
        long decodedCharacters = 0L;
        StringBuilder current = new StringBuilder();
        try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            char[] buffer = new char[4_096];
            int read;
            while ((read = reader.read(buffer)) >= 0) {
                decodedCharacters += read;
                if (decodedCharacters > settings.maximumPreviewDecodedCharacters()) {
                    truncated = true;
                    break;
                }
                for (int i = 0; i < read; i++) {
                    char character = buffer[i];
                    if (character == '\n') {
                        truncated |= appendPreviewLine(tail, current.toString(), maximumLines, keyword);
                        current.setLength(0);
                    } else if (character != '\r') {
                        if (current.length() < settings.maximumRawLineCharacters()) current.append(character);
                        else truncated = true;
                    }
                }
            }
            if (!current.isEmpty()) truncated |= appendPreviewLine(tail, current.toString(), maximumLines, keyword);
        }
        return new PreviewResult(List.copyOf(tail), truncated);
    }

    private static boolean appendPreviewLine(
            Deque<String> tail, String line, int maximumLines, String keyword) {
        if (keyword != null && !line.contains(keyword)) return false;
        boolean truncated = false;
        if (tail.size() == maximumLines) {
            tail.removeFirst();
            truncated = true;
        }
        tail.addLast(line);
        return truncated;
    }

    private InputStream openContent(Path path, boolean compressed) throws IOException {
        InputStream stream = new BufferedInputStream(Files.newInputStream(path));
        return compressed ? new GZIPInputStream(stream) : stream;
    }

    private boolean cleanupExpiredBundlesSafely() {
        try {
            expiredBundlesDeleted.addAndGet(cleanupExpiredBundles());
            return true;
        } catch (IOException | RuntimeException failure) {
            bundleCleanupFailures.incrementAndGet();
            return false;
        }
    }

    private int cleanupExpiredBundles() throws IOException {
        ensureBundleRootSafe();
        Instant now = clock.instant();
        int deleted = 0;
        try (var entries = Files.list(bundleRoot)) {
            var iterator = entries.iterator();
            while (iterator.hasNext()) {
                Path candidate = iterator.next();
                String fileName = candidate.getFileName().toString();
                if (Files.isSymbolicLink(candidate)) {
                    throw new SecurityException("symbolic links are not allowed in the bundle directory");
                }
                if (!isManagedBundleFile(fileName)
                        || !Files.isRegularFile(candidate, LinkOption.NOFOLLOW_LINKS)) {
                    continue;
                }
                BasicFileAttributes attributes = Files.readAttributes(
                        candidate, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                if (!attributes.lastModifiedTime().toInstant()
                        .plus(settings.bundleTimeToLive()).isAfter(now)) {
                    Files.delete(candidate);
                    deleted++;
                }
            }
        }
        return deleted;
    }

    private static boolean isManagedBundleFile(String fileName) {
        String prefix = "cpf-log-bundle-";
        String suffix;
        if (fileName.endsWith(".zip.tmp")) suffix = ".zip.tmp";
        else if (fileName.endsWith(".zip")) suffix = ".zip";
        else return false;
        if (!fileName.startsWith(prefix)) return false;
        String identifier = fileName.substring(prefix.length(), fileName.length() - suffix.length());
        try {
            UUID.fromString(identifier);
            return true;
        } catch (IllegalArgumentException invalid) {
            return false;
        }
    }

    private Path prepareRoot(Path configuredRoot) {
        Objects.requireNonNull(configuredRoot, "root");
        try {
            Path normalized = configuredRoot.toAbsolutePath().normalize();
            if (Files.isSymbolicLink(normalized)) throw new IllegalArgumentException("log root cannot be a symbolic link");
            Files.createDirectories(normalized);
            if (!Files.isDirectory(normalized, LinkOption.NOFOLLOW_LINKS)) {
                throw new IllegalArgumentException("log root must be a directory");
            }
            return normalized.toRealPath(LinkOption.NOFOLLOW_LINKS);
        } catch (IOException failure) {
            throw new IllegalStateException("log root initialization failed: " + failure.getClass().getSimpleName());
        }
    }

    private void prepareBundleRoot() {
        try {
            Files.createDirectories(bundleRoot);
            ensureBundleRootSafe();
        } catch (IOException failure) {
            throw new IllegalStateException("bundle root initialization failed: " + failure.getClass().getSimpleName());
        }
    }

    private void ensureBundleRootSafe() throws IOException {
        if (Files.isSymbolicLink(bundleRoot)
                || !Files.isDirectory(bundleRoot, LinkOption.NOFOLLOW_LINKS)
                || !bundleRoot.toRealPath(LinkOption.NOFOLLOW_LINKS).startsWith(root)) {
            throw new SecurityException("bundle directory is not safe");
        }
    }

    private boolean isSafeRegularFile(Path candidate) {
        try {
            requireSafeFile(candidate);
            return true;
        } catch (IOException | RuntimeException failure) {
            return false;
        }
    }

    private Path requireSafeFile(Path candidate) throws IOException {
        Path normalized = candidate.toAbsolutePath().normalize();
        if (!normalized.startsWith(root) || normalized.startsWith(bundleRoot)) {
            throw new SecurityException("artifact escapes the managed log root");
        }
        rejectSymbolicLinkSegments(normalized);
        Path real = normalized.toRealPath(LinkOption.NOFOLLOW_LINKS);
        if (!real.startsWith(root) || !Files.isRegularFile(real, LinkOption.NOFOLLOW_LINKS)) {
            throw new SecurityException("artifact is not a safe regular file");
        }
        return real;
    }

    private void rejectSymbolicLinkSegments(Path candidate) throws IOException {
        Path relative = root.relativize(candidate);
        Path current = root;
        for (Path part : relative) {
            current = current.resolve(part);
            if (Files.isSymbolicLink(current)) throw new SecurityException("symbolic links are not allowed");
        }
    }

    private List<String> normalizeArtifactIds(List<String> artifactIds) {
        if (artifactIds == null || artifactIds.isEmpty()) {
            throw new IllegalArgumentException("artifactIds are required");
        }
        Set<String> distinct = new LinkedHashSet<>();
        for (String artifactId : artifactIds) distinct.add(required(artifactId, "artifactId", 200));
        if (distinct.size() > settings.maximumBundleArtifacts()) {
            throw new IllegalArgumentException("too many bundle artifacts");
        }
        return List.copyOf(distinct);
    }

    private static String required(String value, String field, int maximumLength) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        String normalized = value.trim();
        if (normalized.length() > maximumLength || normalized.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(field + " is invalid");
        }
        return normalized;
    }

    private static String optionalKeyword(String value) {
        if (value == null || value.isBlank()) return null;
        return required(value, "keyword", 500);
    }

    private static boolean matchesText(String actual, String expected) {
        return expected == null || actual.equalsIgnoreCase(expected);
    }

    private static String inferLogType(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.contains("audit")) return "AUDIT";
        if (lower.contains("error")) return "ERROR";
        if (lower.contains("transaction")) return "TRANSACTION";
        if (lower.contains("batch")) return "BATCH";
        if (lower.contains("integration")) return "INTEGRATION";
        return "APPLICATION";
    }

    private static String digestFile(Path path, long maximumBytes) throws IOException {
        try (InputStream input = new BufferedInputStream(Files.newInputStream(path))) {
            MessageDigest digest = sha256Digest();
            byte[] buffer = new byte[8_192];
            long total = 0L;
            int read;
            while ((read = input.read(buffer)) >= 0) {
                total += read;
                if (total > maximumBytes) throw new IOException("artifact exceeds download limit");
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        }
    }

    private static CopyResult copyMaskedAndDigest(
            Path path,
            boolean compressed,
            ZipOutputStream output,
            long maximumSourceBytes,
            long maximumOutputBytes,
            long maximumDecodedCharacters,
            int maximumRawLineCharacters) throws IOException {
        if (maximumOutputBytes < 1L) throw new IOException("bundle output capacity exceeded");
        long sourceBytes = Files.size(path);
        if (sourceBytes > maximumSourceBytes) throw new IOException("artifact exceeds source size limit");
        MessageDigest digest = sha256Digest();
        long decoded = 0L;
        long written = 0L;
        StringBuilder line = new StringBuilder();
        try (InputStream input = compressed
                ? new GZIPInputStream(new BufferedInputStream(Files.newInputStream(path)))
                : new BufferedInputStream(Files.newInputStream(path));
             Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            char[] buffer = new char[4_096];
            int read;
            while ((read = reader.read(buffer)) >= 0) {
                decoded += read;
                if (decoded > maximumDecodedCharacters) {
                    throw new IOException("artifact exceeds decoded character limit");
                }
                for (int i = 0; i < read; i++) {
                    char character = buffer[i];
                    if (character == '\n') {
                        written = writeMaskedLine(output, digest, line, written, maximumOutputBytes);
                        line.setLength(0);
                    } else if (character != '\r') {
                        if (line.length() < maximumRawLineCharacters) line.append(character);
                    }
                }
            }
            if (!line.isEmpty()) written = writeMaskedLine(output, digest, line, written, maximumOutputBytes);
        }
        return new CopyResult(HexFormat.of().formatHex(digest.digest()), written);
    }

    private static long writeMaskedLine(
            ZipOutputStream output,
            MessageDigest digest,
            CharSequence rawLine,
            long written,
            long maximumOutputBytes) throws IOException {
        String masked = CpfSensitiveData.sanitizeAuditText(rawLine.toString());
        byte[] bytes = (masked + "\n").getBytes(StandardCharsets.UTF_8);
        long next = Math.addExact(written, bytes.length);
        if (next > maximumOutputBytes) throw new IOException("bundle output capacity exceeded");
        output.write(bytes);
        digest.update(bytes);
        return next;
    }

    private static MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException failure) {
            throw new IllegalStateException("SHA-256 is unavailable", failure);
        }
    }

    private static String sha256(String value) {
        return HexFormat.of().formatHex(sha256Digest().digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    private static void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException unsupported) {
            Files.move(source, target);
        }
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // A failed cleanup is visible through bundleFailures; raw paths are not logged here.
        }
    }

    private record LocatedArtifact(Path path, CpfRemoteLogArtifact artifact) { }
    private record PreviewResult(List<String> lines, boolean truncated) { }
    private record CopyResult(String checksum, long outputBytes) { }

    public record Settings(
            String environment,
            String module,
            String service,
            String instance,
            Duration retention,
            Duration activeWindow,
            Duration bundleTimeToLive,
            int maximumDepth,
            int maximumScannedFiles,
            int maximumPreviewLines,
            long maximumSearchBytes,
            long maximumPreviewBytes,
            long maximumPreviewDecodedCharacters,
            int maximumRawLineCharacters,
            long maximumDownloadBytes,
            long maximumBundleBytes,
            int maximumBundleArtifacts,
            boolean sourceAlreadyMasked) {

        public Settings {
            environment = required(environment, "environment", 100);
            module = required(module, "module", 100);
            service = required(service, "service", 100);
            instance = required(instance, "instance", 200);
            retention = positive(retention, "retention");
            activeWindow = positive(activeWindow, "activeWindow");
            bundleTimeToLive = positive(bundleTimeToLive, "bundleTimeToLive");
            if (maximumDepth < 1 || maximumDepth > 64) throw new IllegalArgumentException("maximumDepth is invalid");
            if (maximumScannedFiles < 1 || maximumScannedFiles > 1_000_000) throw new IllegalArgumentException("maximumScannedFiles is invalid");
            if (maximumPreviewLines < 1 || maximumPreviewLines > 5_000) throw new IllegalArgumentException("maximumPreviewLines is invalid");
            if (maximumSearchBytes < 1 || maximumSearchBytes > 1_073_741_824L) throw new IllegalArgumentException("maximumSearchBytes is invalid");
            if (maximumPreviewBytes < 1 || maximumPreviewBytes > Integer.MAX_VALUE) throw new IllegalArgumentException("maximumPreviewBytes is invalid");
            if (maximumPreviewDecodedCharacters < maximumPreviewBytes || maximumPreviewDecodedCharacters > 4_294_967_296L) throw new IllegalArgumentException("maximumPreviewDecodedCharacters is invalid");
            if (maximumRawLineCharacters < 1 || maximumRawLineCharacters > 65_536) throw new IllegalArgumentException("maximumRawLineCharacters is invalid");
            if (maximumDownloadBytes < 1 || maximumDownloadBytes > 10_737_418_240L) throw new IllegalArgumentException("maximumDownloadBytes is invalid");
            if (maximumBundleBytes < 1 || maximumBundleBytes > 53_687_091_200L) throw new IllegalArgumentException("maximumBundleBytes is invalid");
            if (maximumBundleArtifacts < 1 || maximumBundleArtifacts > 10_000) throw new IllegalArgumentException("maximumBundleArtifacts is invalid");
        }

        public static Settings defaults(String environment, String module, String service, String instance) {
            return new Settings(
                    environment, module, service, instance,
                    Duration.ofDays(30), Duration.ofMinutes(2), Duration.ofMinutes(15),
                    16, 100_000, 1_000, 8L * 1_024 * 1_024,
                    4L * 1_024 * 1_024, 16L * 1_024 * 1_024,
                    16_384, 512L * 1_024 * 1_024, 2L * 1_024 * 1_024 * 1_024,
                    100, false);
        }

        private static Duration positive(Duration value, String field) {
            Objects.requireNonNull(value, field);
            if (value.isZero() || value.isNegative()) throw new IllegalArgumentException(field + " must be positive");
            return value;
        }
    }
}
