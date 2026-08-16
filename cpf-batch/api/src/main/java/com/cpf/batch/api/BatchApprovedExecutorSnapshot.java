package com.cpf.batch.api;

import java.util.Objects;

/**
 * 외부 Service/Message/Protocol Step이 사용하는 최소 불변 승인 Snapshot입니다.
 * 전체 Definition 객체나 임의 Map을 전달하지 않고 승인 시점의 실행 결합값만 전달합니다.
 */
public record BatchApprovedExecutorSnapshot(
        String jobId,
        long definitionVersion,
        BatchJobDefinition.ExecutorType executorType,
        String executorReference,
        String definitionChecksum,
        long timeoutSeconds,
        int maxAttempts) {
    public BatchApprovedExecutorSnapshot {
        jobId = required(jobId, "jobId");
        if (definitionVersion <= 0) throw new IllegalArgumentException("definitionVersion must be positive");
        executorType = Objects.requireNonNull(executorType, "executorType");
        executorReference = required(executorReference, "executorReference");
        definitionChecksum = required(definitionChecksum, "definitionChecksum");
        if (timeoutSeconds <= 0) throw new IllegalArgumentException("timeoutSeconds must be positive");
        if (maxAttempts <= 0) throw new IllegalArgumentException("maxAttempts must be positive");
    }

    public static BatchApprovedExecutorSnapshot from(BatchJobDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        if (definition.state() != BatchJobDefinition.State.PUBLISHED) {
            throw new IllegalArgumentException("Published definition is required");
        }
        return new BatchApprovedExecutorSnapshot(
                definition.jobId(), definition.definitionVersion(), definition.executorType(),
                definition.executorReference(), definition.checksum(),
                definition.resourcePolicy().timeoutSeconds(), definition.recoveryPolicy().maxAttempts());
    }

    public void assertStepBinding(BatchStepDefinition step) {
        Objects.requireNonNull(step, "step");
        if (executorType != step.executorType() || !executorReference.equals(step.executorReference())) {
            throw new SecurityException("Approved executor snapshot does not match step binding");
        }
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value.trim();
    }
}
