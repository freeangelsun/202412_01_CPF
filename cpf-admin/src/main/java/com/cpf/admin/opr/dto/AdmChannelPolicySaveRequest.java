package com.cpf.admin.opr.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/** ADM 표준 실행별 채널 정책 등록·수정 요청입니다. */
public record AdmChannelPolicySaveRequest(
        @NotBlank @Pattern(regexp = "(?:\\*|[OSB][A-Z]{3}[A-Z0-9]{2}[0-9]{4})") String standardExecutionId,
        @NotBlank @Pattern(regexp = "[A-Z][A-Z0-9_]{1,29}") String originalChannelCode,
        @NotBlank @Pattern(regexp = "[A-Z][A-Z0-9_]{1,29}") String callerChannelCode,
        @NotBlank @Pattern(regexp = "[A-Z*][A-Z0-9_*]{0,29}") String requestType,
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
