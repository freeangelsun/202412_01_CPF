package com.cpf.admin.opr.service;

import com.cpf.core.api.reliability.CpfReliabilityOperationsPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * ADM reliability API와 CPF 공개 운영 포트를 연결하는 얇은 어댑터입니다.
 */
@Service
public class AdmReliabilityService extends com.cpf.admin.common.base.AdmBaseService {
    private final CpfReliabilityOperationsPort operationsPort;
    private AdmSessionService sessionService;

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

    public ChangeResult requestDlqReplay(String messageId, String operatorId, String reason) {
        return map(operationsPort.requestDlqReplay(messageId, operatorId, reason));
    }

    public ChangeResult resolveUnknown(String unknownId, String targetStatus, String operatorId, String reason) {
        Map<String,Object> row = operationsPort.findUnknownResult(unknownId)
                .orElseThrow(() -> new IllegalArgumentException("결과 미확정 건을 찾을 수 없습니다. unknownId=" + unknownId));
        String type = String.valueOf(row.get("unknown_type"));
        if ("ADM_SESSION_REVOKE".equals(type) && "RETRY_PENDING".equalsIgnoreCase(targetStatus)) {
            if (sessionService == null) {
                throw new IllegalStateException("ADM Session Service가 연결되지 않아 재처리할 수 없습니다.");
            }
            Object external = row.get("external_key");
            sessionService.retryRevocation(external == null ? null : String.valueOf(external));
            return map(operationsPort.resolveUnknown(unknownId, "RESOLVED", operatorId, reason));
        }
        return map(operationsPort.resolveUnknown(unknownId, targetStatus, operatorId, reason));
    }

    private ChangeResult map(CpfReliabilityOperationsPort.ChangeResult result) {
        return new ChangeResult(result.before(), result.after(), result.reason());
    }

    public record ChangeResult(Map<String, Object> before, Map<String, Object> after, String reason) {
    }
}
