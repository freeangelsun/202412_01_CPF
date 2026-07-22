package com.cpf.common.message.fixedlength;

import java.util.List;
import java.util.Map;

/**
 * 고정길이 전문 parse 결과입니다.
 *
 * @param rawMessage 원문 전문
 * @param fields 필드별 원문 값
 * @param maskedFields 필드별 마스킹 값
 * @param errors 검증 오류 목록
 * @param groups 반복부 원문 값
 * @param maskedGroups 반복부 마스킹 값
 * @param fieldErrors 구조화된 필드 오류 목록
 */
public record FixedLengthParseResult(
        String rawMessage,
        Map<String, String> fields,
        Map<String, String> maskedFields,
        List<String> errors,
        Map<String, List<Map<String, String>>> groups,
        Map<String, List<Map<String, String>>> maskedGroups,
        List<FixedLengthMessageError> fieldErrors) {

    public FixedLengthParseResult(String rawMessage,
                                  Map<String, String> fields,
                                  Map<String, String> maskedFields,
                                  List<String> errors) {
        this(rawMessage, fields, maskedFields, errors, Map.of(), Map.of(), List.of());
    }

    public boolean valid() {
        return errors == null || errors.isEmpty();
    }
}
