package com.cpf.account.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** ACC 계정 등록 요청입니다. */
public record AccAccountCreateRequest(
        @NotBlank @Size(max = 50) String accountNo,
        @NotBlank @Size(max = 150) String accountName,
        @Email @Size(max = 200) String email) {
}
