package com.cpf.gateway.api;

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
    ConnectionTestOperation requestConnectionTest(ConnectionTestRequest command);
    ConnectionTestOperation findConnectionTestOperation(String operationId);
    ConnectionTestOperation cancelConnectionTest(ConnectionTestCancel command);
    ConnectionTestOperation revalidateConnectionTest(ConnectionTestRevalidation command);
    List<ConnectionTestOperation> claimConnectionTests(String gatewayInstanceId, int limit);
    ConnectionTestOperation completeConnectionTest(ConnectionTestCompletion command);
    List<HealthProbeTarget> claimHealthProbes(String gatewayInstanceId, int limit, long leaseSeconds);
    HealthProbeTarget claimHealthProbe(String serverGroupId, String instanceId, String gatewayInstanceId, long leaseSeconds);
    void reportHealth(HealthProbeResult command);
    OperationsSnapshot operationsSnapshot();
    List<OperationsEvent> operationsEvents(String afterEventId, int limit);

    /** ServerGroup 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record ServerGroup(
            String serverGroupId, String groupName, String environmentCode, String serviceId, String endpointCode,
            CpfGatewayProtocol targetProtocol, CpfGatewayLoadBalancePolicy loadBalancePolicy, String hashKeySource,
            String healthPolicyId, String failoverGroupId, String status, boolean directAllowed,
            int memberCount, long version, OffsetDateTime updatedAt) {}
    record GroupMember(
            String serverGroupId, String instanceId, int weight, int priority, int canaryPercent, boolean enabled,
            CpfGatewayHealthStatus effectiveStatus, long fencingToken, OffsetDateTime updatedAt) {
        /** GroupMember 작업을 CPF 표준 계약에 따라 수행한다. */
        public GroupMember(String serverGroupId, String instanceId, int weight, int priority, boolean enabled,
                CpfGatewayHealthStatus effectiveStatus, long fencingToken, OffsetDateTime updatedAt) {
            this(serverGroupId, instanceId, weight, priority, 0, enabled, effectiveStatus, fencingToken, updatedAt);
        }
    }
    record GatewayBinding(
            String bindingId, String routeId, String environmentCode, String hostPattern, String pathPattern,
            String targetPath, String httpMethod, String apiVersion, CpfGatewayProtocol ingressProtocol, CpfGatewayProtocol targetProtocol,
            String serviceId, String serverGroupId, String routeVersion, String tlsPolicyId,
            String authenticationPolicyId, String authorizationPolicyId, String headerPolicyId, String rateLimitPolicyId,
            String healthPolicyId, int connectTimeoutMs, int responseTimeoutMs, int overallTimeoutMs, int maxRetryCount,
            boolean idempotent, String failoverGroupId, String status, boolean gatewayAllowed, boolean directAllowed,
            String approvalId, OffsetDateTime effectiveFrom, OffsetDateTime effectiveTo, String bindingChecksum, long version, OffsetDateTime updatedAt) {
        /** GatewayBinding 작업을 CPF 표준 계약에 따라 수행한다. */
        public GatewayBinding(
                String bindingId, String routeId, String environmentCode, String hostPattern, String pathPattern,
                String httpMethod, String apiVersion, CpfGatewayProtocol ingressProtocol, CpfGatewayProtocol targetProtocol,
                String serviceId, String serverGroupId, String routeVersion, String tlsPolicyId,
                String authenticationPolicyId, String authorizationPolicyId, String headerPolicyId, String rateLimitPolicyId,
                String healthPolicyId, int connectTimeoutMs, int responseTimeoutMs, int overallTimeoutMs, int maxRetryCount,
                boolean idempotent, String failoverGroupId, String status, boolean gatewayAllowed, boolean directAllowed,
                String approvalId, OffsetDateTime effectiveFrom, OffsetDateTime effectiveTo, String bindingChecksum,
                long version, OffsetDateTime updatedAt) {
            this(bindingId, routeId, environmentCode, hostPattern, pathPattern, pathPattern, httpMethod, apiVersion,
                    ingressProtocol, targetProtocol, serviceId, serverGroupId, routeVersion, tlsPolicyId,
                    authenticationPolicyId, authorizationPolicyId, headerPolicyId, rateLimitPolicyId, healthPolicyId,
                    connectTimeoutMs, responseTimeoutMs, overallTimeoutMs, maxRetryCount, idempotent, failoverGroupId,
                    status, gatewayAllowed, directAllowed, approvalId, effectiveFrom, effectiveTo, bindingChecksum,
                    version, updatedAt);
        }
    }
    /** ApplyStatus 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record ApplyStatus(
            String bindingId, String gatewayInstanceId, String expectedVersion, String appliedVersion, String status,
            String errorCode, String errorMessage, OffsetDateTime acknowledgedAt, OffsetDateTime lastSeenAt) {}
    record ConnectionTestResult(
            String testId, String bindingId, String gatewayInstanceId, String instanceId, String testType,
            String status, String failureStage, long durationMs, String traceId, String operationId,
            OffsetDateTime testedAt, String testedBy) {}
    record MutationResult(String resourceType, String resourceId, String status, long version, OffsetDateTime changedAt) {}
    record OperationsSnapshot(
            String status, OffsetDateTime generatedAt, String sourceInstanceId, long windowSeconds,
            long transactionCount, long successCount, long failureCount, long unknownCount,
            double tps, double successRate, double errorRate, long p95DurationMs, long p99DurationMs,
            long openCircuitCount, long expiringCertificateCount, long spoolBacklogCount,
            long spoolBacklogBytes, long driftCount, long failedConnectionTestCount,
            String lastEventId, List<String> warnings) {}
    /** OperationsEvent 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record OperationsEvent(
            String eventId, String eventType, String aggregateType, String aggregateId,
            String eventStatus, String sourceInstanceId, String payloadJson, OffsetDateTime occurredAt) {}

    record ServerGroupCommand(
            String operationId, String serverGroupId, String groupName, String environmentCode, String serviceId,
            String endpointCode, CpfGatewayProtocol targetProtocol, CpfGatewayLoadBalancePolicy loadBalancePolicy,
            String hashKeySource, String healthPolicyId, String failoverGroupId, boolean directAllowed,
            List<MemberCommand> members, Long expectedVersion, String reason, String requestedBy) {}
    /** MemberCommand 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record MemberCommand(String instanceId, int weight, int priority, int canaryPercent, boolean enabled) {
        public MemberCommand(String instanceId, int weight, int priority, boolean enabled) {
            this(instanceId, weight, priority, 0, enabled);
        }
    }
    record GatewayBindingCommand(
            String operationId, String bindingId, CpfGatewayRoute route, String serverGroupId,
            boolean gatewayAllowed, boolean directAllowed, String approvalId, OffsetDateTime effectiveFrom,
            OffsetDateTime effectiveTo, Long expectedVersion, String reason, String requestedBy) {}
    /** BindingStateCommand 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record BindingStateCommand(
            String operationId, String bindingId, String targetState, Long expectedVersion,
            String approvalId, String reason, String requestedBy) {}
    record DeleteCommand(String operationId, Long expectedVersion, String reason, String requestedBy) {}
    record ApplyAckCommand(
            String bindingId, String gatewayInstanceId, String expectedVersion, String appliedVersion,
            String status, String errorCode, String errorMessage, OffsetDateTime acknowledgedAt) {}

    record ConnectionTestOperation(
            String operationId, String bindingId, String testType, String status, String requestedBy,
            String reason, String payloadHash, OffsetDateTime expiresAt, boolean cancelRequested,
            String resultSummary, OffsetDateTime createdAt, OffsetDateTime startedAt,
            OffsetDateTime completedAt, long version) {}
    /** ConnectionTestRequest 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record ConnectionTestRequest(
            String operationId, String bindingId, String testType, String reason,
            String payloadHash, OffsetDateTime expiresAt, String requestedBy) {}
    record ConnectionTestCompletion(
            String operationId, String status, String resultSummary, Long expectedVersion) {}
    record ConnectionTestCancel(
            String operationId, Long expectedVersion, String reason, String requestedBy) {}
    record ConnectionTestRevalidation(
            String sourceOperationId, String newOperationId, String payloadHash,
            OffsetDateTime expiresAt, String reason, String requestedBy) {}
    /** HealthProbeTarget 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record HealthProbeTarget(
            String serverGroupId, String instanceId, String gatewayInstanceId, long fencingToken,
            String host, int port, CpfGatewayProtocol protocol, String healthPath, int timeoutMs) {}
    record HealthProbeResult(
            String healthHistoryId, String serverGroupId, String instanceId, String gatewayInstanceId,
            long fencingToken, String networkStatus, String tcpStatus, String tlsStatus,
            String applicationStatus, CpfGatewayHealthStatus overallStatus, String resultCode,
            long durationMs, OffsetDateTime observedAt, OffsetDateTime certificateNotAfter,
            String certificateFingerprintSha256) {}

    /** ConnectionTestCommand 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record ConnectionTestCommand(
            String testId, String bindingId, String gatewayInstanceId, String instanceId, String testType,
            String status, String failureStage, long durationMs, String traceId, String operationId,
            OffsetDateTime testedAt, String testedBy) {}
}
