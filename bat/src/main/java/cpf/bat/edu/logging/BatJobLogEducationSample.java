package cpf.bat.edu.logging;

import cpf.pfw.common.batch.CpfBatchJobLogPath;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BAT JobInstance별 일자 로그 경로를 계산하는 교육 샘플입니다.
 *
 * <p>jobExecutionId는 재시작 회차를 식별하는 본문 필드이며 파일 분리 키가 아닙니다.
 * 같은 jobInstanceId를 넘기면 최초 실행과 재시작이 같은 파일에 이어집니다.</p>
 */
public class BatJobLogEducationSample {

    public Path logPath(Path cpfLogRoot, String jobName, long jobInstanceId, LocalDate businessDate) {
        return logPath(cpfLogRoot, "local", jobName, jobInstanceId, businessDate);
    }

    public Path logPath(
            Path cpfLogRoot,
            String environment,
            String jobName,
            long jobInstanceId,
            LocalDate businessDate) {
        // PFW 표준 경로 정책을 직접 호출해 운영 코드와 샘플의 규칙이 어긋나지 않게 합니다.
        Path relativePath = CpfBatchJobLogPath.relativePath(jobName, jobInstanceId, businessDate);
        // 환경 디렉터리를 먼저 결합해 local/dev/stg/prod 실행 결과가 섞이지 않게 합니다.
        return cpfLogRoot.toAbsolutePath().normalize()
                .resolve(environment.toLowerCase(java.util.Locale.ROOT))
                .resolve(relativePath)
                .normalize();
    }

    public boolean restartUsesSameFile(
            Path cpfLogRoot,
            String jobName,
            long jobInstanceId,
            LocalDate businessDate) {
        // 최초 실행과 재시작 모두 같은 JobInstance ID를 사용하면 표준 경로도 같아야 합니다.
        Path firstExecution = logPath(cpfLogRoot, jobName, jobInstanceId, businessDate);
        // restartAttempt와 jobExecutionId는 파일명이 아니라 JSON Lines 본문에서 실행 회차를 구분합니다.
        Path restartedExecution = logPath(cpfLogRoot, jobName, jobInstanceId, businessDate);
        return firstExecution.equals(restartedExecution);
    }

    public Map<String, Object> trackingFields(
            String transactionGlobalId,
            String segmentId,
            long jobInstanceId,
            long jobExecutionId) {
        if (transactionGlobalId == null || transactionGlobalId.isBlank()) {
            throw new IllegalArgumentException("transactionGlobalId는 필수입니다.");
        }
        if (segmentId == null || segmentId.isBlank()) {
            throw new IllegalArgumentException("segmentId는 필수입니다.");
        }
        // LinkedHashMap을 사용해 교육 출력에서도 필드 순서가 안정적으로 보이게 합니다.
        Map<String, Object> fields = new LinkedHashMap<>();
        // 글로벌 거래 ID는 온라인 호출, 스케줄러, 하위 step을 하나의 추적 축으로 연결합니다.
        fields.put("transactionGlobalId", transactionGlobalId);
        // segment ID는 Job 또는 Step 단위 처리 구간을 식별합니다.
        fields.put("segmentId", segmentId);
        // JobInstance ID는 BAT 로그 파일을 나누는 핵심 키입니다.
        fields.put("jobInstanceId", jobInstanceId);
        // JobExecution ID는 같은 JobInstance 안의 최초 실행과 재시작 회차를 구분합니다.
        fields.put("jobExecutionId", jobExecutionId);
        return fields;
    }
}
