package cpf.pfw.common.remotelog;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

/** 로그 아티팩트 목록의 인스턴스·실행 식별자·파일 메타데이터 검색 조건입니다. */
public record CpfRemoteLogArtifactSearch(
        String environment,
        String module,
        String service,
        String instance,
        String logType,
        String fileName,
        String standardTransactionId,
        String standardBatchId,
        String transactionGlobalId,
        String transactionId,
        String segmentId,
        String jobInstanceId,
        String jobExecutionId,
        String stepExecutionId,
        String schedulerId,
        Instant modifiedFrom,
        Instant modifiedTo,
        Long minSize,
        Long maxSize,
        Boolean compressed,
        Boolean active,
        int limit) {

    public CpfRemoteLogArtifactSearch {
        limit = limit < 1 ? 100 : Math.min(limit, 500);
        minSize = minSize == null ? null : Math.max(0L, minSize);
        maxSize = maxSize == null ? null : Math.max(0L, maxSize);
        if (minSize != null && maxSize != null && minSize > maxSize) {
            throw new IllegalArgumentException("로그 파일 최소 크기는 최대 크기보다 클 수 없습니다.");
        }
        if (modifiedFrom != null && modifiedTo != null && modifiedFrom.isAfter(modifiedTo)) {
            throw new IllegalArgumentException("로그 수정 시작 시각은 종료 시각보다 늦을 수 없습니다.");
        }
    }

    /** 초기 원격 로그 API와 source 호환을 유지하는 축약 생성자입니다. */
    public CpfRemoteLogArtifactSearch(
            String environment,
            String module,
            String instance,
            String logType,
            String fileName,
            String transactionGlobalId,
            Boolean active,
            int limit) {
        this(environment, module, null, instance, logType, fileName,
                null, null, transactionGlobalId, null, null,
                null, null, null, null,
                null, null, null, null, null, active, limit);
    }

    public List<String> contentIdentifiers() {
        return Stream.of(
                        standardTransactionId,
                        standardBatchId,
                        transactionGlobalId,
                        transactionId,
                        segmentId,
                        jobInstanceId,
                        jobExecutionId,
                        stepExecutionId,
                        schedulerId)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }
}
