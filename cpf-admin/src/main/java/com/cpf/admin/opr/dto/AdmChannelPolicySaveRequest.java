package com.cpf.admin.opr.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/** ADM 업무 Operation별 Caller Channel 정책 등록·수정 요청입니다. */
public record AdmChannelPolicySaveRequest(
        @NotBlank @Pattern(regexp = "(?:\\*|[A-Za-z][A-Za-z0-9_.:-]{2,159})") String operationId,
        @NotBlank @Pattern(regexp = "(?:ANY|[A-Z][A-Z0-9_]{1,29})") String callerChannel,
        boolean allowed,
        boolean authenticationRequired,
        boolean signatureRequired,
        @Min(0) @Max(1_000_000) int maxTps,
        Instant effectiveFrom,
        Instant effectiveTo,
        boolean active,
        @NotBlank @Size(max = 500) String reason,
        @Size(max = 100) String requestUser) {
}
