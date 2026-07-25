package com.cpf.batch.runtime;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.cpf.core.common.logging.ServerInstanceIdentity;

/**
 * BAT JobInstance 로그의 표준 상대 경로를 한 곳에서 계산합니다.
 */
public final class BatBatchJobLogPath {
    private static final DateTimeFormatter BUSINESS_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private BatBatchJobLogPath() {
    }

    /**
     * Job 로그의 인스턴스별 상대 경로를 생성합니다.
     *
     * <p>다중 인스턴스 환경에서 같은 JobInstance가 다른 서버에서 재시작될 수 있으므로
     * serverInstanceId를 디렉터리 축으로 포함해 파일만으로도 실행 위치를 식별합니다.</p>
     */
    public static Path relativePath(String jobName, long jobInstanceId, LocalDate businessDate, String serverInstanceId) {
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
        String safeServerInstanceId = sanitize(serverInstanceId);
        String date = BUSINESS_DATE_FORMATTER.format(businessDate);
        return Path.of(
                "bat",
                "jobs",
                date,
                safeJobName,
                safeServerInstanceId,
                "cpf-bat-" + safeJobName + '-' + jobInstanceId + '-' + safeServerInstanceId + '-' + date + ".log");
    }

    /**
     * 기존 호출부용 호환 overload입니다.
     *
     * <p>호출자가 serverInstanceId를 직접 전달하지 않더라도 현재 CPF 서버 인스턴스 식별자를
     * 사용하므로 생성되는 Job 로그 경로의 인스턴스 축은 생략되지 않습니다.</p>
     */
    public static Path relativePath(String jobName, long jobInstanceId, LocalDate businessDate) {
        return relativePath(jobName, jobInstanceId, businessDate, ServerInstanceIdentity.current().serverInstanceId());
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
