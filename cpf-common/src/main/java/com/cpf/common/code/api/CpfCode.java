package com.cpf.common.code.api;

/** CPF Common code value. */
/** CpfCode 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfCode(long id, Long parentId, String group, String value, String description, boolean active) { }
