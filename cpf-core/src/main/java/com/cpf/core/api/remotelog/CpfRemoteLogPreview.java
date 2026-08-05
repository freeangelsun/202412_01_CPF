package com.cpf.core.api.remotelog;

import com.cpf.core.api.security.CpfSensitiveData;

import java.util.List;

/** 마스킹을 적용한 로그 미리보기입니다. */
public record CpfRemoteLogPreview(
        CpfRemoteLogArtifact artifact,
        List<String> lines,
        int returnedLineCount,
        boolean truncated,
        String keyword) {

    private static final int MAX_PREVIEW_LINES = 5_000;
    private static final int MAX_PREVIEW_LINE_LENGTH = 16_384;
    private static final int MAX_KEYWORD_LENGTH = 500;
    private static final String TRUNCATED_SUFFIX = "...[TRUNCATED]";

    public CpfRemoteLogPreview {
        if (artifact == null) throw new IllegalArgumentException("artifact is required");
        if (lines == null) lines = List.of();
        if (lines.size() > MAX_PREVIEW_LINES) {
            throw new IllegalArgumentException("preview line count exceeds the safe response limit");
        }
        lines = lines.stream()
                .map(CpfRemoteLogPreview::sanitizeLine)
                .toList();
        if (returnedLineCount != lines.size()) {
            throw new IllegalArgumentException("returnedLineCount must match the returned lines");
        }
        keyword = sanitizeKeyword(keyword);
    }

    private static String sanitizeLine(String value) {
        String sanitized = CpfSensitiveData.sanitizeAuditText(value == null ? "" : value);
        if (sanitized.length() <= MAX_PREVIEW_LINE_LENGTH) return sanitized;
        int prefixLength = MAX_PREVIEW_LINE_LENGTH - TRUNCATED_SUFFIX.length();
        return sanitized.substring(0, prefixLength) + TRUNCATED_SUFFIX;
    }

    private static String sanitizeKeyword(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        if (normalized.isEmpty()) return null;
        if (normalized.length() > MAX_KEYWORD_LENGTH
                || normalized.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("keyword is invalid");
        }
        return CpfSensitiveData.sanitizeAuditText(normalized);
    }
}
