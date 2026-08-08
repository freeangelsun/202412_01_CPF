package com.cpf.admin.opr.service;

import com.cpf.admin.approval.service.AdmApprovalService;
import com.cpf.admin.opr.reliability.AdmBrokerDlqReplayApprovalSnapshot;

import com.cpf.core.api.reliability.CpfReliabilityOperationsPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * ADM reliability API와 CPF 공개 운영 포트를 연결하는 얇은 어댑터입니다.
 */
@Service
public class AdmReliabilityService extends com.cpf.admin.common.base.AdmBaseService {
    private static final Duration DLQ_REPLAY_APPROVAL_TTL = Duration.ofMinutes(15);
    private static final String DLQ_REPLAY_ACTION = "BROKER_DLQ_REPLAY";
    private static final String DLQ_REPLAY_OWNER = "cpf-starters-messaging-reliability-jdbc";
    private static final String DLQ_REPLAY_TARGET = "CPF_BROKER_DLQ";

    private final CpfReliabilityOperationsPort operationsPort;
    private AdmSessionService sessionService;
    private AdmApprovalService approvalService;

    public AdmReliabilityService(CpfReliabilityOperationsPort operationsPort) {
        this.operationsPort = operationsPort;
    }

    /**
     * Session revoke 결과불명 재처리는 기존 reliability API에서만 호출되며,
     * 기존 단위 테스트와 외부 생성자 계약을 깨지 않도록 선택 주입한다.
     */
    @Autowired(required = false)
    void setSessionService(AdmSessionService sessionService) {
        this.sessionService = sessionService;
    }

    /** DLQ 위험조치는 Approval Engine이 연결된 경우에만 요청할 수 있습니다. */
    @Autowired(required = false)
    void setApprovalService(AdmApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    public List<Map<String, Object>> findIdempotency(String scope, String status, String key, int limit) {
        return operationsPort.findIdempotency(scope, status, key, limit);
    }

    public List<Map<String, Object>> findOutbox(String status, String transactionId, String topic, int limit) {
        return operationsPort.findOutbox(status, transactionId, topic, limit);
    }

    public List<Map<String, Object>> findInbox(String status, String key, int limit) {
        return operationsPort.findInbox(status, key, limit);
    }

    public List<Map<String, Object>> findDlq(String status, String transactionId, String topic, int limit) {
        return operationsPort.findDlq(status, transactionId, topic, limit);
    }

    public List<Map<String, Object>> findFileTransfers(
            String status,
            String transactionId,
            String endpointCode,
            int limit) {
        return operationsPort.findFileTransfers(status, transactionId, endpointCode, limit);
    }

    public List<Map<String, Object>> findUnknownResults(
            String type,
            String status,
            String transactionId,
            int limit) {
        return operationsPort.findUnknownResults(type, status, transactionId, limit);
    }

    /**
     * 직접 replay를 실행하지 않고 현재 DLQ 상태를 불변 Snapshot으로 고정해 승인 요청을 생성합니다.
     */
    public Map<String, Object> requestDlqReplayApproval(String messageId, String operatorId, String reason) {
        if (approvalService == null) {
            throw new IllegalStateException("ADM Approval Service가 연결되지 않아 DLQ 재처리를 요청할 수 없습니다.");
        }
        Map<String, Object> dlq = findDlqMessage(messageId);
        AdmBrokerDlqReplayApprovalSnapshot.Snapshot snapshot = AdmBrokerDlqReplayApprovalSnapshot.from(dlq);
        String replayStatus = value(dlq, "replay_status");
        if (!java.util.Set.of("WAITING", "FAILED").contains(replayStatus.toUpperCase(java.util.Locale.ROOT))) {
            throw new IllegalArgumentException("현재 상태에서는 DLQ 재처리 승인을 요청할 수 없습니다.");
        }
        String requestKey = DLQ_REPLAY_ACTION + ':' + messageId.trim() + ':'
                + snapshot.replayCount() + ':' + snapshot.updatedAt().toEpochMilli();
        return approvalService.requestApproval(new AdmApprovalService.CreateRequest(
                requestKey,
                null,
                null,
                DLQ_REPLAY_ACTION,
                DLQ_REPLAY_OWNER,
                DLQ_REPLAY_ACTION,
                DLQ_REPLAY_TARGET,
                messageId.trim(),
                snapshot.json(),
                Instant.now().plus(DLQ_REPLAY_APPROVAL_TTL),
                reason), operatorId);
    }

    /** 과거 직접 실행 경로는 승인 우회를 막기 위해 fail-closed합니다. */
    @Deprecated(forRemoval = false)
    public ChangeResult requestDlqReplay(String messageId, String operatorId, String reason) {
        throw new IllegalStateException("DLQ 재처리는 승인 요청 후 Owner Command로만 실행할 수 있습니다.");
    }

    public ChangeResult resolveUnknown(
            String unknownId, String targetStatus, long expectedVersion, String operatorId, String reason) {
        Map<String,Object> row = operationsPort.findUnknownResult(unknownId)
                .orElseThrow(() -> new IllegalArgumentException("결과 미확정 건을 찾을 수 없습니다. unknownId=" + unknownId));
        String type = String.valueOf(row.get("unknown_type"));
        if ("ADM_SESSION_REVOKE".equals(type) && "RETRY_PENDING".equalsIgnoreCase(targetStatus)) {
            if (sessionService == null) {
                throw new IllegalStateException("ADM Session Service가 연결되지 않아 재처리할 수 없습니다.");
            }
            Object external = row.get("external_key");
            sessionService.retryPendingRevocation(external == null ? null : String.valueOf(external));
            return map(operationsPort.resolveUnknown(unknownId, "RESOLVED", expectedVersion, operatorId, reason));
        }
        return map(operationsPort.resolveUnknown(unknownId, targetStatus, expectedVersion, operatorId, reason));
    }

    private Map<String, Object> findDlqMessage(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("messageId가 필요합니다.");
        }
        String normalized = messageId.trim();
        return operationsPort.findDlq(null, null, null, 1_000).stream()
                .filter(row -> normalized.equals(value(row, "message_id")))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("DLQ를 찾을 수 없습니다. messageId=" + normalized));
    }

    private static String value(Map<String, ?> row, String key) {
        String normalized = key.replace("_", "").toLowerCase(java.util.Locale.ROOT);
        for (Map.Entry<String, ?> entry : row.entrySet()) {
            String candidate = entry.getKey().replace("_", "").toLowerCase(java.util.Locale.ROOT);
            if (candidate.equals(normalized)) {
                return entry.getValue() == null ? "" : String.valueOf(entry.getValue()).trim();
            }
        }
        return "";
    }

    private ChangeResult map(CpfReliabilityOperationsPort.ChangeResult result) {
        return new ChangeResult(result.before(), result.after(), result.reason());
    }

    public record ChangeResult(Map<String, Object> before, Map<String, Object> after, String reason) {
    }
}
