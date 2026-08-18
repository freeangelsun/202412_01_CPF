package com.cpf.education.online.transaction.required.repository;
/** MemberRegistrationRecord는 여러 Service가 하나의 REQUIRED Local Transaction에 참여하는 Transaction Golden Path입니다. */
public record MemberRegistrationRecord(String id,String type,String value) { }
