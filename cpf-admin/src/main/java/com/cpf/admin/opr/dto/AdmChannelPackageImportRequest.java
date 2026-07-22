package com.cpf.admin.opr.dto;

import com.cpf.core.channel.model.CpfChannelPolicyPackage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** ADM 채널 정책 패키지 반입 요청입니다. */
public record AdmChannelPackageImportRequest(
        @NotNull @Valid CpfChannelPolicyPackage policyPackage,
        boolean dryRun,
        @NotBlank @Size(max = 500) String reason,
        @Size(max = 100) String requestUser) {
}
