package com.cpf.education.integration.counterparty.application;
import java.util.Map;
/** EducationCounterpartyResult 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record EducationCounterpartyResult(int httpStatus, boolean replayed, Map<String,Object> body) { }
