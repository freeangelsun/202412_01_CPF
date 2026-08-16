package com.cpf.admin.opr.dto;

import java.time.LocalDateTime;

/**
 * ADM 운영 알림 발송 이력 응답입니다.
 *
 * <p>Provider 호출 결과뿐 아니라 Durable Outbox의 operation, retry, lease, CAS 정보를 함께
 * 노출하여 운영자가 {@code UNKNOWN_RESULT}를 실제 Provider 이력과 대조한 뒤 재시도 또는
 * 취소 여부를 판단할 수 있게 합니다. 수신자는 마스킹된 값만 반환합니다.</p>
 *
 * @param deliveryId 알림 발송 로그 순번
 * @param ruleId 알림 규칙 순번
 * @param eventType 알림 이벤트 유형
 * @param targetType 알림 대상 유형
 * @param targetId 알림 대상 ID
 * @param receiver 마스킹된 수신자
 * @param deliveryStatus 발송 상태
 * @param deliveryMessage 마스킹된 발송 결과 메시지
 * @param operationId 멱등 운영 작업 식별자
 * @param requestHash 동일 요청 대조용 SHA-256
 * @param attemptCount 현재 시도 횟수
 * @param maxAttempts 최대 자동 시도 횟수
 * @param nextAttemptAt 다음 자동 시도 예정 시각
 * @param leaseOwner 현재 Worker Lease 소유자
 * @param leaseUntil Worker Lease 만료 시각
 * @param version 운영 조치 CAS Version
 * @param lastErrorCode 마지막 오류 코드
 * @param createdBy 최초 요청 운영자
 * @param updatedBy 마지막 변경 주체
 * @param requestedAt 발송 요청 일시
 * @param deliveredAt 발송 완료 일시
 * @param createdAt 등록일시
 * @param updatedAt 수정일시
 */
public record AdmNotificationDeliveryLogResponse(
        long deliveryId,
        Long ruleId,
        String eventType,
        String targetType,
        String targetId,
        String receiver,
        String deliveryStatus,
        String deliveryMessage,
        String operationId,
        String requestHash,
        int attemptCount,
        int maxAttempts,
        LocalDateTime nextAttemptAt,
        String leaseOwner,
        LocalDateTime leaseUntil,
        long version,
        String lastErrorCode,
        String createdBy,
        String updatedBy,
        LocalDateTime requestedAt,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
