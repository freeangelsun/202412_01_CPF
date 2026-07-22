package com.cpf.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 날짜와 시간 처리 공통 유틸리티입니다.
 *
 * <p>업무 화면, 로그, 채번, 전문 처리에서 반복해서 사용하는 표준 날짜 문자열을 제공합니다.</p>
 */
public final class DateTimeUtils {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter DATETIME_MILLIS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private DateTimeUtils() {
    }

    /**
     * 오늘 날짜를 yyyyMMdd 형식으로 반환합니다.
     *
     * @return 오늘 날짜 문자열
     */
    public static String today() {
        return LocalDate.now().format(DATE_FORMAT);
    }

    /**
     * 현재 일시를 yyyyMMddHHmmss 형식으로 반환합니다.
     *
     * @return 현재 일시 문자열
     */
    public static String nowDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMAT);
    }

    /**
     * 현재 일시를 yyyyMMddHHmmssSSS 형식으로 반환합니다.
     *
     * @return 밀리초 포함 현재 일시 문자열
     */
    public static String nowDateTimeMillis() {
        return LocalDateTime.now().format(DATETIME_MILLIS_FORMAT);
    }

    /**
     * yyyyMMdd 형식 문자열을 LocalDate로 변환합니다.
     *
     * @param value yyyyMMdd 문자열
     * @return 변환된 날짜
     */
    public static LocalDate parseDate(String value) {
        return LocalDate.parse(TextUtils.requireText(value, "date"), DATE_FORMAT);
    }
}

