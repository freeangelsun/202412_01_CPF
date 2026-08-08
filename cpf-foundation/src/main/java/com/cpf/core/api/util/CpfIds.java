package com.cpf.core.api.util;

import java.util.Locale;
import java.util.UUID;

/** 거래 ID가 아닌 일반 기술 식별자 생성을 제공하는 CPF API입니다. */
public final class CpfIds {
    private CpfIds() {}
    /** RFC-4122 형태 UUID 문자열을 생성합니다.
     * @return 정규화/변환된 문자열 또는 계약상 null
     */
    public static String uuid() { return UUID.randomUUID().toString(); }
    /** 하이픈 없는 32자리 UUID 문자열을 생성합니다.
     * @return 정규화/변환된 문자열 또는 계약상 null
     */
    public static String compactUuid() { return UUID.randomUUID().toString().replace("-", ""); }
        /** compactUuid와 동일한 legacy 호환 식별자를 생성합니다.
     * @return 정규화/변환된 문자열 또는 계약상 null
     */
    public static String uuid32() { return compactUuid(); }
    /** System code를 영문 대문자/숫자 3자리로 검증·정규화합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @return 정규화/변환된 문자열 또는 계약상 null
     * @throws IllegalArgumentException blank 또는 영문/숫자 3자리가 아닌 경우
     */
    public static String normalizeSystemCode(String value) {
        String normalized = requireText(value, "systemCode").toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9]{3}")) throw new IllegalArgumentException("systemCode는 영문/숫자 3자리여야 합니다.");
        return normalized;
    }
    /** 업무 거래 ID와 구분되는 임시 기술 식별자를 생성합니다.
     * @param prefix 임시 식별자 prefix. blank면 TMP
     * @return 정규화/변환된 문자열 또는 계약상 null
     */
    public static String temporaryId(String prefix) {
        String p = defaultIfBlank(prefix, "TMP").toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_-]", "");
        if (p.isBlank()) p = "TMP";
        if (p.length() > 12) p = p.substring(0, 12);
        return p + "-" + java.time.Clock.systemUTC().instant().toEpochMilli() + "-" + compactUuid().substring(0, 8);
    }
    private static String requireText(String value, String name) { if(value==null||value.isBlank()) throw new IllegalArgumentException(name+" must not be blank"); return value.trim(); }
    private static String defaultIfBlank(String value, String fallback) { return value==null||value.isBlank()?fallback:value; }
}
