package com.cpf.core.common.logging.lineage;

import com.cpf.core.common.logging.segment.TransactionSegmentRecord;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Objects;

/** Canonical normalized operational lineage projection row. Raw payload/secret values are prohibited. */
public record CpfTransactionLineageRecord(
        String lineageId, String transactionId, String segmentId, String parentSegmentId, int attemptNo,
        String traceId, String spanId, String requestId, String idempotencyKey, String tenantId, String channelCode,
        String actorIdMasked, String instanceId, String wasId, String agentId, String workerId, String remoteSystem,
        String operationId, String messageId, String consumerGroup, String dlqId, String batchJobInstanceId,
        String batchJobExecutionId, String batchStepExecutionId, String partitionId, String fileId, String sourceType,
        String sourceRefId, String lifecycleState, String failureStage, boolean unknown, String reconcileState,
        LocalDateTime occurredAt, LocalDateTime freshnessAt, String payloadHash, LocalDateTime archivedAt) {

    public static CpfTransactionLineageRecord fromSegment(TransactionSegmentRecord source, boolean terminal) {
        Objects.requireNonNull(source, "source");
        String tx = require(source.getTransactionId(), "transactionId");
        String seg = require(source.getTransactionSegmentId(), "transactionSegmentId");
        LocalDateTime occurred = terminal && source.getEndedAt() != null ? source.getEndedAt() :
                (source.getStartedAt() == null ? LocalDateTime.now() : source.getStartedAt());
        int attempt = source.getAttemptNo() == null || source.getAttemptNo() < 1 ? 1 : source.getAttemptNo();
        String state = blankTo(source.getStatus(), terminal ? "UNKNOWN" : "RUNNING");
        String phase = terminal ? "END" : "START";
        String payload = String.join("|", tx, seg, String.valueOf(attempt), phase, state,
                blankTo(source.getResultState(), ""), blankTo(source.getFailureCode(), ""),
                occurred.toString());
        String hash = sha256(payload);
        return new CpfTransactionLineageRecord(sha256("SEGMENT|" + seg + "|" + attempt + "|" + phase + "|" + occurred),
                tx, seg, source.getParentSegmentId(), attempt, null, null, null, null, null,
                firstNonBlank(source.getOriginalChannelCode(), source.getChannelCode()), source.getOperatorIdMasked(),
                source.getSelectedInstanceId(), null, null, null, firstNonBlank(source.getTargetModuleCode(), source.getExternalInstitutionCode()),
                firstNonBlank(source.getApiPath(), source.getTransactionName()), null, null, null, null, null, null, null, null,
                "SEGMENT", seg, state, source.getFailureCode(), source.getUnknownResultId() != null,
                source.getUnknownResultId() == null ? null : blankTo(source.getResultState(), "UNKNOWN"), occurred, LocalDateTime.now(), hash, null);
    }

    private static String require(String value, String name) { if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required"); return value; }
    private static String blankTo(String v, String d) { return v == null || v.isBlank() ? d : v; }
    private static String firstNonBlank(String a, String b) { return a != null && !a.isBlank() ? a : b; }
    private static String sha256(String value) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); } catch (Exception e) { throw new IllegalStateException(e); } }
}
