package cpf.pfw.common.attachment;

import cpf.pfw.common.exception.CpfValidationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 허용된 로컬 root 아래에만 첨부파일을 저장하는 기본 adapter입니다.
 *
 * <p>파일명·확장자·크기·경로 이탈·심볼릭 링크를 검증하고 저장 직후 SHA-256을 계산합니다.
 * 운영에서는 같은 port를 object storage 또는 보안 파일 서버 adapter로 교체할 수 있습니다.</p>
 */
public class LocalCpfAttachmentStorageAdapter implements CpfAttachmentStoragePort {
    private final Path root;
    private final long maxBytes;
    private final Set<String> allowedExtensions;
    private final Clock clock;

    public LocalCpfAttachmentStorageAdapter(Path root, long maxBytes, Set<String> allowedExtensions) {
        this(root, maxBytes, allowedExtensions, Clock.systemUTC());
    }

    LocalCpfAttachmentStorageAdapter(Path root, long maxBytes, Set<String> allowedExtensions, Clock clock) {
        if (root == null) {
            throw new IllegalArgumentException("첨부파일 저장 root는 필수입니다.");
        }
        if (maxBytes < 1) {
            throw new IllegalArgumentException("첨부파일 최대 크기는 1바이트 이상이어야 합니다.");
        }
        this.root = root.toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
        this.allowedExtensions = allowedExtensions.stream()
                .map(value -> value.toLowerCase(Locale.ROOT).replace(".", ""))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        this.clock = clock;
    }

    @Override
    public CpfStoredAttachment store(String groupId, String originalFileName, String contentType, byte[] content) {
        String safeGroup = safeSegment(groupId, "groupId");
        String safeName = safeFileName(originalFileName);
        byte[] bytes = content == null ? new byte[0] : content.clone();
        if (bytes.length == 0 || bytes.length > maxBytes) {
            throw new CpfValidationException("첨부파일 크기가 허용 범위를 벗어났습니다. size=" + bytes.length);
        }
        String extension = extension(safeName);
        if (!allowedExtensions.contains(extension)) {
            throw new CpfValidationException("허용되지 않은 첨부파일 확장자입니다. extension=" + extension);
        }

        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path groupRoot = resolveSafe(root.resolve(safeGroup));
        Path target = resolveSafe(groupRoot.resolve(storedName));
        try {
            Files.createDirectories(groupRoot);
            rejectSymbolicLink(root);
            rejectSymbolicLink(groupRoot);
            Files.write(target, bytes);
            String checksum = sha256(bytes);
            String storageKey = root.relativize(target).toString().replace('\\', '/');
            return new CpfStoredAttachment(
                    storageKey,
                    safeName,
                    storedName,
                    normalizeContentType(contentType),
                    bytes.length,
                    checksum,
                    Instant.now(clock));
        } catch (IOException ex) {
            throw new IllegalStateException("첨부파일 저장에 실패했습니다.", ex);
        }
    }

    @Override
    public CpfAttachmentContent read(String storageKey) {
        Path target = resolveSafe(root.resolve(required(storageKey, "storageKey")));
        rejectSymbolicLink(target);
        try {
            byte[] bytes = Files.readAllBytes(target);
            if (bytes.length > maxBytes) {
                throw new CpfValidationException("첨부파일 읽기 크기가 허용 범위를 벗어났습니다.");
            }
            return new CpfAttachmentContent(bytes, sha256(bytes));
        } catch (IOException ex) {
            throw new IllegalStateException("첨부파일 읽기에 실패했습니다.", ex);
        }
    }

    @Override
    public void delete(String storageKey) {
        Path target = resolveSafe(root.resolve(required(storageKey, "storageKey")));
        rejectSymbolicLink(target);
        try {
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            throw new IllegalStateException("첨부파일 삭제에 실패했습니다.", ex);
        }
    }

    private Path resolveSafe(Path candidate) {
        Path normalized = candidate.toAbsolutePath().normalize();
        if (!normalized.startsWith(root)) {
            throw new CpfValidationException("첨부파일 저장 경로가 허용 root를 벗어났습니다.");
        }
        return normalized;
    }

    private void rejectSymbolicLink(Path path) {
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(path)) {
            throw new CpfValidationException("심볼릭 링크 경로에는 첨부파일을 저장하거나 읽을 수 없습니다.");
        }
    }

    private String safeFileName(String value) {
        String name = required(value, "originalFileName");
        if (!Path.of(name).getFileName().toString().equals(name)
                || name.contains("..")
                || name.chars().anyMatch(Character::isISOControl)) {
            throw new CpfValidationException("첨부파일 이름이 안전하지 않습니다.");
        }
        if (name.length() > 255) {
            throw new CpfValidationException("첨부파일 이름은 255자를 초과할 수 없습니다.");
        }
        return name;
    }

    private String safeSegment(String value, String field) {
        String segment = required(value, field);
        if (!segment.matches("[A-Za-z0-9_-]{1,80}")) {
            throw new CpfValidationException(field + "는 영문·숫자·밑줄·하이픈 80자 이하여야 합니다.");
        }
        return segment;
    }

    private String extension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 1 || index == fileName.length() - 1) {
            throw new CpfValidationException("첨부파일 확장자가 필요합니다.");
        }
        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeContentType(String value) {
        String contentType = value == null || value.isBlank() ? "application/octet-stream" : value.trim();
        if (contentType.length() > 120 || contentType.contains("\r") || contentType.contains("\n")) {
            throw new CpfValidationException("첨부파일 Content-Type이 올바르지 않습니다.");
        }
        return contentType;
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new CpfValidationException(field + " 값은 필수입니다.");
        }
        return value.trim();
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", ex);
        }
    }
}
