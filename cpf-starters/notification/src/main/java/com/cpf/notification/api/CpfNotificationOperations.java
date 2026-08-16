package com.cpf.notification.api;

/** Public command/query boundary for notification dispatch and controlled reprocessing. */
/** CpfNotificationOperations 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfNotificationOperations {
    CpfNotificationResult dispatch(CpfNotificationRequest request);
    CpfNotificationResult findResult(String notificationId);
    CpfNotificationResult approveReprocess(String notificationId, String operatorId, String reason);
    void recordReceipt(CpfNotificationReceipt receipt, String operatorId);
}
