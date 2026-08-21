package com.cpf.admin.opr.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Schema(name = "AdmDataQualityValidationRequest", description = "동적 업무 레코드의 데이터 품질 검증 요청. 최대 256개 field를 허용합니다.")
public final class AdmDataQualityValidationRequest {
    private static final int MAX_FIELDS = 256;
    private final Map<String,Object> fields = new LinkedHashMap<>();

    @JsonAnySetter
    public void put(String key, Object value) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("data-quality field name is required");
        if (!fields.containsKey(key) && fields.size() >= MAX_FIELDS) {
            throw new IllegalArgumentException("data-quality field count exceeds " + MAX_FIELDS);
        }
        fields.put(key, value);
    }

    @JsonAnyGetter
    public Map<String,Object> fields() {
        return Collections.unmodifiableMap(fields);
    }

    public Map<String,Object> toMap() { return fields(); }
}
