package com.cpf.admin.opr.dto;

/** Durable Outbox에 접수된 알림 테스트 요청 결과입니다. */
public record AdmNotificationTestSendResponse(
        long deliveryId,
        AdmNotificationRuleResponse rule,
        String deliveryStatus,
        String providerVerification) {
}
