package com.cpf.admin.opr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "AdmBreakGlassOpenRequest", description = "범위와 TTL이 제한된 Break-glass 세션 발급 요청")
public record AdmBreakGlassOpenRequest(
        @NotBlank @Size(max = 64) String scopeType,
        @NotBlank @Size(max = 256) String scopeValue,
        @NotBlank @Size(min = 8, max = 500) String reason,
        @Min(1) @Max(120) int ttlMinutes) {
}
