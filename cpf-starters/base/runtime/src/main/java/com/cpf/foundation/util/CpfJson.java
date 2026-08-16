package com.cpf.foundation.util;

import com.cpf.core.api.error.CpfValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/** Jackson 세부 구현을 감춘 기술중립 JSON Utility입니다. */
public final class CpfJson {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };

    private CpfJson() {
    }

    public static Map<String, Object> map(String json) {
        if (json == null || json.isBlank()) {
            throw new CpfValidationException("json 값은 필수입니다.");
        }
        try {
            return MAPPER.readValue(json, MAP_TYPE);
        } catch (JsonProcessingException ex) {
            throw new CpfValidationException("유효한 JSON Object가 아닙니다.");
        }
    }

    public static String write(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new CpfValidationException("JSON 문자열로 변환할 수 없습니다.");
        }
    }
}
