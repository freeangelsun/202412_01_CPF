package com.cpf.common.sample;

import java.time.Instant;

/**
 * CMN 단일 DB 검증 테이블의 공개 Sample DTO입니다.
 *
 * <p>고객 업무 원장이 아니라 CPF 연결·Migration·CRUD·Paging·낙관적 잠금과
 * Transaction 동작을 검증하기 위한 선택형 계약입니다.</p>
 */
public record CmnSampleItem(
        long sampleItemId,
        String sampleKey,
        String itemName,
        String categoryCode,
        String statusCode,
        String searchableText,
        String ownerReference,
        long sortOrder,
        long version,
        Instant createdAt,
        Instant updatedAt) {
}
