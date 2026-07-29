package com.cpf.member.sampleitem.dto;

import java.time.Instant;

/** 변경 요청 Hash와 결과를 보존하는 Generated Domain 멱등 원장 DTO입니다. */
public record MemberIdempotencyEntry(
        String idempotencyKey,
        String operationCode,
        String requestHash,
        long sampleItemId,
        long resultVersion,
        String deletedYn,
        String transactionId,
        Instant createdAt) {
    public boolean deleted(){ return "Y".equalsIgnoreCase(deletedYn); }
}