package com.cpf.starter.sftp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/** Internal fail-closed local/remote root policy for the CPF SFTP capability. */
final class CpfSftpPathPolicy {
    private final Path localRoot;
    private final String remoteRoot;

    CpfSftpPathPolicy(String configuredLocalRoot, String configuredRemoteRoot) {
        try {
            Path configured = Path.of(Objects.requireNonNull(
                    configuredLocalRoot, "configuredLocalRoot"))
                    .toAbsolutePath().normalize();
            if (!Files.isDirectory(configured, LinkOption.NOFOLLOW_LINKS)) {
                throw new IllegalStateException(
                        "SFTP local-root must be an existing non-symbolic-link directory: " + configured);
            }
            localRoot = configured.toRealPath(LinkOption.NOFOLLOW_LINKS);
        } catch (IOException exception) {
            throw new IllegalStateException("SFTP local-root cannot be resolved", exception);
        }
        remoteRoot = normalizeRemote(Objects.requireNonNull(
                configuredRemoteRoot, "configuredRemoteRoot"));
    }

    Path existingLocalFile(Path value) {
        Path candidate = lexicalLocal(value);
        try {
            Path real = candidate.toRealPath();
            if (!real.startsWith(localRoot) || !Files.isRegularFile(real)) {
                throw new SecurityException("SFTP local file escapes configured root");
            }
            return real;
        } catch (IOException exception) {
            throw new IllegalArgumentException("SFTP local file does not exist", exception);
        }
    }

    Path localTarget(Path value) {
        Path candidate = lexicalLocal(value);
        Path parent = candidate.getParent();
        if (parent == null) {
            throw new SecurityException("SFTP local target has no parent");
        }
        try {
            Files.createDirectories(parent);
            Path realParent = parent.toRealPath();
            if (!realParent.startsWith(localRoot)) {
                throw new SecurityException("SFTP local target escapes configured root");
            }
            Path target = realParent.resolve(candidate.getFileName()).normalize();
            if (!target.startsWith(localRoot)) {
                throw new SecurityException("SFTP local target escapes configured root");
            }
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)
                    && Files.isSymbolicLink(target)) {
                throw new SecurityException("SFTP local target symbolic link is forbidden");
            }
            return target;
        } catch (IOException exception) {
            throw new IllegalStateException("SFTP local target parent cannot be resolved", exception);
        }
    }

    String remote(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SFTP remote path is required");
        }
        String supplied = value.replace('\\', '/');
        String candidate = supplied.startsWith("/")
                ? normalizeRemote(supplied)
                : normalizeRemote(remoteRoot + "/" + supplied);
        if (!candidate.equals(remoteRoot)
                && !candidate.startsWith(remoteRoot + "/")) {
            throw new SecurityException("SFTP remote path escapes configured root");
        }
        return candidate;
    }

    private Path lexicalLocal(Path value) {
        if (value == null) {
            throw new IllegalArgumentException("SFTP local path is required");
        }
        Path candidate = value.isAbsolute()
                ? value.toAbsolutePath().normalize()
                : localRoot.resolve(value).normalize();
        if (!candidate.startsWith(localRoot)) {
            throw new SecurityException("SFTP local path escapes configured root");
        }
        return candidate;
    }

    private static String normalizeRemote(String value) {
        String source = value.trim().replace('\\', '/');
        if (source.isEmpty()) {
            throw new IllegalArgumentException("SFTP remote-root is required");
        }
        Deque<String> segments = new ArrayDeque<>();
        for (String segment : source.split("/+")) {
            if (segment.isEmpty() || ".".equals(segment)) {
                continue;
            }
            if ("..".equals(segment)) {
                if (segments.isEmpty()) {
                    throw new SecurityException("SFTP remote path escapes root");
                }
                segments.removeLast();
                continue;
            }
            if (segment.indexOf('\0') >= 0 || segment.contains(":")) {
                throw new IllegalArgumentException("SFTP remote path contains an unsafe segment");
            }
            segments.addLast(segment);
        }
        return segments.isEmpty() ? "/" : "/" + String.join("/", segments);
    }
}
