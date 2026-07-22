package com.cpf.common.message.fixedlength;

import java.util.Map;
import java.util.List;

/**
 * 고정길이 전문 format 결과입니다.
 *
 * @param message 생성된 전문
 * @param byteLength 전문 byte 길이
 * @param maskedFields 필드별 마스킹 값
 * @param maskedGroups 반복부 필드별 마스킹 값
 */
public record FixedLengthFormatResult(
        String message,
        int byteLength,
        Map<String, String> maskedFields,
        Map<String, List<Map<String, String>>> maskedGroups) {

    public FixedLengthFormatResult(String message, int byteLength, Map<String, String> maskedFields) {
        this(message, byteLength, maskedFields, Map.of());
    }
}
