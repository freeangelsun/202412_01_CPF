package com.cpf.platform.operations.runtimecontrol;

/** Topology-independent Runtime Control Plane Public API입니다. */
public interface CpfRuntimeControlPlane extends CpfRuntimeAgentPort {
    CpfRuntimeChangeResult createChange(CpfRuntimeChangeCommand command);
    CpfRuntimeChangeResult getChange(String changeId);
    CpfRuntimeChangeResult getByCommandId(String commandId);
    CpfRuntimeChangeResult cancel(String changeId, String commandId, String reason, String operatorId);
    CpfRuntimeChangeResult rollback(String changeId, String commandId, String reason, String operatorId);
    CpfRuntimeGroupResult saveGroup(CpfRuntimeGroupCommand command);
    CpfRuntimeGroupResult changeGroupMember(CpfRuntimeGroupMemberCommand command);
    CpfRuntimeGroupResult getGroup(String groupId);
    void deleteGroup(String groupId, String commandId, Long expectedVersion, String reason, String operatorId);
    CpfRuntimeStatus status(String environment, String serviceId);
    CpfRuntimeControlHealth health();
    CpfRuntimeTargetPreview previewTargets(String changeType, int payloadSchemaVersion, CpfRuntimeTargetSelector target);
    CpfRuntimeChangePreview previewChange(CpfRuntimeChangeCommand command);
    CpfRuntimeAuditVerification verifyAudit(String changeId);
    java.util.List<CpfManagedServerSnapshot> findManagedServers(String environment, String status, String keyword, int limit);
    CpfManagedServerSnapshot getManagedServer(String managedServerId);
    CpfManagedServerSnapshot saveManagedServer(CpfManagedServerCommand command);
    void disableManagedServer(String managedServerId, long expectedVersion, String reason, String operatorId);
    java.util.List<CpfRuntimeInventorySnapshot> findRuntimeInventory(String environment, String capability, String status, String keyword, int limit);
}
