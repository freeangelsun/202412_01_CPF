package com.cpf.common.mqe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.core.common.exception.CpfSystemException;
import org.springframework.stereotype.Component;

import java.util.Map;

/** 메시지 payload를 Jackson 기반 JSON으로 직렬화·역직렬화합니다. */
@Component
public class CmnMessageCodec {
    private final ObjectMapper objectMapper;

    public CmnMessageCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** 객체를 브로커 전송용 JSON 문자열로 변환합니다. */
    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new CpfSystemException("메시지 payload를 JSON으로 직렬화하지 못했습니다.", ex);
        }
    }

    /** JSON 문자열을 지정한 메시지 타입으로 복원합니다. */
    public <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw new CpfSystemException("JSON 메시지 payload를 대상 타입으로 역직렬화하지 못했습니다.", ex);
        }
    }

    /** 객체를 메시지 속성 조회에 사용할 Map 구조로 변환합니다. */
    public Map<String, Object> toMap(Object value) {
        return objectMapper.convertValue(value, new TypeReference<>() {
        });
    }
}

