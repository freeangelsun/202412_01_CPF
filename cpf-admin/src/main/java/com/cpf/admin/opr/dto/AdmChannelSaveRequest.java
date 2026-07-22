package com.cpf.admin.opr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** ADM 채널 등록·수정 요청입니다. */
public record AdmChannelSaveRequest(
        @NotBlank @Size(max = 100) String channelName,
        @NotBlank @Pattern(regexp = "[A-Z][A-Z0-9_]{1,29}") String channelType,
        @NotBlank @Pattern(regexp = "[A-Z][A-Z0-9_]{1,29}") String trustLevel,
        boolean clientChannel,
        boolean internalChannel,
        boolean authenticationRequired,
        boolean signatureRequired,
        boolean active,
        @Size(max = 500) String description,
        @NotBlank @Size(max = 500) String reason,
        @Size(max = 100) String requestUser) {
}
