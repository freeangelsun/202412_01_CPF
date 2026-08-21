package com.cpf.admin.opr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "AdmMaintenanceApprovalRequiredRequest", description = "Service instance 변경 승인 경로 안내 요청")
public record AdmMaintenanceApprovalRequiredRequest(
        @NotBlank @Size(max = 256) String instanceId,
        @NotBlank @Size(min = 8, max = 500) String reason) {
}
