package com.cpf.account.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** ACC 계정 수정 요청입니다. */
public record AccAccountUpdateRequest(
        @NotBlank @Size(max = 150) String accountName,
        @Email @Size(max = 200) String email,
        @NotBlank @Size(max = 30) String statusCode,
        @NotNull @PositiveOrZero Long version) {
}
