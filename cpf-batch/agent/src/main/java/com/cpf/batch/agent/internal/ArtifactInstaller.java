package com.cpf.batch.agent.internal;

import com.cpf.batch.agent.AgentProperties;
import com.cpf.batch.api.AgentArtifactRequest;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * 서명·환경 결합·anti-rollback·서비스별 fencing lock을 적용하는 Artifact Installer입니다.
 * Artifact 상태는 HMAC으로 보호하며 rollback 직전에 Binary와 원 서명을 다시 검증합니다.
 */
public final class ArtifactInstaller {
    private static final Pattern SAFE = Pattern.compile("[A-Za-z0-9._-]{1,128}");
    private static final Pattern COORDINATE = Pattern.compile("[A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+");
    private final AgentProperties properties;
    private final ArtifactVerifier verifier;
    private final ArtifactStateStore stateStore;
    private final PinnedArtifactHttpTransport artifactTransport;

    public ArtifactInstaller(AgentProperties properties, ArtifactVerifier verifier, ArtifactStateStore stateStore) {
        this.properties = Objects.requireNonNull(properties, "properties");
        this.verifier = Objects.requireNonNull(verifier, "verifier");
        this.stateStore = Objects.requireNonNull(stateStore, "stateStore");
        if (properties.getArtifactProxyHost() != null && !properties.getArtifactProxyHost().isBlank()
                && (properties.getArtifactProxyPort() < 1 || properties.getArtifactProxyPort() > 65535)) {
            throw new IllegalStateException("ARTIFACT_PROXY_PORT_INVALID");
        }
        this.artifactTransport = new PinnedArtifactHttpTransport(properties);
    }

    public Result install(AgentArtifactRequest request) throws Exception {
        Objects.requireNonNull(request, "request");
        AgentProperties.ServiceDefinition service = service(request.serviceId());
        validate(request, service);
        String extension = extension(request.runtimeMode());
        Path root = secureRoot(service.getInstallRoot());
        Path part = null;
        try (FileChannel channel = FileChannel.open(root.resolve(".install.lock"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS);
                FileLock ignored = exclusiveLock(channel)) {
            Path currentStatePath = root.resolve("artifact-state.properties");
            Path previousStatePath = root.resolve("artifact-previous.properties");
            Path configPath = root.resolve("deployment-config.ref");
            Properties current = stateStore.read(currentStatePath, false);
            long currentSequence = longValue(current, "releaseSequence", -1);
            validateSequence(request, current, currentSequence);
            if (isExactReplay(request, current, currentSequence)) {
                Path replayArtifact = secureArtifactPath(root, required(current, "path"));
                verifier.verifyStored(replayArtifact, current, service);
                return new Result(request.version(), current.getProperty("previousVersion", ""), replayArtifact.toString());
            }

            Properties previous = stateStore.read(previousStatePath, false);
            boolean currentStateExisted = Files.exists(currentStatePath, LinkOption.NOFOLLOW_LINKS);
            boolean previousStateExisted = Files.exists(previousStatePath, LinkOption.NOFOLLOW_LINKS);
            boolean configExisted = Files.exists(configPath, LinkOption.NOFOLLOW_LINKS);
            byte[] previousConfig = readOptionalRegularFile(configPath);

            if (!current.isEmpty()) {
                Path activeArtifact = secureArtifactPath(root, required(current, "path"));
                verifier.verifyStored(activeArtifact, current, service);
            }

            Path releases = secureDirectory(root, root.resolve("releases"));
            Path release = secureDirectory(root, releases.resolve(request.version()));
            part = release.resolve(service.getArtifactId() + extension + ".part").normalize();
            Path target = release.resolve(service.getArtifactId() + extension).normalize();
            requireChild(root, part);
            requireChild(root, target);
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                throw new SecurityException("ARTIFACT_RELEASE_PATH_COLLISION");
            }
            Files.deleteIfExists(part);

            long size = download(artifactUri(request, extension), part, request, extension);
            ArtifactVerifier.Verified verified = verifier.verify(part, request, size);
            Properties next = state(request, current, verified, target);

            boolean targetPublished = false;
            try {
                move(part, target);
                part = null;
                targetPublished = true;
                publishConfig(configPath, request.configRef());
                if (!current.isEmpty()) {
                    stateStore.write(previousStatePath, current);
                }
                stateStore.write(currentStatePath, next);
                return new Result(request.version(), current.getProperty("version", ""), target.toString());
            } catch (Exception publicationFailure) {
                Exception restoreFailure = restorePublication(
                        target, targetPublished,
                        configPath, configExisted, previousConfig,
                        previousStatePath, previousStateExisted, previous,
                        currentStatePath, currentStateExisted, current);
                String code = restoreFailure == null
                        ? "ARTIFACT_INSTALL_PUBLICATION_ROLLED_BACK"
                        : "ARTIFACT_INSTALL_RESULT_UNKNOWN";
                IllegalStateException wrapped = new IllegalStateException(code, publicationFailure);
                if (restoreFailure != null) wrapped.addSuppressed(restoreFailure);
                throw wrapped;
            }
        } catch (OverlappingFileLockException failure) {
            throw new IllegalStateException("ARTIFACT_INSTALL_ALREADY_RUNNING", failure);
        } finally {
            if (part != null) Files.deleteIfExists(part);
        }
    }

