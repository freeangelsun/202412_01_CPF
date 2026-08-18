package com.cpf.education.online.domaincall.dto;
import com.cpf.core.api.base.CpfResponse;
/** EXS 신용조회 Domain Contract 응답입니다. */
public record CreditCheckResponse(String status) implements CpfResponse { }
