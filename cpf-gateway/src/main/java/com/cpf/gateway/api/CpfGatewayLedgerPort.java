package com.cpf.gateway.api;

import java.time.OffsetDateTime;

/** Gateway Transaction/Attempt 필수 운영 원장을 기록하는 공개 Port입니다. */
public interface CpfGatewayLedgerPort {
    void begin(TransactionStart event);
    void recordAttempt(Attempt event);
    void recordCapture(CaptureSegment event);
    void complete(TransactionCompletion event);

    record TransactionStart(
            String gatewayTransactionId, String transactionId, String traceId, String channelId,
            String sourceIp, int sourcePort, String gatewayInstanceId, String bindingId,
            String routeId, String routeVersion, long bindingVersion, String configChecksum,
            String serverGroupId, String requestMethod, String requestPath, long requestSize,
            OffsetDateTime startedAt) {}

    record Attempt(
            String attemptId, String gatewayTransactionId, int attemptNo, String instanceId,
            String targetHost, Integer targetPort, String targetProtocol, long connectDurationMs,
            long responseDurationMs, String status, String protocolStatus, String failureCode,
            String failureMessage, String gatewayInstanceId, String selectionReason,
            boolean unknown, OffsetDateTime startedAt, OffsetDateTime finishedAt) {}

    record CaptureSegment(
            String gatewayTransactionId, String segmentType, int policySchemaVersion,
            String policyChecksum, String capturedValue, boolean truncated, boolean metadataOnly,
            long observedBytes, OffsetDateTime capturedAt) {}

    record TransactionCompletion(
            String gatewayTransactionId, String finalInstanceId, String resultStatus,
            String protocolStatus, String businessCode, String failureStage, boolean unknown,
            long totalDurationMs, long responseSize, OffsetDateTime completedAt) {}
}
