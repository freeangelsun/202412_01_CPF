package com.cpf.admin.opr.dto;

import java.time.LocalDateTime;

/**
 * ADM 운영 알림 규칙 응답입니다.
 *
 * @param ruleId 알림 규칙 순번
 * @param eventType 알림 이벤트 유형
 * @param eventSubType 알림 이벤트 세부 유형
 * @param channelCode 알림 채널 코드
 * @param templateCode 알림 템플릿 코드
 * @param severity 알림 심각도
 * @param receiverGroup 수신자 그룹
 * @param useYn 사용 여부
 * @param createdBy 등록자
 * @param createdAt 등록일시
 * @param updatedBy 수정자
 * @param updatedAt 수정일시
 */
public record AdmNotificationRuleResponse(
        long ruleId,
        String eventType,
        String eventSubType,
        String channelCode,
        String templateCode,
        String severity,
        String receiverGroup,
        String useYn,
        String createdBy,
        LocalDateTime createdAt,
        String updatedBy,
        LocalDateTime updatedAt) {
}
