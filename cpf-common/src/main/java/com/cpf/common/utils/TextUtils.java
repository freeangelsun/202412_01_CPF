package com.cpf.common.utils;

import java.util.Locale;

/**
 * Common text helpers used by framework and business modules.
 */
public final class TextUtils {

    private TextUtils() {
    }

    /**
     * Returns true when the value is not null and contains at least one non-blank character.
     */
    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Returns the default value when the source value is null or blank.
     */
    public static String defaultIfBlank(String value, String defaultValue) {
        return hasText(value) ? value : defaultValue;
    }

    /**
     * Returns trimmed text or throws when the value is null or blank.
     */
    public static String requireText(String value, String fieldName) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    /**
     * Normalizes a code value to upper-case trimmed text. Null and blank values become an empty string.
     */
    public static String normalizeCode(String value) {
        return hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : "";
    }
}
