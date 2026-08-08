package com.cpf.core.common.logging.lineage;

import com.cpf.core.common.logging.segment.TransactionSegmentRecord;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Objects;

/**
 * 운영 거래 lineage의 정규화 불변 projection row. raw payload/secret 저장은 금지되며 값 객체 자체는 thread-safe하다.
 * <p>이 타입은 조회/투영용 값 객체이며 DB transaction을 시작하거나 외부 side effect를 수행하지 않는다. nullable 보조 식별자는
 * 해당 hop에서 적용되지 않을 수 있으나 transactionId/segmentId는 생성 경로에서 필수 검증한다.</p>
 * @param lineageId lineage row 고유 식별자
 * @param transactionId end-to-end 거래 식별자
 * @param segmentId 현재 segment 식별자
 * @param parentSegmentId 부모 segment 식별자, 최초 segment면 null 가능
 * @param attemptNo retry attempt 번호(1 이상)
 * @param traceId trace 식별자, 미수집 시 null 가능
 * @param spanId span 식별자, 미수집 시 null 가능
 * @param requestId 요청 식별자, 미적용 시 null 가능
 * @param idempotencyKey 멱등성 키, 미적용 시 null 가능
 * @param tenantId tenant 식별자, 단일 tenant면 null 가능
 * @param channelCode 인가된 Channel 코드
 * @param actorIdMasked 마스킹된 actor 식별자
 * @param instanceId 처리 instance 식별자
 * @param wasId WAS 식별자
 * @param agentId agent 식별자
 * @param workerId worker 식별자
 * @param remoteSystem 원격 시스템 코드
 * @param operationId operation/API 식별자
 * @param messageId message 식별자
 * @param consumerGroup consumer group 식별자
 * @param dlqId DLQ 식별자
 * @param batchJobInstanceId Batch job instance 식별자
 * @param batchJobExecutionId Batch job execution 식별자
 * @param batchStepExecutionId Batch step execution 식별자
 * @param partitionId Batch partition 식별자
 * @param fileId File 처리 식별자
 * @param sourceType lineage source 유형
 * @param sourceRefId source 원본 참조 식별자
 * @param lifecycleState RUNNING/RETRYING/UNKNOWN/terminal 등의 lifecycle 상태
 * @param failureStage 실패 단계, 정상 시 null 가능
 * @param unknown UNKNOWN 여부
 * @param reconcileState Reconcile 상태, 미적용 시 null 가능
 * @param occurredAt 원본 이벤트 발생 시각
 * @param freshnessAt projection freshness 시각
 * @param payloadHash 원문 대신 저장하는 안전한 hash
 * @param archivedAt archive 시각, active row면 null 가능
 */
public record CpfTransactionLineageRecord(
        String lineageId, String transactionId, String segmentId, String parentSegmentId, int attemptNo,
        String traceId, String spanId, String requestId, String idempotencyKey, String tenantId, String channelCode,
        String actorIdMasked, String instanceId, String wasId, String agentId, String workerId, String remoteSystem,
        String operationId, String messageId, String consumerGroup, String dlqId, String batchJobInstanceId,
        String batchJobExecutionId, String batchStepExecutionId, String partitionId, String fileId, String sourceType,
        String sourceRefId, String lifecycleState, String failureStage, boolean unknown, String reconcileState,
        LocalDateTime occurredAt, LocalDateTime freshnessAt, String payloadHash, LocalDateTime archivedAt) {

    /** Segment를 lineage row로 정규화한다. @param source 비-null 원본 segment @param terminal terminal projection 여부 @return 비-null lineage record @throws NullPointerException source가 null이면 발생 @throws IllegalArgumentException 필수 거래/segment ID가 비어 있으면 발생. 원본을 변경하지 않으며 transaction side effect가 없다. */
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
