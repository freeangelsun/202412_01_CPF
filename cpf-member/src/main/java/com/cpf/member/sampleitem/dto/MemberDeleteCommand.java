package com.cpf.member.sampleitem.dto;
import jakarta.validation.constraints.PositiveOrZero;
/** 낙관적 잠금 삭제 입력입니다. */
public record MemberDeleteCommand(@PositiveOrZero long expectedVersion){}