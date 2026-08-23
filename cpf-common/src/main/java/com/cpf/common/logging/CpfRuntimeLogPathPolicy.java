package com.cpf.common.logging;

import java.nio.file.Path;
import java.util.regex.Pattern;

/** Application/Instance별 로그 디렉터리를 안전하게 분리하는 공통 경로 정책입니다. */
public final class CpfRuntimeLogPathPolicy {
    private static final Pattern SEGMENT = Pattern.compile("[\\p{L}\\p{N}._-]{1,100}");

    private CpfRuntimeLogPathPolicy() { }

    public static Path resolveDirectory(Path root, String applicationName, String instanceId) {
        if (root == null) throw new IllegalArgumentException("cpf.logging.root 값이 필요합니다.");
        String application = requireSegment(applicationName, "spring.application.name");
        String instance = requireSegment(instanceId, "cpf.logging.instance-id");
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path resolved = normalizedRoot.resolve(application).resolve(instance).normalize();
        if (!resolved.startsWith(normalizedRoot)) {
            throw new IllegalArgumentException("cpf.logging.root 밖의 로그 경로는 허용되지 않습니다.");
        }
        return resolved;
    }

    static String requireSegment(String value, String property) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.equals(".") || normalized.equals("..") || !SEGMENT.matcher(normalized).matches()) {
            throw new IllegalArgumentException(property
                    + " 값은 1~100자의 문자/숫자/점/밑줄/하이픈만 허용합니다: " + normalized);
        }
        return normalized;
    }
}
