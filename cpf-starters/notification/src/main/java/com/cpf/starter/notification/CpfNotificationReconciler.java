package com.cpf.starter.notification;

/** Provider가 결과불명 거래를 외부 수신결과로 재조회할 때 구현하는 확장 SPI입니다. */
public interface CpfNotificationReconciler {
    CpfNotificationResult reconcile(CpfNotificationRequest request, CpfNotificationResult unknownResult);
}
