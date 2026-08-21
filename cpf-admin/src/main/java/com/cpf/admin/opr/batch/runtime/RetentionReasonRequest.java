package com.cpf.admin.opr.batch.runtime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RetentionReasonRequest", description = "Retention 승인 요청의 감사 사유")
public record RetentionReasonRequest(@NotBlank @Size(min=5,max=500) String reason) {}
