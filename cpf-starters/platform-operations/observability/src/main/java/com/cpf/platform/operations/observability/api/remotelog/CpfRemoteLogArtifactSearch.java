package com.cpf.platform.operations.observability.api.remotelog;

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

    private static final int MAX_SELECTOR_LENGTH = 200;
    private static final int MAX_FILE_NAME_LENGTH = 255;

    public CpfRemoteLogArtifactSearch {
        environment = optional(environment, "environment", 100);
        module = optional(module, "module", 100);
        service = optional(service, "service", 100);
        instance = optional(instance, "instance", MAX_SELECTOR_LENGTH);
        logType = optional(logType, "logType", 100);
        fileName = optionalFileName(fileName);
        standardTransactionId = optional(standardTransactionId, "standardTransactionId", MAX_SELECTOR_LENGTH);
        standardBatchId = optional(standardBatchId, "standardBatchId", MAX_SELECTOR_LENGTH);
        transactionId = optional(transactionId, "transactionId", MAX_SELECTOR_LENGTH);
        segmentId = optional(segmentId, "segmentId", MAX_SELECTOR_LENGTH);
        jobInstanceId = optional(jobInstanceId, "jobInstanceId", MAX_SELECTOR_LENGTH);
        jobExecutionId = optional(jobExecutionId, "jobExecutionId", MAX_SELECTOR_LENGTH);
        stepExecutionId = optional(stepExecutionId, "stepExecutionId", MAX_SELECTOR_LENGTH);
        schedulerId = optional(schedulerId, "schedulerId", MAX_SELECTOR_LENGTH);
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
            String transactionId,
            Boolean active,
            int limit) {
        this(environment, module, null, instance, logType, fileName,
                null, null, transactionId, null,
                null, null, null, null,
                null, null, null, null, null, active, limit);
    }

    /** contentIdentifiers 작업을 CPF 표준 계약에 따라 수행한다. */
    public List<String> contentIdentifiers() {
        return Stream.of(
                        standardTransactionId,
                        standardBatchId,
                        transactionId,
                        segmentId,
                        jobInstanceId,
                        jobExecutionId,
                        stepExecutionId,
                        schedulerId)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList();
    }

    private static String optional(String value, String name, int maximumLength) {
        if (value == null) return null;
        String normalized = value.trim();
        if (normalized.isEmpty()) return null;
        if (normalized.length() > maximumLength || containsControlCharacter(normalized)) {
            throw new IllegalArgumentException(name + " is invalid");
        }
        return normalized;
    }

    private static String optionalFileName(String value) {
        String normalized = optional(value, "fileName", MAX_FILE_NAME_LENGTH);
        if (normalized == null) return null;
        if (normalized.contains("/") || normalized.contains("\\")
                || ".".equals(normalized) || "..".equals(normalized)) {
            throw new IllegalArgumentException("fileName must be a safe name fragment");
        }
        return normalized;
    }

    private static boolean containsControlCharacter(String value) {
        return value.codePoints().anyMatch(Character::isISOControl);
    }
}