    public String rollback(String serviceId) throws Exception {
        AgentProperties.ServiceDefinition service = service(serviceId);
        Path root = secureRoot(service.getInstallRoot());
        try (FileChannel channel = FileChannel.open(root.resolve(".install.lock"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS);
                FileLock ignored = exclusiveLock(channel)) {
            Path currentStatePath = root.resolve("artifact-state.properties");
            Path previousStatePath = root.resolve("artifact-previous.properties");
            Path configPath = root.resolve("deployment-config.ref");
            Properties previous = stateStore.read(previousStatePath, true);
            Properties current = stateStore.read(currentStatePath, true);
            boolean configExisted = Files.exists(configPath, LinkOption.NOFOLLOW_LINKS);
            byte[] currentConfig = readOptionalRegularFile(configPath);

            Path previousArtifact = secureArtifactPath(root, required(previous, "path"));
            Path currentArtifact = secureArtifactPath(root, required(current, "path"));
            verifier.verifyStored(previousArtifact, previous, service);
            verifier.verifyStored(currentArtifact, current, service);
            String previousConfigRef = previous.getProperty("configRef", "");
            validateConfigReference(previousConfigRef);

            try {
                publishConfig(configPath, previousConfigRef);
                stateStore.write(currentStatePath, previous);
                stateStore.write(previousStatePath, current);
                return required(previous, "version");
            } catch (Exception publicationFailure) {
                Exception restoreFailure = null;
                restoreFailure = attemptRestore(
                        restoreFailure, () -> stateStore.write(currentStatePath, current));
                restoreFailure = attemptRestore(
                        restoreFailure, () -> stateStore.write(previousStatePath, previous));
                restoreFailure = attemptRestore(
                        restoreFailure, () -> restoreFile(configPath, configExisted, currentConfig));
                String code = restoreFailure == null
                        ? "ARTIFACT_ROLLBACK_PUBLICATION_ROLLED_BACK"
                        : "ARTIFACT_ROLLBACK_RESULT_UNKNOWN";
                IllegalStateException wrapped = new IllegalStateException(code, publicationFailure);
                if (restoreFailure != null) wrapped.addSuppressed(restoreFailure);
                throw wrapped;
            }
        } catch (OverlappingFileLockException failure) {
            throw new IllegalStateException("ARTIFACT_INSTALL_ALREADY_RUNNING", failure);
        }
    }

    private static FileLock exclusiveLock(FileChannel channel) throws IOException {
        FileLock lock = channel.tryLock();
        if (lock == null) throw new IllegalStateException("ARTIFACT_INSTALL_ALREADY_RUNNING");
        return lock;
    }

    private void validate(AgentArtifactRequest request, AgentProperties.ServiceDefinition service) {
        if (request.serviceId() == null || !SAFE.matcher(request.serviceId()).matches()) {
            throw new SecurityException("ARTIFACT_SERVICE_ID_INVALID");
        }
        if (request.coordinate() == null || !COORDINATE.matcher(request.coordinate()).matches()
                || !request.coordinate().endsWith(":" + service.getArtifactId())) {
            throw new SecurityException("ARTIFACT_SERVICE_MISMATCH");
        }
        if (request.version() == null || !SAFE.matcher(request.version()).matches() || request.releaseSequence() <= 0) {
            throw new SecurityException("ARTIFACT_RELEASE_IDENTITY_INVALID");
        }
        if (!Objects.equals(request.runtimeMode(), service.getRuntimeMode())) {
            throw new SecurityException("ARTIFACT_RUNTIME_MODE_MISMATCH");
        }
        if (!Objects.equals(request.environmentCode(), service.getEnvironmentCode())
                || !Objects.equals(request.channel(), service.getReleaseChannel())) {
            throw new SecurityException("ARTIFACT_ENVIRONMENT_CHANNEL_MISMATCH");
        }
        if (request.reason() == null || request.reason().trim().length() < 5
                || request.requestedBy() == null || request.requestedBy().isBlank()) {
            throw new SecurityException("ARTIFACT_OPERATOR_REASON_REQUIRED");
        }
        validateConfigReference(request.configRef());
    }

    private static void validateSequence(AgentArtifactRequest request, Properties current, long currentSequence) {
        if (request.releaseSequence() < currentSequence) {
            throw new SecurityException("ARTIFACT_ROLLBACK_SEQUENCE_REJECTED");
        }
        if (request.releaseSequence() == currentSequence && !isExactReplay(request, current, currentSequence)) {
            throw new SecurityException("ARTIFACT_RELEASE_SEQUENCE_COLLISION");
        }
    }

    private static boolean isExactReplay(AgentArtifactRequest request, Properties current, long currentSequence) {
        return request.releaseSequence() == currentSequence
                && request.serviceId().equals(current.getProperty("serviceId"))
                && request.coordinate().equals(current.getProperty("coordinate"))
                && request.version().equals(current.getProperty("version"))
                && request.sha256().equalsIgnoreCase(current.getProperty("sha256", ""))
                && request.environmentCode().equals(current.getProperty("environment", ""))
                && request.channel().equals(current.getProperty("channel", ""))
                && request.keyId().equals(current.getProperty("keyId", ""));
    }

    private Properties state(AgentArtifactRequest request, Properties current,
            ArtifactVerifier.Verified verified, Path target) {
        Properties next = new Properties();
        next.setProperty("serviceId", request.serviceId());
        next.setProperty("coordinate", request.coordinate());
        next.setProperty("version", request.version());
        next.setProperty("sha256", verified.sha256());
        next.setProperty("size", Long.toString(verified.size()));
        next.setProperty("releaseSequence", Long.toString(request.releaseSequence()));
        next.setProperty("path", target.toAbsolutePath().normalize().toString());
        next.setProperty("environment", request.environmentCode());
        next.setProperty("channel", request.channel());
        next.setProperty("keyId", request.keyId());
        next.setProperty("runtimeMode", request.runtimeMode());
        next.setProperty("signatureBase64", request.signatureBase64());
        next.setProperty("configRef", request.configRef() == null ? "" : request.configRef());
        next.setProperty("previousVersion", current.getProperty("version", ""));
        return next;
    }

    private long download(URI uri, Path target, AgentArtifactRequest request, String extension) throws Exception {
        return artifactTransport.download(uri, target, expectedMime(extension), request.sha256());
    }

    private static String expectedMime(String extension) {
        return ".jar".equals(extension) ? "application/java-archive" : "application/zip";
    }

    private URI artifactUri(AgentArtifactRequest request, String extension) {
        URI base = URI.create(Objects.requireNonNull(properties.getArtifactRepositoryBaseUrl())).normalize();
        validateRepositoryBase(base);
        String[] coordinate = request.coordinate().split(":", 2);
        URI root = URI.create(base.toString().replaceAll("/+$", "") + "/");
        URI artifact = root.resolve(coordinate[0].replace('.', '/') + "/" + coordinate[1] + "/"
                + request.version() + "/" + coordinate[1] + "-" + request.version() + extension).normalize();
        if (!Objects.equals(root.getScheme(), artifact.getScheme())
                || !Objects.equals(root.getHost(), artifact.getHost())
                || root.getPort() != artifact.getPort()
                || !artifact.getPath().startsWith(root.getPath())) {
            throw new SecurityException("ARTIFACT_REPOSITORY_ESCAPE");
        }
        return artifact;
    }

    private void validateRepositoryBase(URI base) {
        if (!base.isAbsolute() || base.getHost() == null || base.getUserInfo() != null
                || base.getQuery() != null || base.getFragment() != null) {
            throw new SecurityException("ARTIFACT_REPOSITORY_URL_INVALID");
        }
        if (!"https".equalsIgnoreCase(base.getScheme())) {
            if (!"http".equalsIgnoreCase(base.getScheme())
                    || !properties.isAllowHttpLoopback()
                    || !literalLoopback(base.getHost())) {
                throw new SecurityException("ARTIFACT_REPOSITORY_TLS_REQUIRED");
            }
        }
        if (!properties.getArtifactAllowedHosts().isEmpty()
                && properties.getArtifactAllowedHosts().stream().noneMatch(base.getHost()::equalsIgnoreCase)) {
            throw new SecurityException("ARTIFACT_REPOSITORY_HOST_DENIED");
        }
    }

    private static boolean literalLoopback(String host) {
        if (host == null) return false;
        String value = host.trim().toLowerCase(Locale.ROOT);
        return "localhost".equals(value) || "127.0.0.1".equals(value) || "::1".equals(value)
                || "0:0:0:0:0:0:0:1".equals(value);
    }

    private AgentProperties.ServiceDefinition service(String id) {
        return properties.getServices().values().stream()
                .filter(candidate -> id != null && id.equals(candidate.getServiceId()))
                .findFirst()
                .orElseThrow(() -> new SecurityException("ARTIFACT_SERVICE_NOT_APPROVED"));
    }

    private static String extension(String runtimeMode) {
        if (runtimeMode == null) throw new SecurityException("ARTIFACT_RUNTIME_MODE_REQUIRED");
        return switch (runtimeMode.toLowerCase(Locale.ROOT)) {
            case "embedded-bootjar" -> ".jar";
            case "external-tomcat-war" -> ".war";
            default -> throw new SecurityException("ARTIFACT_RUNTIME_MODE_UNSUPPORTED");
        };
    }

    private static Path secureRoot(String value) throws IOException {
        Path root = Path.of(value).toAbsolutePath().normalize();
        if (Files.exists(root, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(root)) {
            throw new SecurityException("ARTIFACT_INSTALL_ROOT_SYMLINK");
        }
        Files.createDirectories(root);
        return root.toRealPath(LinkOption.NOFOLLOW_LINKS);
    }

    private static Path secureDirectory(Path root, Path path) throws IOException {
        Path normalized = path.toAbsolutePath().normalize();
        requireChild(root, normalized);
        if (Files.exists(normalized, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(normalized)) {
            throw new SecurityException("ARTIFACT_DIRECTORY_SYMLINK");
        }
        Files.createDirectories(normalized);
        return normalized.toRealPath(LinkOption.NOFOLLOW_LINKS);
    }

    private static Path secureArtifactPath(Path root, String value) throws IOException {
        Path artifact = Path.of(value).toAbsolutePath().normalize();
        requireChild(root, artifact);
        if (Files.isSymbolicLink(artifact) || !Files.isRegularFile(artifact, LinkOption.NOFOLLOW_LINKS)) {
            throw new SecurityException("ARTIFACT_BINARY_MISSING_OR_UNSAFE");
        }
        return artifact;
    }

    private static void requireChild(Path root, Path path) {
        if (!path.toAbsolutePath().normalize().startsWith(root.toAbsolutePath().normalize())) {
            throw new SecurityException("ARTIFACT_PATH_ESCAPE");
        }
    }

    private static void move(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException failure) {
            throw new IOException("ARTIFACT_ATOMIC_MOVE_REQUIRED", failure);
        }
    }

    private static void validateConfigReference(String configRef) {
        if (configRef != null && !configRef.isBlank()
                && !configRef.matches("(?i)^(vault|secret|config)://[A-Za-z0-9._/@:-]+$")) {
            throw new SecurityException("ARTIFACT_CONFIG_REFERENCE_INVALID");
        }
    }

    private static void publishConfig(Path configPath, String configRef) throws IOException {
        if (configRef == null || configRef.isBlank()) {
            Files.deleteIfExists(configPath);
        } else {
            write(configPath, configRef);
        }
    }

    private Exception restorePublication(
            Path target,
            boolean targetPublished,
            Path configPath,
            boolean configExisted,
            byte[] previousConfig,
            Path previousStatePath,
            boolean previousStateExisted,
            Properties previousState,
            Path currentStatePath,
            boolean currentStateExisted,
            Properties currentState) {
        Exception first = null;
        first = attemptRestore(first, () -> restoreState(currentStatePath, currentStateExisted, currentState));
        first = attemptRestore(first, () -> restoreState(previousStatePath, previousStateExisted, previousState));
        first = attemptRestore(first, () -> restoreFile(configPath, configExisted, previousConfig));
        if (targetPublished) first = attemptRestore(first, () -> Files.deleteIfExists(target));
        return first;
    }

    private void restoreState(Path path, boolean existed, Properties state) throws Exception {
        if (existed) stateStore.write(path, state);
        else Files.deleteIfExists(path);
    }

    private static void restoreFile(Path path, boolean existed, byte[] content) throws Exception {
        if (existed) write(path, content == null ? new byte[0] : content);
        else Files.deleteIfExists(path);
    }

    private static Exception attemptRestore(Exception first, RestoreAction action) {
        try {
            action.run();
            return first;
        } catch (Exception failure) {
            if (first == null) return failure;
            first.addSuppressed(failure);
            return first;
        }
    }

    private static byte[] readOptionalRegularFile(Path path) throws IOException {
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) return null;
        if (Files.isSymbolicLink(path) || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new SecurityException("ARTIFACT_CONFIG_FILE_UNSAFE");
        }
        return Files.readAllBytes(path);
    }

    private static void write(Path path, String value) throws IOException {
        write(path, value.getBytes(StandardCharsets.UTF_8));
    }

    private static void write(Path path, byte[] value) throws IOException {
        Path parent = path.toAbsolutePath().normalize().getParent();
        if (parent == null) throw new IOException("ARTIFACT_CONFIG_PARENT_MISSING");
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(path)) {
            throw new SecurityException("ARTIFACT_CONFIG_FILE_UNSAFE");
        }
        Path temporary = Files.createTempFile(parent, path.getFileName() + ".", ".tmp");
        try {
            try (FileChannel channel = FileChannel.open(temporary,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, LinkOption.NOFOLLOW_LINKS)) {
                java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(value);
                while (buffer.hasRemaining()) channel.write(buffer);
                channel.force(true);
            }
            move(temporary, path);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    @FunctionalInterface
    private interface RestoreAction {
        void run() throws Exception;
    }

    private static String required(Properties state, String name) {
        String value = state.getProperty(name, "").trim();
        if (value.isEmpty()) throw new SecurityException("ARTIFACT_STATE_FIELD_MISSING:" + name);
        return value;
    }

    private static long longValue(Properties properties, String name, long fallback) {
        try { return Long.parseLong(properties.getProperty(name)); }
        catch (RuntimeException failure) { return fallback; }
    }

    public record Result(String version, String previousVersion, String path) {}
}
