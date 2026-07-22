package com.cpf.reference.query.dto;

import java.util.List;

/**
 * offset 기반 페이징 응답 샘플입니다.
 *
 * @param items   조회 결과
 * @param page    1부터 시작하는 현재 페이지
 * @param size    페이지 크기
 * @param total   전체 건수
 * @param hasNext 다음 페이지 존재 여부
 */
public record ReferenceQueryPageResponse<T>(
        List<T> items,
        int page,
        int size,
        long total,
        boolean hasNext) {
}
