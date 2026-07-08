package cpf.bat.logging;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * BAT Job 실행 단위 로그 파일 경로를 계산하는 정책 클래스입니다.
 *
 * <p>같은 Job이 여러 번 재시작되거나 병렬 실행될 때 로그가 섞이지 않도록
 * jobName, 업무일자, jobInstanceId, jobExecutionId 기준으로 파일을 분리합니다.</p>
 */
public class BatJobLogPathPolicy {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    public String buildJobExecutionLogPath(
            String basePath,
            String jobName,
            LocalDate businessDate,
            long jobInstanceId,
            long jobExecutionId
    ) {
        requireNonBlank(basePath, "basePath");
        requireNonBlank(jobName, "jobName");
        if (businessDate == null) {
            throw new IllegalArgumentException("businessDate는 필수입니다.");
        }
        if (jobInstanceId <= 0) {
            throw new IllegalArgumentException("jobInstanceId는 1 이상이어야 합니다.");
        }
        if (jobExecutionId <= 0) {
            throw new IllegalArgumentException("jobExecutionId는 1 이상이어야 합니다.");
        }

        return normalize(basePath)
                + "/jobs/" + sanitize(jobName)
                + "/" + DATE_FORMATTER.format(businessDate)
                + "/jobInstance-" + jobInstanceId
                + "/execution-" + jobExecutionId + ".log";
    }

    public String buildCenterCutLogPath(String basePath, String centerCutExecutionId, String fileName) {
        requireNonBlank(basePath, "basePath");
        requireNonBlank(centerCutExecutionId, "centerCutExecutionId");
        requireNonBlank(fileName, "fileName");

        return normalize(basePath)
                + "/centercut/" + sanitize(centerCutExecutionId)
                + "/" + sanitize(fileName);
    }

    private static String normalize(String value) {
        return value.replace("\\", "/").replaceAll("/+$", "");
    }

    private static String sanitize(String value) {
        return value.trim().replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static void requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + "는 필수입니다.");
        }
    }
}
