package cpf.adm.opr.dto;

/**
 * ADM 운영 알림 규칙 등록/수정 요청입니다.
 *
 * @param eventType 알림 이벤트 유형
 * @param eventSubType 알림 이벤트 세부 유형
 * @param channelCode 알림 채널 코드
 * @param templateCode 알림 템플릿 코드
 * @param severity 알림 심각도
 * @param receiverGroup 수신자 그룹
 * @param useYn 사용 여부
 * @param reason 감사 사유
 * @param requestUser 요청 운영자
 */
public record AdmNotificationRuleRequest(
        String eventType,
        String eventSubType,
        String channelCode,
        String templateCode,
        String severity,
        String receiverGroup,
        String useYn,
        String reason,
        String requestUser) {
}
