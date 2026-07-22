package cpf.adm.opr.service;

import cpf.pfw.api.reliability.CpfReliabilityOperationsPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * ADM reliability API와 PFW 공개 운영 포트를 연결하는 얇은 어댑터입니다.
 */
@Service
public class AdmReliabilityService extends cpf.adm.common.base.AdmBaseService {
    private final CpfReliabilityOperationsPort operationsPort;

    public AdmReliabilityService(CpfReliabilityOperationsPort operationsPort) {
        this.operationsPort = operationsPort;
    }

    public List<Map<String, Object>> findIdempotency(String scope, String status, String key, int limit) {
        return operationsPort.findIdempotency(scope, status, key, limit);
    }

    public List<Map<String, Object>> findOutbox(String status, String transactionGlobalId, String topic, int limit) {
        return operationsPort.findOutbox(status, transactionGlobalId, topic, limit);
    }

    public List<Map<String, Object>> findInbox(String status, String key, int limit) {
        return operationsPort.findInbox(status, key, limit);
    }

    public List<Map<String, Object>> findDlq(String status, String transactionGlobalId, String topic, int limit) {
        return operationsPort.findDlq(status, transactionGlobalId, topic, limit);
    }

    public List<Map<String, Object>> findFileTransfers(
            String status,
            String transactionGlobalId,
            String endpointCode,
            int limit) {
        return operationsPort.findFileTransfers(status, transactionGlobalId, endpointCode, limit);
    }

    public List<Map<String, Object>> findUnknownResults(
            String type,
            String status,
            String transactionGlobalId,
            int limit) {
        return operationsPort.findUnknownResults(type, status, transactionGlobalId, limit);
    }

    public ChangeResult requestDlqReplay(String messageId, String operatorId, String reason) {
        return map(operationsPort.requestDlqReplay(messageId, operatorId, reason));
    }

    public ChangeResult resolveUnknown(String unknownId, String targetStatus, String operatorId, String reason) {
        return map(operationsPort.resolveUnknown(unknownId, targetStatus, operatorId, reason));
    }

    private ChangeResult map(CpfReliabilityOperationsPort.ChangeResult result) {
        return new ChangeResult(result.before(), result.after(), result.reason());
    }

    public record ChangeResult(Map<String, Object> before, Map<String, Object> after, String reason) {
    }
}
