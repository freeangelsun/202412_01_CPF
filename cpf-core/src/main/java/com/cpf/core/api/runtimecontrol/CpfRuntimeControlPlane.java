package com.cpf.core.api.runtimecontrol;

/** Topology-independent Runtime Control Plane Public API입니다. */
public interface CpfRuntimeControlPlane extends CpfRuntimeAgentPort {
    CpfRuntimeChangeResult createChange(CpfRuntimeChangeCommand command);
    CpfRuntimeChangeResult getChange(String changeId);
    CpfRuntimeChangeResult getByOperationId(String operationId);
    CpfRuntimeChangeResult cancel(String changeId, String operationId, String reason, String operatorId);
    CpfRuntimeChangeResult rollback(String changeId, String operationId, String reason, String operatorId);
    CpfRuntimeGroupResult saveGroup(CpfRuntimeGroupCommand command);
    CpfRuntimeGroupResult changeGroupMember(CpfRuntimeGroupMemberCommand command);
    CpfRuntimeGroupResult getGroup(String groupId);
    void deleteGroup(String groupId, String operationId, Long expectedVersion, String reason, String operatorId);
    CpfRuntimeStatus status(String environment, String serviceId);
    CpfRuntimeControlHealth health();
    CpfRuntimeTargetPreview previewTargets(String changeType, int payloadSchemaVersion, CpfRuntimeTargetSelector target);
    CpfRuntimeChangePreview previewChange(CpfRuntimeChangeCommand command);
    CpfRuntimeAuditVerification verifyAudit(String changeId);
}
