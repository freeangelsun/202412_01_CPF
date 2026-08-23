package com.cpf.common.logging;

import java.nio.file.InvalidPathException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Boot 전에 Logging 오설정을 구체적인 property 오류로 차단합니다. */
public final class CpfApplicationLoggingPolicyValidator {
    private static final Pattern LOGICAL_NAME = Pattern.compile("[a-z][a-z0-9-]{0,63}");
    private static final Pattern FILE_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,119}\\.log");
    private static final Set<String> LEVELS = Set.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR");

    public CpfApplicationLoggingPolicy validate(CpfApplicationLoggingPolicy policy) {
        if (policy == null) throw new IllegalArgumentException("cpf.logging 설정이 필요합니다.");
        try {
            CpfRuntimeLogPathPolicy.resolveDirectory(
                    policy.root(), policy.applicationName(), policy.instanceId());
        } catch (InvalidPathException invalid) {
            throw new IllegalArgumentException("cpf.logging.root 경로 형식이 올바르지 않습니다: "
                    + policy.root(), invalid);
        }
        if (policy.files().isEmpty()) {
            throw new IllegalArgumentException("cpf.logging.files에는 하나 이상의 로그파일 정책이 필요합니다.");
        }
        Set<String> fileNames = new HashSet<>();
        boolean enabled = false;
        for (var entry : policy.files().entrySet()) {
            String logicalName = entry.getKey() == null ? "" : entry.getKey().trim();
            if (!LOGICAL_NAME.matcher(logicalName).matches()) {
                throw new IllegalArgumentException("cpf.logging.files logical-name 형식 오류: " + logicalName);
            }
            CpfLogFilePolicy file = entry.getValue();
            if (file == null) {
                throw new IllegalArgumentException("cpf.logging.files." + logicalName + " 설정이 필요합니다.");
            }
            if (!file.enabled()) continue;
            enabled = true;
            if (file.fileName() == null || !FILE_NAME.matcher(file.fileName().trim()).matches()) {
                throw new IllegalArgumentException("cpf.logging.files." + logicalName
                        + ".file-name은 하위경로가 없는 .log 파일명이어야 합니다: " + file.fileName());
            }
            String collisionKey = file.fileName().trim().toLowerCase(Locale.ROOT);
            if (!fileNames.add(collisionKey)) {
                throw new IllegalArgumentException("cpf.logging.files에 중복 file-name이 있습니다: "
                        + file.fileName());
            }
            if (file.rolling() != CpfLogFilePolicy.Rolling.DAILY) {
                throw new IllegalArgumentException("cpf.logging.files." + logicalName
                        + ".rolling은 DAILY여야 합니다: " + file.rolling());
            }
            if (file.compressAfterDays() < 0) {
                throw new IllegalArgumentException("cpf.logging.files." + logicalName
                        + ".compress-after-days는 0 이상이어야 합니다: " + file.compressAfterDays());
            }
            if (file.deleteAfterDays() <= file.compressAfterDays()) {
                throw new IllegalArgumentException("cpf.logging.files." + logicalName
                        + ".delete-after-days는 compress-after-days보다 커야 합니다: "
                        + file.deleteAfterDays());
            }
            if (file.level() != null && !LEVELS.contains(file.level())) {
                throw new IllegalArgumentException("cpf.logging.files." + logicalName
                        + ".level 허용값은 TRACE/DEBUG/INFO/WARN/ERROR입니다: " + file.level());
            }
        }
        if (!enabled) {
            throw new IllegalArgumentException("cpf.logging.files에는 enabled=true인 로그파일이 필요합니다.");
        }
        return policy;
    }
}
