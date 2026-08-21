package com.cpf.admin.opr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "AdmReasonRequest", description = "감사 가능한 운영 사유")
public record AdmReasonRequest(@NotBlank @Size(min = 8, max = 500) String reason) {
}
