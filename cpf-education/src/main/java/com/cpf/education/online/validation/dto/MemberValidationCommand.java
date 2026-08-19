package com.cpf.education.online.validation.dto;
import jakarta.validation.constraints.*;
/** Validation·표준 오류 교육 예제의 DTO 역할과 CPF 표준 사용 경계를 보여줍니다. */
public record MemberValidationCommand(@NotBlank String memberId,@Size(min=2,max=40) String name,@NotBlank String scenario) { }
