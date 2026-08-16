package com.cpf.notification.spi;

import com.cpf.notification.api.CpfNotificationRequest;
import com.cpf.notification.api.CpfNotificationResult;

/** Reconciles a provider-side result after an ambiguous transport outcome. */
/** CpfNotificationReconciler 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfNotificationReconciler {
    CpfNotificationResult reconcile(CpfNotificationRequest request, CpfNotificationResult unknownResult);
}
