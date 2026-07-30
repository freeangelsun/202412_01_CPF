package com.cpf.core.api.gateway;

import java.time.Instant;

/** Gateway Control 보안 검증 실패를 변경 불가능한 감사 원장에 적재하는 Port입니다. */
public interface CpfGatewayControlSecurityAuditPort {
    void append(SecurityFailure event);

    record SecurityFailure(
            String eventId,
            Instant occurredAt,
            String audience,
            String keyId,
            String callerService,
            String operatorId,
            String httpMethod,
            String requestTarget,
            String remoteAddress,
            String resultCode,
            String safeMessage) {
        public SecurityFailure {
            eventId = required(eventId, "eventId");
            occurredAt = occurredAt == null ? Instant.now() : occurredAt;
            audience = safe(audience, 160);
            keyId = safe(keyId, 80);
            callerService = safe(callerService, 80);
            operatorId = safe(operatorId, 100);
            httpMethod = safe(httpMethod, 16);
            requestTarget = safe(requestTarget, 1000);
            remoteAddress = safe(remoteAddress, 128);
            resultCode = required(resultCode, "resultCode");
            safeMessage = safe(safeMessage, 1000);
        }

        private static String required(String value, String field) {
            if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
            return value.trim();
        }

        private static String safe(String value, int maxLength) {
            String normalized = value == null ? "" : value.replace('\r', ' ').replace('\n', ' ').trim();
            return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
        }
    }
}
