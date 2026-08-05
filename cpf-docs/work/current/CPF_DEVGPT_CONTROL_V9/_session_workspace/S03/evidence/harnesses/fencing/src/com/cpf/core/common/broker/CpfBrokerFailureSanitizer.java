package com.cpf.core.common.broker;

import java.util.regex.Pattern;

/** Removes credentials and bounds provider failure details before durable persistence. */
public final class CpfBrokerFailureSanitizer {
    private static final int MAX_DETAIL_LENGTH = 512;
    private static final Pattern KEY_VALUE_SECRET = Pattern.compile(
            "(?i)(password|passwd|pwd|secret|token|api[-_]?key|authorization|credential)"
                    + "\\s*[:=]\\s*([^,;\\s]+)");
    private static final Pattern BEARER = Pattern.compile("(?i)\\bBearer\\s+[A-Za-z0-9._~+/-]+=*");

    private CpfBrokerFailureSanitizer() {
    }

    public static String sanitizeNullable(String detail) {
        return detail == null ? null : sanitize(detail);
    }

    public static String sanitize(String detail) {
        if (detail == null || detail.isBlank()) {
            return "provider failure";
        }
        String masked = BEARER.matcher(detail).replaceAll("Bearer ***");
        masked = KEY_VALUE_SECRET.matcher(masked).replaceAll("$1=***");
        masked = masked.replace('\r', ' ').replace('\n', ' ').trim();
        if (masked.length() > MAX_DETAIL_LENGTH) {
            masked = masked.substring(0, MAX_DETAIL_LENGTH) + "...";
        }
        return masked;
    }
}
