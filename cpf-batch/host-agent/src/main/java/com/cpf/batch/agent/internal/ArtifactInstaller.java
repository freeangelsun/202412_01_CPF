package com.cpf.batch.agent.internal;

import com.cpf.batch.agent.AgentProperties;
import com.cpf.batch.api.AgentArtifactRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
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
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

/** 서명·환경 결합·anti-rollback·서비스별 fencing lock을 적용하는 Artifact Installer입니다. */
public final class ArtifactInstaller {
    private static final Pattern SAFE = Pattern.compile("[A-Za-z0-9._-]{1,128}");
    private static final Pattern COORDINATE = Pattern.compile("[A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+");
    private final AgentProperties properties;
    private final ArtifactVerifier verifier;
    private final HttpClient httpClient;

    public ArtifactInstaller(AgentProperties properties, ArtifactVerifier verifier) {
        this.properties = properties;
        this.verifier = verifier;
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
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
            Properties current = read(root.resolve("artifact-state.properties"));
            long currentSequence = longValue(current, "releaseSequence", -1);
            validateSequence(request, current, currentSequence);
            if (isExactReplay(request, current, currentSequence)) {
                return new Result(request.version(), current.getProperty("previousVersion", ""),
                        current.getProperty("path", ""));
            }

            Path releases = secureDirectory(root, root.resolve("releases"));
            Path release = secureDirectory(root, releases.resolve(request.version()));
            part = release.resolve(service.getArtifactId() + extension + ".part").normalize();
            Path target = release.resolve(service.getArtifactId() + extension).normalize();
            requireChild(root, part);
            requireChild(root, target);
            Files.deleteIfExists(part);

            long size = download(artifactUri(request, extension), part);
            ArtifactVerifier.Verified verified = verifier.verify(part, request, size);
            move(part, target);
            part = null;

            if (request.configRef() != null && !request.configRef().isBlank()) {
                if (!request.configRef().matches("(?i)^(vault|secret|config)://[A-Za-z0-9._/@:-]+$")) {
                    throw new SecurityException("ARTIFACT_CONFIG_REFERENCE_INVALID");
                }
                write(root.resolve("deployment-config.ref"), request.configRef());
            }

            if (!current.isEmpty()) writeProperties(root.resolve("artifact-previous.properties"), current);
            Properties next = state(request, current, verified, target);
            writeProperties(root.resolve("artifact-state.properties"), next);
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
            Path previousPath = root.resolve("artifact-previous.properties");
            Properties previous = read(previousPath);
            if (previous.isEmpty()) throw new IllegalStateException("ARTIFACT_ROLLBACK_STATE_MISSING");
            Path artifact = Path.of(previous.getProperty("path", "")).toAbsolutePath().normalize();
            requireChild(root, artifact);
            if (!Files.isRegularFile(artifact, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(artifact)) {
                throw new SecurityException("ARTIFACT_ROLLBACK_BINARY_MISSING_OR_UNSAFE");
            }
            String expected = previous.getProperty("sha256", "");
            if (expected.isBlank()) throw new SecurityException("ARTIFACT_ROLLBACK_DIGEST_MISSING");
            Properties current = read(root.resolve("artifact-state.properties"));
            writeProperties(root.resolve("artifact-state.properties"), previous);
            writeProperties(previousPath, current);
            return previous.getProperty("version");
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
        if (!Objects.equals(request.environmentCode(), service.getEnvironmentCode())
                || !Objects.equals(request.channel(), service.getReleaseChannel())) {
            throw new SecurityException("ARTIFACT_ENVIRONMENT_CHANNEL_MISMATCH");
        }
        if (request.reason() == null || request.reason().trim().length() < 5
                || request.requestedBy() == null || request.requestedBy().isBlank()) {
            throw new SecurityException("ARTIFACT_OPERATOR_REASON_REQUIRED");
        }
    }

    private static void validateSequence(
            AgentArtifactRequest request, Properties current, long currentSequence) {
        if (request.releaseSequence() < currentSequence) {
            throw new SecurityException("ARTIFACT_ROLLBACK_SEQUENCE_REJECTED");
        }
        if (request.releaseSequence() == currentSequence && !isExactReplay(request, current, currentSequence)) {
            throw new SecurityException("ARTIFACT_RELEASE_SEQUENCE_COLLISION");
        }
    }

    private static boolean isExactReplay(
            AgentArtifactRequest request, Properties current, long currentSequence) {
        return request.releaseSequence() == currentSequence
                && request.version().equals(current.getProperty("version"))
                && request.sha256().equalsIgnoreCase(current.getProperty("sha256", ""))
                && request.environmentCode().equals(current.getProperty("environment", ""))
                && request.channel().equals(current.getProperty("channel", ""));
    }

    private Properties state(
            AgentArtifactRequest request,
            Properties current,
            ArtifactVerifier.Verified verified,
            Path target) {
        Properties next = new Properties();
        next.setProperty("version", request.version());
        next.setProperty("sha256", verified.sha256());
        next.setProperty("size", Long.toString(verified.size()));
        next.setProperty("releaseSequence", Long.toString(request.releaseSequence()));
        next.setProperty("path", target.toString());
        next.setProperty("environment", request.environmentCode());
        next.setProperty("channel", request.channel());
        next.setProperty("keyId", request.keyId());
        next.setProperty("previousVersion", current.getProperty("version", ""));
        return next;
    }

    private long download(URI uri, Path target) throws Exception {
        HttpResponse<InputStream> response = httpClient.send(
                HttpRequest.newBuilder(uri).timeout(Duration.ofMinutes(5)).GET().build(),
                HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) throw new IOException("ARTIFACT_REPOSITORY_STATUS:" + response.statusCode());
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

    private static void validateRepositoryBase(URI base) {
        if (!base.isAbsolute() || base.getHost() == null || base.getUserInfo() != null
                || base.getQuery() != null || base.getFragment() != null) {
            throw new SecurityException("ARTIFACT_REPOSITORY_URL_INVALID");
        }
        if (!"https".equalsIgnoreCase(base.getScheme()) && !isLoopback(base.getHost())) {
            throw new SecurityException("ARTIFACT_REPOSITORY_TLS_REQUIRED");
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

    private static void requireChild(Path root, Path path) {
        if (!path.toAbsolutePath().normalize().startsWith(root.toAbsolutePath().normalize())) {
            throw new SecurityException("ARTIFACT_PATH_ESCAPE");
        }
    }

    private static void move(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void write(Path path, String value) throws IOException {
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(temporary, value, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        move(temporary, path);
    }

    private static Properties read(Path path) throws IOException {
        Properties properties = new Properties();
        if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            try (InputStream input = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)) {
                properties.load(input);
            }
        }
        return properties;
    }

    private static void writeProperties(Path path, Properties properties) throws IOException {
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        try (OutputStream output = Files.newOutputStream(temporary,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            properties.store(output, "CPF verified artifact state");
        }
        move(temporary, path);
    }

    private static long longValue(Properties properties, String name, long fallback) {
        try { return Long.parseLong(properties.getProperty(name)); }
        catch (RuntimeException failure) { return fallback; }
    }

    public record Result(String version, String previousVersion, String path) {}
}
