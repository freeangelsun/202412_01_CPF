package com.cpf.core.spi.notification;

import com.cpf.core.api.notification.CpfNotificationRequest;
import com.cpf.core.api.notification.CpfNotificationResult;

/** Reconciles a provider-side result after an ambiguous transport outcome. */
public interface CpfNotificationReconciler {
    CpfNotificationResult reconcile(CpfNotificationRequest request, CpfNotificationResult unknownResult);
}
