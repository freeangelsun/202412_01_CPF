package com.cpf.member.sampleitem.dto;

import com.cpf.member.common.contract.MemberResponse;
import java.time.Instant;

/** Vendor와 무관한 Generated Domain Sample Item 논리 모델입니다. */
public record MemberSampleItem(
        long sampleItemId,
        String sampleKey,
        String itemName,
        String statusCode,
        long versionNo,
        String idempotencyKey,
        String transactionId,
        long transactionSequence,
        Instant transactionAt,
        String createdBy,
        Instant createdAt,
        String updatedBy,
        Instant updatedAt) implements MemberResponse {
}