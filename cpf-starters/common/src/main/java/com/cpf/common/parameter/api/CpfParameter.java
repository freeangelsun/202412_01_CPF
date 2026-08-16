package com.cpf.common.parameter.api;

/** CPF Common parameter metadata. Raw encrypted database payload is never exposed through this API. */
/** CpfParameter 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfParameter(long id, String key, String type, String value, String description, boolean encrypted, boolean active) { }
