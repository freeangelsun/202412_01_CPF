package com.cpf.messaging.spi.broker;

import java.util.regex.Pattern;

/** Removes credentials and bounds provider failure details before durable persistence. */
/** CpfBrokerFailureSanitizer는 Broker 처리 결과의 확인·복구 경계를 외부 구현과 분리하는 SPI입니다. */
public final class CpfBrokerFailureSanitizer {
    private static final int MAX_DETAIL_LENGTH = 512;
    private static final Pattern KEY_VALUE_SECRET = Pattern.compile(
            "(?i)(password|passwd|pwd|secret|token|api[-_]?key|authorization|credential)"
                    + "\\s*[:=]\\s*([^,;\\s]+)");
    private static final Pattern BEARER = Pattern.compile("(?i)\\bBearer\\s+[A-Za-z0-9._~+/-]+=*");
    private static final Pattern SASL = Pattern.compile("(?i)(sasl\\.[a-z0-9_.-]+)\\s*[:=]\\s*[^,;\\s]+");
    private static final Pattern URL_USERINFO = Pattern.compile("(?i)(https?://)([^/@\\s]+)@");

    private CpfBrokerFailureSanitizer() {
    }

    /** sanitizeNullable는 Broker 실패 상세에서 secret·개인정보 후보를 제거해 로그와 DLQ에 안전하게 남깁니다. */
    public static String sanitizeNullable(String detail) {
        return detail == null ? null : sanitize(detail);
    }

    /** sanitize는 Broker 실패 상세에서 secret·개인정보 후보를 제거해 로그와 DLQ에 안전하게 남깁니다. */
    public static String sanitize(String detail) {
        if (detail == null || detail.isBlank()) {
            return "provider failure";
        }
        String masked = BEARER.matcher(detail).replaceAll("Bearer ***");
        masked = KEY_VALUE_SECRET.matcher(masked).replaceAll("$1=***");
        masked = SASL.matcher(masked).replaceAll("$1=***");
        masked = URL_USERINFO.matcher(masked).replaceAll("$1***@");
        masked = masked.replace('\r', ' ').replace('\n', ' ').trim();
        if (masked.length() > MAX_DETAIL_LENGTH) {
            masked = masked.substring(0, MAX_DETAIL_LENGTH) + "...";
        }
        return masked;
    }
}
