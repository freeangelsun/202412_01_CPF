package com.cpf.member.sampleitem.dto;
/** 논리 삭제 결과입니다. */
public record MemberDeleteResult(boolean deleted,long sampleItemId,long deletedVersion){}