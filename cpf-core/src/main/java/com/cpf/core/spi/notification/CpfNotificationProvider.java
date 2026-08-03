package com.cpf.core.spi.notification;

import com.cpf.core.api.notification.CpfNotificationProviderStatus;
import com.cpf.core.api.notification.CpfNotificationRequest;
import com.cpf.core.api.notification.CpfNotificationResult;

/** Customer extension SPI. No mail/SMS SDK type may cross this boundary. */
public interface CpfNotificationProvider {
    String channel();
    CpfNotificationResult send(CpfNotificationRequest request);
    default CpfNotificationProviderStatus health() {
        return CpfNotificationProviderStatus.unknown("NOT_IMPLEMENTED");
    }
}
