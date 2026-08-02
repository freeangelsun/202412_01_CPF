package com.cpf.starter.notification;

/** Customer-extensible notification Provider SPI that does not expose transport OSS types. */
public interface CpfNotificationProvider {
    String channel();

    CpfNotificationResult send(CpfNotificationRequest request);

    default CpfNotificationProviderStatus health() {
        return CpfNotificationProviderStatus.unknown("HEALTH_CHECK_NOT_IMPLEMENTED");
    }
}
