package fps.pfw.common.logging;

import java.util.List;
import java.util.regex.Pattern;

public final class SensitiveDataMasker {

    private static final int DEFAULT_MAX_LENGTH = 4000;

    private static final List<String> SENSITIVE_KEYS = List.of(
            "password",
            "passwd",
            "pwd",
            "token",
            "authorization",
            "auth",
            "secret",
            "ssn",
            "rrn",
            "resident",
            "residentNo",
            "accountNo",
            "accountNumber",
            "cardNo",
            "cardNumber",
            "pin",
            "otp"
    );

    private static final List<Pattern> JSON_STRING_PATTERNS = SENSITIVE_KEYS.stream()
            .map(key -> Pattern.compile("(?i)(\"" + key + "\"\\s*:\\s*\")[^\"]*(\")"))
            .toList();

    private static final List<Pattern> KEY_VALUE_PATTERNS = SENSITIVE_KEYS.stream()
            .map(key -> Pattern.compile("(?i)(\\b" + key + "\\b\\s*[=:]\\s*)[^,\\]\\}&]+"))
            .toList();

    private SensitiveDataMasker() {
    }

    public static String mask(String value) {
        return mask(value, DEFAULT_MAX_LENGTH);
    }

    public static String mask(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String masked = value;
        for (Pattern pattern : JSON_STRING_PATTERNS) {
            masked = pattern.matcher(masked).replaceAll("$1***$2");
        }
        for (Pattern pattern : KEY_VALUE_PATTERNS) {
            masked = pattern.matcher(masked).replaceAll("$1***");
        }

        masked = Pattern.compile("(?i)(Bearer\\s+)[A-Za-z0-9._~+/=-]+")
                .matcher(masked)
                .replaceAll("$1***");

        return truncate(masked, maxLength);
    }

    public static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...(truncated)";
    }
}
