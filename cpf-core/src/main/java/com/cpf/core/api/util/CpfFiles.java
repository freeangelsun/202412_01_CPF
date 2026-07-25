package com.cpf.core.api.util;

import java.nio.file.Path;

/** Path Traversal을 줄이는 파일명/하위경로 편의 API입니다. */
public final class CpfFiles {
    private CpfFiles() {}
    public static String safeFileName(String value) {
        String fileName = CpfStrings.requireText(value, "fileName").replace('\\', '/');
        if (fileName.contains("/") || fileName.equals(".") || fileName.equals("..")) {
            throw new IllegalArgumentException("경로가 포함된 fileName은 허용하지 않습니다.");
        }
        return fileName;
    }
    public static String extension(String value) {
        String fileName = safeFileName(value);
        int index = fileName.lastIndexOf('.');
        return index <= 0 || index == fileName.length() - 1 ? "" : fileName.substring(index + 1).toLowerCase();
    }
    public static Path resolveChild(Path root, String child) {
        Path base = root.toAbsolutePath().normalize();
        Path resolved = base.resolve(child).normalize();
        if (!resolved.startsWith(base)) throw new IllegalArgumentException("허용 Root 밖의 경로는 사용할 수 없습니다.");
        return resolved;
    }
}
