package com.cpf.starter.attachment;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Web multipart 입력을 storage port 호출 전에 제한하는 안전 정책입니다. */
public final class CpfAttachmentUploadPolicy {
    public static final long DEFAULT_MAX_BYTES = 10L * 1024L * 1024L;
    private final long maxBytes;
    private final Set<String> allowedContentTypes;
    private final Set<String> allowedExtensions;

    public CpfAttachmentUploadPolicy(long maxBytes, Set<String> allowedContentTypes, Set<String> allowedExtensions) {
        if (maxBytes < 1 || maxBytes > 100L * 1024L * 1024L) throw new IllegalArgumentException("maxBytes는 1~104857600 범위여야 합니다.");
        this.maxBytes = maxBytes;
        this.allowedContentTypes = normalize(allowedContentTypes);
        this.allowedExtensions = normalize(allowedExtensions);
    }

    public UploadMetadata validate(String originalFilename, String contentType, long size) {
        String name = Objects.requireNonNull(originalFilename, "originalFilename").trim();
        if (name.isEmpty() || name.length() > 255 || name.contains("..") || name.contains("/") || name.contains("\\") || name.indexOf('\0') >= 0)
            throw new IllegalArgumentException("안전하지 않은 첨부 파일명입니다.");
        if (size < 0 || size > maxBytes) throw new IllegalArgumentException("첨부 크기 제한을 초과했습니다: " + size);
        String type = contentType == null ? "application/octet-stream" : contentType.trim().toLowerCase(Locale.ROOT);
        if (!allowedContentTypes.isEmpty() && !allowedContentTypes.contains(type)) throw new IllegalArgumentException("허용되지 않은 content-type: " + type);
        int dot = name.lastIndexOf('.');
        String ext = dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!allowedExtensions.isEmpty() && !allowedExtensions.contains(ext)) throw new IllegalArgumentException("허용되지 않은 첨부 확장자: " + ext);
        return new UploadMetadata(name, type, size);
    }

    public long maxBytes() { return maxBytes; }
    public Set<String> allowedContentTypes() { return allowedContentTypes; }
    public Set<String> allowedExtensions() { return allowedExtensions; }

    private static Set<String> normalize(Set<String> source) {
        if (source == null || source.isEmpty()) return Set.of();
        var result = new LinkedHashSet<String>();
        for (String value : source) if (value != null && !value.isBlank()) result.add(value.trim().toLowerCase(Locale.ROOT));
        return Set.copyOf(result);
    }
    public record UploadMetadata(String filename, String contentType, long size) { }
}
