package com.cpf.batch.edu.ondemand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** 온라인에서 접수하는 온디맨드 배치 실행 요청입니다. */
public record BatOnDemandRequest(
        @NotBlank @Pattern(regexp = "^B[A-Z]{3}[A-Z0-9]{2}[0-9]{4}$") String standardBatchId,
        @NotBlank @Pattern(regexp = "^[0-9]{8}$") String businessDate,
        @NotBlank @Size(max = 120) String idempotencyKey,
        @NotBlank @Size(max = 500) String reason,
        @NotBlank @Size(max = 100) String requestUser,
        Map<String, Object> parameters) {

    public BatOnDemandRequest {
        parameters = immutableNullableMap(parameters);
    }

    private static Map<String, Object> immutableNullableMap(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}
