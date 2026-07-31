package com.cpf.batch.agent.internal;

import com.cpf.batch.agent.AgentProperties;
import com.cpf.batch.api.AgentArtifactRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
import java.util.Base64;
import java.util.HexFormat;
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
    private final HttpClient httpClient;

    public ArtifactInstaller(AgentProperties properties, ArtifactVerifier verifier, ArtifactStateStore stateStore) {
        this.properties = Objects.requireNonNull(properties, "properties");
        this.verifier = Objects.requireNonNull(verifier, "verifier");
        this.stateStore = Objects.requireNonNull(stateStore, "stateStore");
        HttpClient.Builder builder = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(20));
        if (properties.getArtifactProxyHost() != null && !properties.getArtifactProxyHost().isBlank()) {
            if (properties.getArtifactProxyPort() < 1 || properties.getArtifactProxyPort() > 65535) {
                throw new IllegalStateException("ARTIFACT_PROXY_PORT_INVALID");
            }
            builder.proxy(ProxySelector.of(new InetSocketAddress(
                    properties.getArtifactProxyHost().trim(), properties.getArtifactProxyPort())));
        }
        this.httpClient = builder.build();
    }

    public Result install(AgentArtifactRequest request) throws Exception {
        Objects.requireNonNull(request, "request");
        AgentProperties.ServiceDefinition service = service(request.serviceId());
        validate(request, service);
        String extension = extension(request.runtimeMode());
        Path root = secureRoot(service.getInstallRoot());
        Path part = null;
        try (FileChannel channel = FileChannel.open(root.resolve(".install.lock"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock ignored = exclusiveLock(channel)) {
            Path currentStatePath = root.resolve("artifact-state.properties");
            Path previousStatePath = root.resolve("artifact-previous.properties");
            Properties current = stateStore.read(currentStatePath, false);
            long currentSequence = longValue(current, "releaseSequence", -1);
            validateSequence(request, current, currentSequence);
            if (isExactReplay(request, current, currentSequence)) {
                // State와 Binary가 모두 온전한 경우에만 idempotent replay로 인정합니다.
                Path replayArtifact = secureArtifactPath(root, required(current, "path"));
                verifier.verifyStored(replayArtifact, current, service);
                return new Result(request.version(), current.getProperty("previousVersion", ""), replayArtifact.toString());
            }

            Path releases = secureDirectory(root, root.resolve("releases"));
            Path release = secureDirectory(root, releases.resolve(request.version()));
            part = release.resolve(service.getArtifactId() + extension + ".part").normalize();
            Path target = release.resolve(service.getArtifactId() + extension).normalize();
            requireChild(root, part);
            requireChild(root, target);
            Files.deleteIfExists(part);

            long size = download(artifactUri(request, extension), part, request, extension);
            ArtifactVerifier.Verified verified = verifier.verify(part, request, size);
            move(part, target);
            part = null;

            if (request.configRef() != null && !request.configRef().isBlank()) {
                if (!request.configRef().matches("(?i)^(vault|secret|config)://[A-Za-z0-9._/@:-]+$")) {
                    throw new SecurityException("ARTIFACT_CONFIG_REFERENCE_INVALID");
                }
                write(root.resolve("deployment-config.ref"), request.configRef());
            }

            if (!current.isEmpty()) {
                // 기존 active state도 쓰기 전에 Binary/서명을 재검증하여 손상 상태를 previous로 승격하지 않습니다.
                Path activeArtifact = secureArtifactPath(root, required(current, "path"));
                verifier.verifyStored(activeArtifact, current, service);
                stateStore.write(previousStatePath, current);
            }
            Properties next = state(request, current, verified, target);
            stateStore.write(currentStatePath, next);
            return new Result(request.version(), current.getProperty("version", ""), target.toString());
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
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock ignored = exclusiveLock(channel)) {
            Path currentStatePath = root.resolve("artifact-state.properties");
            Path previousStatePath = root.resolve("artifact-previous.properties");
            Properties previous = stateStore.read(previousStatePath, true);
            Properties current = stateStore.read(currentStatePath, true);

            Path previousArtifact = secureArtifactPath(root, required(previous, "path"));
            Path currentArtifact = secureArtifactPath(root, required(current, "path"));
            // Rollback 전 양쪽 상태를 모두 검증합니다. 손상된 active를 previous slot에 저장하지 않습니다.
            verifier.verifyStored(previousArtifact, previous, service);
            verifier.verifyStored(currentArtifact, current, service);

            stateStore.write(currentStatePath, previous);
            try {
                stateStore.write(previousStatePath, current);
            } catch (Exception failure) {
                // 두 상태 파일 publish 중 부분 실패가 발생하면 active pointer를 원래 상태로 복구합니다.
                stateStore.write(currentStatePath, current);
                throw new IllegalStateException("ARTIFACT_ROLLBACK_STATE_SWAP_FAILED", failure);
            }
            return required(previous, "version");
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
        validateResolvedAddresses(uri.getHost());
        HttpResponse<InputStream> response = httpClient.send(
                HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofMinutes(5))
                        .header("Accept", expectedMime(extension))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) throw new IOException("ARTIFACT_REPOSITORY_STATUS:" + response.statusCode());
        String contentType = response.headers().firstValue("Content-Type")
                .map(value -> value.split(";", 2)[0].trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new SecurityException("ARTIFACT_CONTENT_TYPE_MISSING"));
        if (!properties.getArtifactAllowedContentTypes().stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(contentType::equals)) {
            throw new SecurityException("ARTIFACT_CONTENT_TYPE_DENIED:" + contentType);
        }
        validateDigestHeader(response, request.sha256());
        long declared = response.headers().firstValueAsLong("Content-Length").orElse(-1);
        if (declared < 0 || declared > properties.getMaxArtifactBytes()) {
            throw new SecurityException("ARTIFACT_CONTENT_LENGTH_INVALID");
        }
        try (InputStream input = response.body();
                OutputStream output = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
            byte[] buffer = new byte[8192];
            long total = 0;
            for (int read; (read = input.read(buffer)) >= 0;) {
                if (read == 0) continue;
                total += read;
                if (total > declared || total > properties.getMaxArtifactBytes()) {
                    throw new SecurityException("ARTIFACT_STREAM_SIZE_EXCEEDED");
                }
                output.write(buffer, 0, read);
            }
            if (total != declared) throw new SecurityException("ARTIFACT_CONTENT_LENGTH_MISMATCH");
            return total;
        }
    }

    private void validateDigestHeader(HttpResponse<?> response, String expectedHex) {
        String checksum = response.headers().firstValue("X-Checksum-Sha256").orElse(null);
        if (checksum == null) {
            String digest = response.headers().firstValue("Digest").orElse(null);
            if (digest != null) {
                for (String part : digest.split(",")) {
                    String trimmed = part.trim();
                    if (trimmed.regionMatches(true, 0, "sha-256=", 0, 8)) {
                        try { checksum = HexFormat.of().formatHex(Base64.getDecoder().decode(trimmed.substring(8))); }
                        catch (IllegalArgumentException failure) {
                            throw new SecurityException("ARTIFACT_DIGEST_HEADER_INVALID", failure);
                        }
                        break;
                    }
                }
            }
        }
        if (checksum == null || checksum.isBlank()) {
            if (properties.isRequireRepositoryDigestHeader()) {
                throw new SecurityException("ARTIFACT_DIGEST_HEADER_REQUIRED");
            }
            return;
        }
        String normalized = checksum.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[0-9a-f]{64}") || !normalized.equals(expectedHex.toLowerCase(Locale.ROOT))) {
            throw new SecurityException("ARTIFACT_DIGEST_HEADER_MISMATCH");
        }
    }

    private void validateResolvedAddresses(String host) throws Exception {
        if (properties.isAllowPrivateRepositoryAddresses()) return;
        for (InetAddress address : InetAddress.getAllByName(host)) {
            if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress() || address.isMulticastAddress()) {
                throw new SecurityException("ARTIFACT_REPOSITORY_ADDRESS_DENIED:" + address.getHostAddress());
            }
        }
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
        if (!"https".equalsIgnoreCase(base.getScheme()) && !isLoopback(base.getHost())) {
            throw new SecurityException("ARTIFACT_REPOSITORY_TLS_REQUIRED");
        }
        if (!properties.getArtifactAllowedHosts().isEmpty()
                && properties.getArtifactAllowedHosts().stream().noneMatch(base.getHost()::equalsIgnoreCase)) {
            throw new SecurityException("ARTIFACT_REPOSITORY_HOST_DENIED");
        }
    }

    private static boolean isLoopback(String host) {
        try { return InetAddress.getByName(host).isLoopbackAddress(); }
        catch (Exception failure) { return false; }
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

    private static void write(Path path, String value) throws IOException {
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(temporary, value, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        move(temporary, path);
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
