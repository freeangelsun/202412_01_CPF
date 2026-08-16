package com.cpf.notification.spi;

import com.cpf.notification.api.CpfNotificationProviderStatus;
import com.cpf.notification.api.CpfNotificationRequest;
import com.cpf.notification.api.CpfNotificationResult;

/** Customer extension SPI. No mail/SMS SDK type may cross this boundary. */
/** CpfNotificationProvider 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfNotificationProvider {
    String channel();
    CpfNotificationResult send(CpfNotificationRequest request);
    default CpfNotificationProviderStatus health() {
        return CpfNotificationProviderStatus.unknown("PROVIDER_HEALTH_UNAVAILABLE");
    }
}
