package com.cpf.admin.opr.dto;

import java.time.LocalDateTime;

/**
 * Durable Notification Outbox의 개별 Provider 호출 Attempt 이력입니다.
 *
 * <p>재시도 성공 후에도 최초 실패·Timeout·결과 불명 Attempt를 덮어쓰지 않고 보존합니다.
 * Provider 메시지는 저장·응답 전에 민감정보를 제거한 값만 사용합니다.</p>
 *
 * @param deliveryId 발송 ID
 * @param attemptNo 시도 순번
 * @param operationId 멱등 작업 ID
 * @param workerId Provider 호출을 소유한 Worker
 * @param attemptStatus Attempt 처리 상태
 * @param providerStatus Provider 결과 코드
 * @param providerMessage 민감정보가 제거된 Provider 결과 메시지
 * @param startedAt 호출 시작 시각
 * @param completedAt 결과 확정 시각
 * @param leaseVersion Claim 시점 CAS Version
 * @param createdBy 기록 주체
 * @param createdAt 기록 시각
 */
public record AdmNotificationDeliveryAttemptResponse(
        long deliveryId,
        int attemptNo,
        String operationId,
        String workerId,
        String attemptStatus,
        String providerStatus,
        String providerMessage,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        long leaseVersion,
        String createdBy,
        LocalDateTime createdAt) {
}
