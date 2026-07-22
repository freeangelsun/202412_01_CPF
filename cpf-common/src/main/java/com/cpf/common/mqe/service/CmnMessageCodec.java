package cpf.cmn.mqe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cpf.pfw.common.exception.CpfSystemException;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * CPF 기능 설명입니다.
 */
@Component
public class CmnMessageCodec {
    private final ObjectMapper objectMapper;

    public CmnMessageCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다. */
    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new CpfSystemException("CPF 처리 기준입니다.", ex);
        }
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    public <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw new CpfSystemException("CPF 처리 기준입니다.", ex);
        }
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    public Map<String, Object> toMap(Object value) {
        return objectMapper.convertValue(value, new TypeReference<>() {
        });
    }
}

