package cpf.pfw.common.batch;

/**
 * 실행 중인 배치의 진행 상태를 CPF 운영 메타에 반영하기 위한 값 객체입니다.
 *
 * <p>기존 DB에는 read/write/skip 중심으로 저장하고, 확장 칼럼이 있는 DB에서는
 * total, processed, progressRate, tps 같은 관제용 지표까지 함께 저장합니다.</p>
 */
public record CpfBatchRuntimeProgress(
        long totalCount,
        long processedCount,
        long successCount,
        long failureCount,
        long skipCount,
        long retryCount,
        double progressRate,
        double tps,
        long avgElapsedMs,
        long maxElapsedMs,
        String currentStepName,
        String status,
        String stepLog) {

    public static CpfBatchRuntimeProgress of(
            long totalCount,
            long processedCount,
            long successCount,
            long failureCount,
            long skipCount,
            long retryCount,
            long elapsedMs,
            String currentStepName,
            String status,
            String stepLog) {
        long safeTotal = Math.max(0, totalCount);
        long safeProcessed = Math.max(0, processedCount);
        double progressRate = safeTotal == 0
                ? terminalRate(status)
                : Math.min(100.0d, (safeProcessed * 100.0d) / safeTotal);
        double tps = elapsedMs > 0 ? safeProcessed * 1000.0d / elapsedMs : 0.0d;
        long avgElapsedMs = safeProcessed > 0 && elapsedMs > 0 ? elapsedMs / safeProcessed : 0;
        return new CpfBatchRuntimeProgress(
                safeTotal,
                safeProcessed,
                Math.max(0, successCount),
                Math.max(0, failureCount),
                Math.max(0, skipCount),
                Math.max(0, retryCount),
                round2(progressRate),
                round4(tps),
                avgElapsedMs,
                Math.max(0, elapsedMs),
                blankToNull(currentStepName),
                defaultIfBlank(status, "RUNNING"),
                blankToNull(stepLog));
    }

    public static CpfBatchRuntimeProgress empty(String status) {
        return of(0, 0, 0, 0, 0, 0, 0, null, status, null);
    }

    private static double terminalRate(String status) {
        String normalized = defaultIfBlank(status, "").toUpperCase();
        return ("COMPLETED".equals(normalized) || "FAILED".equals(normalized) || "STOPPED".equals(normalized))
                ? 100.0d
                : 0.0d;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0d) / 100.0d;
    }

    private static double round4(double value) {
        return Math.round(value * 10000.0d) / 10000.0d;
    }

    private static String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
