package com.cpf.education.online.cache.dto;
/** MemberProfileResponse는 CpfCache의 getOrLoad와 명시적 무효화를 사용하는 Cache Golden Path입니다. */
public record MemberProfileResponse(String memberId, String displayName) { }
