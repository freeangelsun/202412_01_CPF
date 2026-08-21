package com.cpf.admin.opr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "AdmBreakGlassReviewRequest", description = "Break-glass 사후검토 요청")
public record AdmBreakGlassReviewRequest(
        @NotBlank @Size(max = 32) String status,
        @NotBlank @Size(min = 8, max = 500) String reason) {
}
