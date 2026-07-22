package com.cpf.core.api.reliability;

import java.util.List;
import java.util.Map;

/**
 * CPF 신뢰성 상태 조회와 운영 명령을 제공하는 공개 포트입니다.
 */
public interface CpfReliabilityOperationsPort {
    List<Map<String, Object>> findIdempotency(String scope, String status, String key, int limit);

    List<Map<String, Object>> findOutbox(String status, String transactionGlobalId, String topic, int limit);

    List<Map<String, Object>> findInbox(String status, String key, int limit);

    List<Map<String, Object>> findDlq(String status, String transactionGlobalId, String topic, int limit);

    List<Map<String, Object>> findFileTransfers(String status, String transactionGlobalId, String endpointCode, int limit);

    List<Map<String, Object>> findUnknownResults(String type, String status, String transactionGlobalId, int limit);

    ChangeResult requestDlqReplay(String messageId, String operatorId, String reason);

    ChangeResult resolveUnknown(String unknownId, String targetStatus, String operatorId, String reason);

    record ChangeResult(Map<String, Object> before, Map<String, Object> after, String reason) {
    }
}
