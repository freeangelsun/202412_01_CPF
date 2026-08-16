package com.cpf.notification.dispatch;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.notification.dispatch")
/** CpfNotificationProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfNotificationProperties(
        boolean enabled,
        String workerId,
        int batchSize,
        Duration leaseDuration,
        Duration retryDelay,
        Duration unknownReconcileDelay) {

    public CpfNotificationProperties {
        workerId = workerId == null || workerId.isBlank() ? "cpf-notification-worker" : workerId.trim();
        batchSize = batchSize <= 0 ? 100 : Math.min(batchSize, 500);
        leaseDuration = leaseDuration == null ? Duration.ofSeconds(30) : leaseDuration;
        retryDelay = retryDelay == null ? Duration.ofSeconds(30) : retryDelay;
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        unknownReconcileDelay = unknownReconcileDelay == null ? Duration.ofMinutes(1) : unknownReconcileDelay;
        if (leaseDuration.isNegative() || leaseDuration.isZero()) {
            throw new IllegalArgumentException("Notification lease duration must be positive.");
        }
        if (retryDelay.isNegative() || retryDelay.isZero()) {
            throw new IllegalArgumentException("Notification retry delay must be positive.");
        }
        if (unknownReconcileDelay.isNegative() || unknownReconcileDelay.isZero()) {
            throw new IllegalArgumentException("Notification reconcile delay must be positive.");
        }
    }
}
