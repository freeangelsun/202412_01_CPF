package com.cpf.education.online.concurrency.model;
/** 동시성 제어 교육 예제의 Model 역할과 CPF 표준 사용 경계를 보여줍니다. */
public record MemberVersion(String memberId,String value,long version) { }
