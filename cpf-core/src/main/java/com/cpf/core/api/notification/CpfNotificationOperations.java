package com.cpf.core.api.notification;

/** Public command/query boundary for notification dispatch and controlled reprocessing. */
public interface CpfNotificationOperations {
    CpfNotificationResult dispatch(CpfNotificationRequest request);
    CpfNotificationResult findResult(String notificationId);
    CpfNotificationResult approveReprocess(String notificationId, String operatorId, String reason);
    void recordReceipt(CpfNotificationReceipt receipt, String operatorId);
}
