package com.cpf.batch.api;

/** 동일 JVM과 분리 WAS에서 공통으로 사용하는 BAT Job Definition Control Port입니다. */
public interface BatchJobDefinitionControlPort {
    DefinitionState state(String jobId, long definitionVersion);
    PublishResult publishApproved(PublishCommand command);

    record DefinitionState(
            String jobId, long definitionVersion, String state, long rowVersion,
            String checksum, String requestedBy) {}

    record PublishCommand(
            String operationId, String jobId, long definitionVersion, long expectedRowVersion,
            long approvalRequestId, String payloadHash, String requestedBy,
            String approvedBy, String reason) {}

    record PublishResult(
            String jobId, long definitionVersion, String state, long rowVersion,
            String checksum, String operationId) {}
}
