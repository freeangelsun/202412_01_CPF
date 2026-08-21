package com.cpf.admin.opr.batch.runtime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RetentionVersionedReasonRequest", description = "Retention 상태 변경 요청. expectedVersion은 BAT Owner CAS 기준입니다.")
public record RetentionVersionedReasonRequest(
        @Min(0) long expectedVersion,
        @NotBlank @Size(min=5,max=500) String reason) {}
