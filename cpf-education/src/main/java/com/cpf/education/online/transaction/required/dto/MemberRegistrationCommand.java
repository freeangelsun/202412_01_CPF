package com.cpf.education.online.transaction.required.dto;
/** MemberRegistrationCommand는 여러 Service가 하나의 REQUIRED Local Transaction에 참여하는 Transaction Golden Path입니다. */
public record MemberRegistrationCommand(String memberId,String profileId,boolean failProfile) { }
