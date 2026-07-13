package cpf.bat.logging;

import cpf.pfw.common.batch.CpfBatchJobLogPath;

import java.nio.file.Path;
import java.time.LocalDate;

/**
 * BAT Job 실행 단위 로그 파일 경로를 계산하는 정책 클래스입니다.
 *
 * <p>같은 JobInstance의 재시작 로그는 같은 파일에 이어지고 서로 다른 JobInstance는
 * 분리되도록 jobName, 업무일자, jobInstanceId만 파일 분리 키로 사용합니다.</p>
 */
public class BatJobLogPathPolicy {
    public String buildJobInstanceLogPath(
            String basePath,
            String jobName,
            LocalDate businessDate,
            long jobInstanceId
    ) {
        requireNonBlank(basePath, "basePath");
        Path root = Path.of(basePath).toAbsolutePath().normalize();
        return root.resolve(CpfBatchJobLogPath.relativePath(jobName, jobInstanceId, businessDate))
                .normalize()
                .toString()
                .replace('\\', '/');
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
