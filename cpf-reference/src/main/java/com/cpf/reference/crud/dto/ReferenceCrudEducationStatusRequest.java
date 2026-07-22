package com.cpf.reference.crud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * REF CRUD 교육 항목 상태 변경 요청 DTO입니다.
 *
 * @param statusCode 변경할 상태 코드
 * @param requestUser 요청 사용자
 */
public record ReferenceCrudEducationStatusRequest(
        @NotBlank(message = "상태 코드는 필수입니다.")
        @Pattern(regexp = "^(ACTIVE|INACTIVE|READY|CLOSED)$", message = "상태 코드는 ACTIVE, INACTIVE, READY, CLOSED 중 하나여야 합니다.")
        String statusCode,

        @Size(max = 100, message = "요청 사용자는 100자 이하로 입력해야 합니다.")
        String requestUser) {
}
