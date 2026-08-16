package com.cpf.file.util;

import com.cpf.core.api.error.CpfValidationException;
import java.nio.file.Path;

/**
 * 파일명과 하위 경로를 안전하게 처리하는 File Capability Utility입니다.
 */
public final class CpfFiles {
    private CpfFiles() {
    }

    public static String safeFileName(String value) {
        if (value == null || value.isBlank()) {
            throw new CpfValidationException("fileName 값은 필수입니다.");
        }
        String name = value.trim();
        if (name.equals(".") || name.equals("..")
                || name.contains("/") || name.contains("\\")
                || name.indexOf('\0') >= 0) {
            throw new CpfValidationException("안전하지 않은 fileName 입니다.");
        }
        return name;
    }

    public static Path resolveChild(Path root, String fileName) {
        if (root == null) {
            throw new CpfValidationException("root 값은 필수입니다.");
        }
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path child = normalizedRoot.resolve(safeFileName(fileName)).normalize();
        if (!child.startsWith(normalizedRoot)) {
            throw new CpfValidationException("root 밖의 파일 경로는 허용되지 않습니다.");
        }
        return child;
    }
}
