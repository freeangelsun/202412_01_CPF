package com.cpf.education.online.domaincall.dto;
import com.cpf.core.api.base.CpfRequest;
/** EXS 신용조회 Domain Contract 요청입니다. */
public record CreditCheckRequest(String memberId) implements CpfRequest { }
