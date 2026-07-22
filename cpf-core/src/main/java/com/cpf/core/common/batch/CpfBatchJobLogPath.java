package com.cpf.core.common.batch;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * BAT JobInstance 로그의 표준 상대 경로를 한 곳에서 계산합니다.
 */
public final class CpfBatchJobLogPath {
    private static final DateTimeFormatter BUSINESS_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private CpfBatchJobLogPath() {
    }

    public static Path relativePath(String jobName, long jobInstanceId, LocalDate businessDate) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("jobName은 필수입니다.");
        }
        if (jobInstanceId < 1) {
            throw new IllegalArgumentException("jobInstanceId는 1 이상이어야 합니다.");
        }
        if (businessDate == null) {
            throw new IllegalArgumentException("businessDate는 필수입니다.");
        }
        String safeJobName = sanitize(jobName);
        String date = BUSINESS_DATE_FORMATTER.format(businessDate);
        return Path.of(
                "bat",
                "jobs",
                date,
                safeJobName,
                "cpf-bat-" + safeJobName + '-' + jobInstanceId + '-' + date + ".log");
    }

    public static String sanitize(String value) {
        String sanitized = value.trim().replaceAll("[^A-Za-z0-9._-]", "_");
        if (sanitized.isBlank()
                || ".".equals(sanitized)
                || "..".equals(sanitized)
                || !sanitized.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,127}")) {
            throw new IllegalArgumentException("경로 식별자는 영문 또는 숫자로 시작하는 128자 이하 토큰이어야 합니다.");
        }
        return sanitized;
    }
}
