package com.cpf.core.common.remotelog;

import com.cpf.core.common.logging.SensitiveDataMasker;
import com.cpf.core.common.logging.file.CpfFileLogWriter;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * 현재 실행 인스턴스의 allowlist 로그 root만 조회하는 adapter입니다.
 *
 * <p>API에는 절대경로를 노출하지 않으며, 다운로드 시에도 artifact ID를 다시 해석하고
 * 정규화·심볼릭 링크·확장자·크기 검사를 반복합니다.</p>
 */
public final class LocalCpfRemoteLogArtifactAdapter implements CpfRemoteLogArtifactPort {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".log", ".json", ".gz");
    private static final int MAX_PREVIEW_LINES = 1_000;

    private final CpfFileLogWriter fileLogWriter;
    private final long maxFileBytes;
    private final int retentionDays;

    public LocalCpfRemoteLogArtifactAdapter(CpfFileLogWriter fileLogWriter, Environment environment) {
        this.fileLogWriter = fileLogWriter;
        long configuredMb = environment.getProperty("cpf.remote-log.max-file-size-mb", Long.class, 50L);
        this.maxFileBytes = Math.max(1L, Math.min(configuredMb, 1_024L)) * 1_024L * 1_024L;
        this.retentionDays = Math.max(1, environment.getProperty(
                "cpf.logging.file.max-history-days", Integer.class, 30));
    }

    @Override
    public List<CpfRemoteLogArtifact> search(CpfRemoteLogArtifactSearch search) {
        CpfRemoteLogArtifactSearch condition = search == null
                ? new CpfRemoteLogArtifactSearch(null, null, null, null, null, null, null, 100)
                : search;
        Path root = root();
        if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) {
            return List.of();
        }
        List<CpfRemoteLogArtifact> result = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths.filter(Files::isRegularFile).sorted().toList()) {
                if (result.size() >= condition.limit() || !isAllowedFile(path)) {
                    continue;
                }
                CpfRemoteLogArtifact artifact = toArtifact(path);
                if (matches(artifact, condition) && containsIdentifiers(path, condition.contentIdentifiers())) {
                    result.add(artifact);
                }
            }
            return List.copyOf(result);
        } catch (IOException ex) {
            throw new IllegalStateException("로그 아티팩트 목록을 조회할 수 없습니다.", ex);
        }
    }

    @Override
    public CpfRemoteLogPreview preview(String artifactId, int lastLines, String keyword) {
        Path path = resolveArtifact(artifactId);
        int limit = Math.max(1, Math.min(lastLines <= 0 ? 200 : lastLines, MAX_PREVIEW_LINES));
        ArrayDeque<String> tail = new ArrayDeque<>(limit);
        int matchedCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(open(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (hasText(keyword) && !line.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT))) {
                    continue;
                }
                matchedCount++;
                if (tail.size() == limit) {
                    tail.removeFirst();
                }
                tail.addLast(SensitiveDataMasker.mask(line, 20_000));
            }
        } catch (IOException ex) {
            throw new IllegalStateException("로그 아티팩트 미리보기를 읽을 수 없습니다.", ex);
        }
        return new CpfRemoteLogPreview(
                toArtifact(path), List.copyOf(tail), tail.size(), matchedCount > tail.size(), keyword);
    }

    @Override
    public Path resolveDownload(String artifactId) {
        return resolveArtifact(artifactId);
    }

    private CpfRemoteLogArtifact toArtifact(Path path) {
        Path safe = validate(path);
        Path relative = root().relativize(safe);
        List<String> parts = new ArrayList<>();
        relative.forEach(part -> parts.add(part.toString()));
        String environment = value(parts, 0, fileLogWriter.environmentCode());
        String module = value(parts, 1, fileLogWriter.runtimeModuleCode()).toUpperCase(Locale.ROOT);
        String instance = value(parts, 2, fileLogWriter.instanceId());
        String fileName = safe.getFileName().toString();
        String logType = inferLogType(parts, fileName);
        try {
            long size = Files.size(safe);
            Instant modified = Files.getLastModifiedTime(safe, LinkOption.NOFOLLOW_LINKS).toInstant();
            boolean compressed = fileName.toLowerCase(Locale.ROOT).endsWith(".gz");
            boolean active = !compressed && modified.isAfter(Instant.now().minus(1, ChronoUnit.DAYS));
            String relativeText = relative.toString().replace('\\', '/');
            return new CpfRemoteLogArtifact(
                    encode(relativeText), environment, module, module, instance, logType, fileName,
                    relativeText, size, modified, compressed, checksum(safe), active,
                    "CPF_SENSITIVE_DATA_MASKER", size <= maxFileBytes,
                    modified.plus(retentionDays, ChronoUnit.DAYS), "ONLINE");
        } catch (IOException ex) {
            throw new IllegalStateException("로그 아티팩트 메타데이터를 읽을 수 없습니다.", ex);
        }
    }

    private Path resolveArtifact(String artifactId) {
        if (!hasText(artifactId)) {
            throw new IllegalArgumentException("로그 artifact ID는 필수입니다.");
        }
        String relative;
        try {
            relative = new String(Base64.getUrlDecoder().decode(artifactId), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("로그 artifact ID 형식이 올바르지 않습니다.", ex);
        }
        Path relativePath = Path.of(relative);
        if (relativePath.isAbsolute() || relativePath.normalize().startsWith("..")) {
            throw new IllegalArgumentException("로그 상대경로가 허용 범위를 벗어났습니다.");
        }
        return validate(root().resolve(relativePath));
    }

    private Path validate(Path path) {
        Path root = root();
        Path normalized = path.toAbsolutePath().normalize();
        if (!normalized.startsWith(root) || !Files.isRegularFile(normalized, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("허용된 로그 파일이 아닙니다.");
        }
        Path current = root;
        for (Path segment : root.relativize(normalized)) {
            current = current.resolve(segment);
            if (Files.isSymbolicLink(current)) {
                throw new IllegalArgumentException("심볼릭 링크 로그 경로는 허용하지 않습니다.");
            }
        }
        if (!isAllowedFile(normalized)) {
            throw new IllegalArgumentException("허용되지 않은 로그 확장자이거나 파일 크기입니다.");
        }
        return normalized;
    }

    private boolean isAllowedFile(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        boolean allowedExtension = ALLOWED_EXTENSIONS.stream().anyMatch(name::endsWith);
        try {
            return allowedExtension
                    && !Files.isSymbolicLink(path)
                    && Files.size(path) <= maxFileBytes;
        } catch (IOException ex) {
            return false;
        }
    }

    private boolean matches(CpfRemoteLogArtifact artifact, CpfRemoteLogArtifactSearch search) {
        return textMatches(artifact.environment(), search.environment())
                && textMatches(artifact.module(), search.module())
                && textMatches(artifact.service(), search.service())
                && textMatches(artifact.instance(), search.instance())
                && textMatches(artifact.logType(), search.logType())
                && textContains(artifact.fileName(), search.fileName())
                && (search.modifiedFrom() == null || !artifact.modifiedAt().isBefore(search.modifiedFrom()))
                && (search.modifiedTo() == null || !artifact.modifiedAt().isAfter(search.modifiedTo()))
                && (search.minSize() == null || artifact.size() >= search.minSize())
                && (search.maxSize() == null || artifact.size() <= search.maxSize())
                && (search.compressed() == null || artifact.compressed() == search.compressed())
                && (search.active() == null || artifact.active() == search.active());
    }

    private boolean containsIdentifiers(Path path, List<String> identifiers) {
        if (identifiers.isEmpty()) {
            return true;
        }
        Set<String> remaining = new LinkedHashSet<>(identifiers);
        String relativePath = root().relativize(path).toString();
        remaining.removeIf(relativePath::contains);
        if (remaining.isEmpty()) {
            return true;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(open(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null && !remaining.isEmpty()) {
                String current = line;
                remaining.removeIf(current::contains);
            }
            return remaining.isEmpty();
        } catch (IOException ex) {
            return false;
        }
    }

    private InputStream open(Path path) throws IOException {
        InputStream input = Files.newInputStream(path);
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".gz")
                ? new GZIPInputStream(input)
                : input;
    }

    private String checksum(Path path) {
        try (InputStream input = Files.newInputStream(path)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8_192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception ex) {
            throw new IllegalStateException("로그 checksum을 계산할 수 없습니다.", ex);
        }
    }

    private String inferLogType(List<String> parts, String fileName) {
        for (String part : parts) {
            String normalized = part.toLowerCase(Locale.ROOT);
            if (Set.of("transactions", "application", "error", "integration", "audit", "security", "batch", "recovery")
                    .contains(normalized)) {
                return normalized;
            }
        }
        String[] tokens = fileName.split("-");
        return tokens.length > 2 ? tokens[2].toLowerCase(Locale.ROOT) : "unknown";
    }

    private Path root() {
        return fileLogWriter.logRoot().toAbsolutePath().normalize();
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String value(List<String> values, int index, String fallback) {
        return values.size() > index ? values.get(index) : fallback;
    }

    private boolean textMatches(String actual, String expected) {
        return !hasText(expected) || expected.equalsIgnoreCase(actual);
    }

    private boolean textContains(String actual, String expected) {
        return !hasText(expected) || actual.toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
