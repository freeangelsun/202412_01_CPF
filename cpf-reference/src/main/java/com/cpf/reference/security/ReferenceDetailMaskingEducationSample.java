package com.cpf.reference.security;

/**
 * 목록/상세 화면에서 민감정보를 마스킹하는 전용 샘플입니다.
 */
public class ReferenceDetailMaskingEducationSample {

    public String name(String value) {
        if (value == null || value.length() < 2) {
            return "*";
        }
        return value.charAt(0) + "*".repeat(value.length() - 1);
    }
}
