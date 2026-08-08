package com.cpf.core.api.page;

/** CPF 공개 정렬 방향입니다. API/EDU/Generated Domain에서 같은 값을 사용합니다. */
public enum CpfSortDirection {
    ASC, DESC;

    /** null/blank 입력은 ASC로 안전하게 정규화합니다.
     * @param value ASC/DESC 문자열. null/blank이면 ASC
     * @return 정규화된 정렬 방향
     * @throws IllegalArgumentException ASC/DESC 이외 문자열인 경우
     */
    public static CpfSortDirection from(String value) {
        if (value == null || value.isBlank()) return ASC;
        if ("ASC".equalsIgnoreCase(value.trim())) return ASC;
        if ("DESC".equalsIgnoreCase(value.trim())) return DESC;
        throw new IllegalArgumentException("정렬 방향은 ASC 또는 DESC여야 합니다: " + value);
    }
}
