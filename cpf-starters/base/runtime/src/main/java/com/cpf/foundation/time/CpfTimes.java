package com.cpf.foundation.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 기술중립 시각 문자열 Utility입니다.
 *
 * <p>거래 시각 정책이나 테스트 Clock 주입이 필요한 로직은 {@link CpfTimeOperations}를 사용합니다.
 * 이 클래스는 화면/샘플용 표준 문자열 변환만 제공합니다.</p>
 */
public final class CpfTimes {
    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter DATE_TIME_MILLIS =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private CpfTimes() {
    }

    public static String today() {
        return LocalDate.now().format(DATE);
    }

    public static String nowDateTimeMillis() {
        return LocalDateTime.now().format(DATE_TIME_MILLIS);
    }
}
