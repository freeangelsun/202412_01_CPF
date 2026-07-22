package com.cpf.core.common.batch;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 배치 실행 상태를 알림, 감사, broker 전파 대상으로 넘기기 위한 공통 이벤트입니다.
 *
 * @param eventType           이벤트 유형
 * @param jobId               배치 Job ID
 * @param cpfExecutionId      CPF 운영 메타 실행 ID
 * @param transactionGlobalId 거래 글로벌 ID
 * @param message             이벤트 메시지
 * @param payload             추가 속성
 * @param occurredAt          발생 일시
 */
public record CpfBatchEvent(
        CpfBatchEventType eventType,
        String jobId,
        Long cpfExecutionId,
        String transactionGlobalId,
        String message,
        Map<String, Object> payload,
        LocalDateTime occurredAt) {

    public CpfBatchEvent {
        payload = payload == null ? Map.of() : new LinkedHashMap<>(payload);
        occurredAt = occurredAt == null ? LocalDateTime.now() : occurredAt;
    }

    public static CpfBatchEvent now(
            CpfBatchEventType eventType,
            String jobId,
            Long cpfExecutionId,
            String transactionGlobalId,
            String message,
            Map<String, Object> payload) {
        return new CpfBatchEvent(eventType, jobId, cpfExecutionId, transactionGlobalId, message, payload, LocalDateTime.now());
    }
}
