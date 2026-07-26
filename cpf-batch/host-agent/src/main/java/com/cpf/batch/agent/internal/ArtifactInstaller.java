package com.cpf.batch.agent.internal;

import com.cpf.batch.agent.AgentProperties;
import com.cpf.batch.api.AgentArtifactRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Objects;
import java.util.regex.Pattern;

public final class ArtifactInstaller {
    private static final Pattern VERSION = Pattern.compile("[A-Za-z0-9._-]{1,80}");
    private static final Pattern SHA256 = Pattern.compile("[A-Fa-f0-9]{64}");
    private static final Pattern GROUP_ID =
            Pattern.compile("[A-Za-z0-9_]+(?:\\.[A-Za-z0-9_-]+)*");
    private static final Pattern ARTIFACT_ID =
            Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,127}");
    private final AgentProperties properties;
    private final ArtifactVerifier verifier;

    public ArtifactInstaller(AgentProperties properties, ArtifactVerifier verifier) {
        this.properties = properties;
        this.verifier = verifier;
    }

    public Result install(AgentArtifactRequest request) throws Exception {
        Objects.requireNonNull(request, "request");
        AgentProperties.ServiceDefinition service = service(request.serviceId());
        Coordinate coordinate = parseCoordinate(request.coordinate());
        if (!coordinate.artifactId().equals(service.getArtifactId())) {
            throw new SecurityException("artifact/service mismatch");
        }
        String extension = artifactExtension(request.runtimeMode(), service.getRuntimeMode());
        if (!VERSION.matcher(request.version()).matches()) {
            throw new SecurityException("unsafe version");
        }
        if (request.sha256() == null || !SHA256.matcher(request.sha256()).matches()) {
            throw new SecurityException("invalid artifact checksum");
        }

        Path root = secureRoot(service.getInstallRoot());
        Path releases = secureDirectory(root, "releases");
        Path releaseDirectory = secureDirectory(releases, request.version());
        Path target = secureChild(releaseDirectory, service.getArtifactId() + extension);
        Path temporary = secureChild(releaseDirectory, service.getArtifactId() + extension + ".part");
        URI artifactUri = artifactUri(coordinate, request.version(), extension);

        try {
            download(artifactUri, temporary);
            verifier.verify(temporary, request.sha256(), request.signatureBase64());
            moveAtomically(temporary, target);
        } finally {
            Files.deleteIfExists(temporary);
        }

        if (request.configRef() != null && !request.configRef().isBlank()) {
            if (!request.configRef().matches("(?i)^(vault|secret|config)://[A-Za-z0-9._/@:-]+$")) {
                throw new SecurityException("Only approved secret/config references are allowed");
            }
            Path configDirectory = secureDirectory(root, "config");
            writeAtomically(configDirectory.resolve("deployment-config.ref"), request.configRef());
        }

        Path currentVersion = secureChild(root, "current.version");
        String previous = Files.isRegularFile(currentVersion, LinkOption.NOFOLLOW_LINKS)
                ? Files.readString(currentVersion).trim() : "";
        if (!previous.isBlank()) {
            writeAtomically(secureChild(root, "previous.version"), previous);
        }
        writeAtomically(currentVersion, request.version());
        return new Result(request.version(), previous, target.toString());
    }

    public String rollback(String serviceId) throws Exception {
        AgentProperties.ServiceDefinition service = service(serviceId);
        Path root = secureRoot(service.getInstallRoot());
        Path previousVersion = secureChild(root, "previous.version");
        if (!Files.isRegularFile(previousVersion, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalStateException("no rollback version");
        }
        String previous = Files.readString(previousVersion).trim();
        if (!VERSION.matcher(previous).matches()) {
            throw new SecurityException("invalid rollback version");
        }
        Path release = secureDirectory(secureDirectory(root, "releases"), previous);
        if (!Files.isDirectory(release, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalStateException("rollback release missing");
        }
        Path currentVersion = secureChild(root, "current.version");
        String current = Files.readString(currentVersion).trim();
        writeAtomically(currentVersion, previous);
        writeAtomically(previousVersion, current);
        return previous;
    }

    private void download(URI uri, Path target) throws Exception {
        HttpResponse<InputStream> response = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(30))
                .build()
                .send(HttpRequest.newBuilder(uri).timeout(Duration.ofMinutes(5)).GET().build(),
                        HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            try (InputStream ignored = response.body()) {
                throw new IOException("repository status=" + response.statusCode());
            }
        }
        long declaredLength = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
        if (declaredLength > properties.getMaxArtifactBytes()) {
            try (InputStream ignored = response.body()) {
                throw new SecurityException("artifact too large");
            }
        }
        try (InputStream input = response.body();
             var output = Files.newOutputStream(target, StandardOpenOption.CREATE,
                     StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            byte[] buffer = new byte[8192];
            long total = 0L;
            int read;
            while ((read = input.read(buffer)) >= 0) {
                total += read;
                if (total > properties.getMaxArtifactBytes()) {
                    throw new SecurityException("artifact too large");
                }
                output.write(buffer, 0, read);
            }
        }
    }

    private URI artifactUri(Coordinate coordinate, String version, String extension) {
        URI base = URI.create(Objects.requireNonNull(
                properties.getArtifactRepositoryBaseUrl(), "artifactRepositoryBaseUrl"));
        if (!base.isAbsolute() || base.getHost() == null || base.getUserInfo() != null
                || base.getQuery() != null || base.getFragment() != null) {
            throw new SecurityException("invalid artifact repository base URL");
        }
        URI repositoryRoot = URI.create(base.toString().replaceAll("/+$", "") + "/").normalize();
        URI uri = repositoryRoot.resolve(coordinate.groupId().replace('.', '/') + "/"
                + coordinate.artifactId() + "/" + version + "/"
                + coordinate.artifactId() + "-" + version + extension).normalize();
        if (!Objects.equals(base.getHost(), uri.getHost())
                || !Objects.equals(base.getScheme(), uri.getScheme())
                || base.getPort() != uri.getPort()
                || !uri.getPath().startsWith(repositoryRoot.getPath())) {
            throw new SecurityException("repository escape");
        }
        return uri;
    }

    static Coordinate parseCoordinate(String value) {
        if (value == null) {
            throw new SecurityException("coordinate is required");
        }
        String[] parts = value.split(":", -1);
        if (parts.length != 2 || !GROUP_ID.matcher(parts[0]).matches()
                || !ARTIFACT_ID.matcher(parts[1]).matches()) {
            throw new SecurityException("invalid artifact coordinate");
        }
        return new Coordinate(parts[0], parts[1]);
    }

    static String artifactExtension(String requestedMode, String approvedMode) {
        if (requestedMode == null || approvedMode == null
                || !requestedMode.equalsIgnoreCase(approvedMode)) {
            throw new SecurityException("runtime mode is not approved for service");
        }
        return switch (approvedMode.toLowerCase(java.util.Locale.ROOT)) {
            case "embedded-bootjar" -> ".jar";
            case "external-tomcat-war" -> ".war";
            default -> throw new SecurityException("unsupported host-agent runtime mode");
        };
    }

    private AgentProperties.ServiceDefinition service(String serviceId) {
        return properties.getServices().values().stream()
                .filter(candidate -> Objects.equals(serviceId, candidate.getServiceId()))
                .findFirst()
                .orElseThrow(() -> new SecurityException("service not approved"));
    }

    private static Path secureRoot(String configuredRoot) throws IOException {
        Path root = Path.of(Objects.requireNonNull(configuredRoot, "installRoot")).toAbsolutePath().normalize();
        if (Files.exists(root, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(root)) {
            throw new SecurityException("install root symlink is forbidden");
        }
        Files.createDirectories(root);
        return root.toRealPath(LinkOption.NOFOLLOW_LINKS);
    }

    private static Path secureDirectory(Path root, String child) throws IOException {
        Path candidate = secureChild(root, child);
        if (Files.exists(candidate, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(candidate)) {
            throw new SecurityException("sandbox directory symlink is forbidden");
        }
        Files.createDirectories(candidate);
        Path real = candidate.toRealPath(LinkOption.NOFOLLOW_LINKS);
        if (!real.startsWith(root)) {
            throw new SecurityException("path escape");
        }
        return real;
    }

    private static Path secureChild(Path root, String child) {
        Path candidate = root.resolve(child).normalize();
        if (!candidate.startsWith(root)) {
            throw new SecurityException("path escape");
        }
        if (Files.exists(candidate, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(candidate)) {
            throw new SecurityException("sandbox file symlink is forbidden");
        }
        return candidate;
    }

    private static void writeAtomically(Path target, String value) throws IOException {
        Path temporary = secureChild(target.getParent(), target.getFileName() + ".part");
        try {
            Files.writeString(temporary, value, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            moveAtomically(temporary, target);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException unsupported) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    record Coordinate(String groupId, String artifactId) {}
    public record Result(String version, String previousVersion, String artifactPath) {}
}
