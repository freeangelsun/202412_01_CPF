package com.cpf.batch.api;

import com.cpf.platform.operations.api.runtime.CpfInstanceIdentity;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * BAT JobInstance 파일 로그의 표준 상대 경로를 계산하는 공개 API입니다.
 *
 * <p>다중 인스턴스 환경에서 동일 JobInstance가 다른 서버에서 재시작될 수 있으므로
 * instanceId를 디렉터리와 파일명 축에 포함합니다. BAT/ADM/EDU는 Core 내부
 * Logging 구현을 직접 참조하지 않고 이 공개 규칙을 사용합니다.</p>
 */
public final class CpfBatchLogPaths {
    private static final DateTimeFormatter BUSINESS_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private CpfBatchLogPaths() {
    }

    public static Path relativePath(
            String jobName,
            long jobInstanceId,
            LocalDate businessDate,
            String instanceId) {
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
        String safeInstanceId = sanitize(instanceId);
        String date = BUSINESS_DATE_FORMATTER.format(businessDate);
        return Path.of(
                "bat",
                "jobs",
                date,
                safeJobName,
                safeInstanceId,
                "cpf-bat-" + safeJobName + '-' + jobInstanceId + '-' + safeInstanceId + '-' + date + ".log");
    }

    public static Path relativePath(String jobName, long jobInstanceId, LocalDate businessDate) {
        return relativePath(
                jobName,
                jobInstanceId,
                businessDate,
                CpfInstanceIdentity.current().instanceId());
    }

    public static String sanitize(String value) {
        if (value == null) {
            throw new IllegalArgumentException("경로 식별자는 필수입니다.");
        }
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
