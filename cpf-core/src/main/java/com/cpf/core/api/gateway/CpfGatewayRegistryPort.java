package com.cpf.core.api.gateway;

import java.time.OffsetDateTime;
import java.util.List;

/** Gateway Control Plane이 Server Group·Binding·Apply ACK·Connection Test를 관리하는 공개 Port입니다. */
public interface CpfGatewayRegistryPort {
    List<ServerGroup> findServerGroups(String environmentCode, String serviceId, String status, int limit);
    List<GroupMember> findMembers(String serverGroupId);
    List<GatewayBinding> findBindings(String environmentCode, String routeId, String status, int limit);
    List<ApplyStatus> findApplyStatuses(String bindingId, int limit);
    List<ConnectionTestResult> findConnectionTests(String bindingId, int limit);

    MutationResult saveServerGroup(ServerGroupCommand command);
    MutationResult saveBinding(GatewayBindingCommand command);
    MutationResult changeBindingState(BindingStateCommand command);
    void deleteServerGroup(String serverGroupId, DeleteCommand command);
    void deleteBinding(String bindingId, DeleteCommand command);
    ApplyStatus acknowledge(ApplyAckCommand command);
    ConnectionTestResult recordConnectionTest(ConnectionTestCommand command);

    record ServerGroup(
            String serverGroupId, String groupName, String environmentCode, String serviceId, String endpointCode,
            CpfGatewayProtocol targetProtocol, CpfGatewayLoadBalancePolicy loadBalancePolicy, String hashKeySource,
            String healthPolicyId, String failoverGroupId, String status, boolean directAllowed,
            int memberCount, long version, OffsetDateTime updatedAt) {}
    record GroupMember(
            String serverGroupId, String instanceId, int weight, int priority, boolean enabled,
            CpfGatewayHealthStatus effectiveStatus, long fencingToken, OffsetDateTime updatedAt) {}
    record GatewayBinding(
            String bindingId, String routeId, String environmentCode, String hostPattern, String pathPattern,
            String httpMethod, String apiVersion, CpfGatewayProtocol ingressProtocol, CpfGatewayProtocol targetProtocol,
            String serviceId, String serverGroupId, String routeVersion, String tlsPolicyId,
            String authenticationPolicyId, String authorizationPolicyId, String headerPolicyId, String rateLimitPolicyId,
            String healthPolicyId, int connectTimeoutMs, int responseTimeoutMs, int overallTimeoutMs, int maxRetryCount,
            boolean idempotent, String failoverGroupId, String status, boolean gatewayAllowed, boolean directAllowed,
            String approvalId, OffsetDateTime effectiveFrom, OffsetDateTime effectiveTo, long version, OffsetDateTime updatedAt) {}
    record ApplyStatus(
            String bindingId, String gatewayInstanceId, String expectedVersion, String appliedVersion, String status,
            String errorCode, String errorMessage, OffsetDateTime acknowledgedAt, OffsetDateTime lastSeenAt) {}
    record ConnectionTestResult(
            String testId, String bindingId, String gatewayInstanceId, String instanceId, String testType,
            String status, String failureStage, long durationMs, String traceId, String operationId,
            OffsetDateTime testedAt, String testedBy) {}
    record MutationResult(String resourceType, String resourceId, String status, long version, OffsetDateTime changedAt) {}

    record ServerGroupCommand(
            String operationId, String serverGroupId, String groupName, String environmentCode, String serviceId,
            String endpointCode, CpfGatewayProtocol targetProtocol, CpfGatewayLoadBalancePolicy loadBalancePolicy,
            String hashKeySource, String healthPolicyId, String failoverGroupId, boolean directAllowed,
            List<MemberCommand> members, Long expectedVersion, String reason, String requestedBy) {}
    record MemberCommand(String instanceId, int weight, int priority, boolean enabled) {}
    record GatewayBindingCommand(
            String operationId, String bindingId, CpfGatewayRoute route, String serverGroupId,
            boolean gatewayAllowed, boolean directAllowed, String approvalId, OffsetDateTime effectiveFrom,
            OffsetDateTime effectiveTo, Long expectedVersion, String reason, String requestedBy) {}
    record BindingStateCommand(
            String operationId, String bindingId, String targetState, Long expectedVersion,
            String approvalId, String reason, String requestedBy) {}
    record DeleteCommand(String operationId, Long expectedVersion, String reason, String requestedBy) {}
    record ApplyAckCommand(
            String bindingId, String gatewayInstanceId, String expectedVersion, String appliedVersion,
            String status, String errorCode, String errorMessage, OffsetDateTime acknowledgedAt) {}
    record ConnectionTestCommand(
            String testId, String bindingId, String gatewayInstanceId, String instanceId, String testType,
            String status, String failureStage, long durationMs, String traceId, String operationId,
            OffsetDateTime testedAt, String testedBy) {}
}
