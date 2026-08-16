package com.cpf.foundation.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 프레임워크 공통 시각 문자열 유틸리티입니다.
 *
 * <p>Core 소유 유틸리티를 제거한 뒤 Base Runtime이 소유하는 경량 유틸리티입니다.
 * 거래 시간/클록 주입이 필요한 업무 로직은 {@code com.cpf.foundation.time} 계약을 사용하고,
 * 이 클래스는 기존 문자열 시각 포맷 호환 용도로만 사용합니다.</p>
 */
public final class CpfTimes {
    private static final DateTimeFormatter DATE_TIME_MILLIS =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private CpfTimes() {
    }

    public static String nowDateTimeMillis() {
        return LocalDateTime.now().format(DATE_TIME_MILLIS);
    }
}
