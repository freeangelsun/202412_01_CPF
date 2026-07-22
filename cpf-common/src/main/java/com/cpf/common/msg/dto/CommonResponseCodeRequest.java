package com.cpf.common.msg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Common response code create/update request.
 */
@Data
public class CommonResponseCodeRequest {
    @NotBlank(message = "responseCode is required.")
    private String responseCode;

    @NotBlank(message = "messageCode is required.")
    private String messageCode;

    @NotBlank(message = "resultType is required.")
    private String resultType;

    @NotBlank(message = "moduleId is required.")
    private String moduleId;

    @NotBlank(message = "responseGroup is required.")
    private String responseGroup;

    @NotBlank(message = "sequenceNo is required.")
    private String sequenceNo;

    @NotNull(message = "httpStatus is required.")
    private Integer httpStatus;

    private String description;
    private String useYn = "Y";
    private String requestUser = "SYSTEM";
}

