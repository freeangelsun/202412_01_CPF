package cpf.pfw.common.batch;

/**
 * CPF 공통 배치 실행 요청 모델입니다.
 *
 * <p>ADM, BAT, EDU 같은 호출자는 Spring Batch의 {@code JobLauncher}를 직접 다루지 않고
 * 이 요청 모델을 통해 PFW 표준 실행 계층에 작업을 위임합니다.</p>
 *
 * @param jobId             배치 Job ID
 * @param scheduleId        스케줄 실행인 경우 스케줄 ID
 * @param sourceExecutionId 재수행/중지 기준이 되는 CPF 실행 ID
 * @param jobParameters     JSON 문자열 형태의 업무 파라미터
 * @param requestUser       요청자 ID
 * @param reason            운영 감사 사유
 * @param operationType     실행/스케줄 실행/재수행/중지 유형
 * @param lockRequired      동일 Job/파라미터 중복 실행 방지 여부
 */
public record CpfBatchExecutionRequest(
        String jobId,
        String scheduleId,
        Long sourceExecutionId,
        String jobParameters,
        String requestUser,
        String reason,
        CpfBatchOperationType operationType,
        boolean lockRequired) {

    public static CpfBatchExecutionRequest run(
            String jobId,
            String jobParameters,
            String requestUser,
            String reason) {
        return new CpfBatchExecutionRequest(
                jobId,
                null,
                null,
                jobParameters,
                requestUser,
                reason,
                CpfBatchOperationType.RUN,
                true);
    }

    public static CpfBatchExecutionRequest scheduledRun(
            String scheduleId,
            String jobId,
            String jobParameters,
            String requestUser,
            String reason) {
        return new CpfBatchExecutionRequest(
                jobId,
                scheduleId,
                null,
                jobParameters,
                requestUser,
                reason,
                CpfBatchOperationType.SCHEDULE_RUN,
                true);
    }

    public static CpfBatchExecutionRequest retry(
            long sourceExecutionId,
            String requestUser,
            String reason) {
        return new CpfBatchExecutionRequest(
                null,
                null,
                sourceExecutionId,
                null,
                requestUser,
                reason,
                CpfBatchOperationType.RETRY,
                true);
    }

    public static CpfBatchExecutionRequest stop(
            long sourceExecutionId,
            String requestUser,
            String reason) {
        return new CpfBatchExecutionRequest(
                null,
                null,
                sourceExecutionId,
                null,
                requestUser,
                reason,
                CpfBatchOperationType.STOP,
                false);
    }

    public String normalizedJobParameters() {
        return hasText(jobParameters) ? jobParameters.trim() : "{}";
    }

    public String normalizedRequestUser(String fallback) {
        return hasText(requestUser) ? requestUser.trim() : fallback;
    }

    public String normalizedReason(String fallback) {
        return hasText(reason) ? reason.trim() : fallback;
    }

    public String requiredJobId() {
        if (!hasText(jobId)) {
            throw new IllegalArgumentException("배치 Job ID는 필수입니다.");
        }
        return jobId.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
