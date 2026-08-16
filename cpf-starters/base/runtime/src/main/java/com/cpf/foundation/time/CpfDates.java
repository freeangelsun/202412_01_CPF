package com.cpf.foundation.time;

import com.cpf.core.api.error.CpfValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** 기술중립 날짜 변환 Utility입니다. */
public final class CpfDates {
    private static final DateTimeFormatter BASIC = DateTimeFormatter.BASIC_ISO_DATE;

    private CpfDates() {
    }

    public static LocalDate parse(String value) {
        if (value == null || value.isBlank()) {
            throw new CpfValidationException("date 값은 필수입니다.");
        }
        String normalized = value.trim();
        try {
            return normalized.indexOf('-') >= 0
                    ? LocalDate.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE)
                    : LocalDate.parse(normalized, BASIC);
        } catch (DateTimeParseException ex) {
            throw new CpfValidationException("date 형식은 yyyyMMdd 또는 yyyy-MM-dd 이어야 합니다.");
        }
    }

    public static String formatBasic(LocalDate value) {
        if (value == null) {
            throw new CpfValidationException("date 값은 필수입니다.");
        }
        return value.format(BASIC);
    }
}
