package com.cpf.admin.opr.dto;

import java.time.Instant;
import java.util.List;

/**
 * ADM 상용 화면의 Server Paging 응답 계약입니다.
 *
 * @param items 현재 페이지 데이터
 * @param page 0부터 시작하는 페이지 번호
 * @param size 요청 페이지 크기
 * @param total 필터 적용 후 전체 건수
 * @param hasNext 다음 페이지 존재 여부
 * @param fetchedAt 조회 완료 시각
 * @param stale Owner Runtime 기준 최신성 보장 실패 여부
 * @param partial 다중 인스턴스 중 일부만 성공했는지 여부
 * @param errorCode 부분 실패 또는 stale 원인 코드
 */
public record AdmPageResponse<T>(
        List<T> items,
        int page,
        int size,
        long total,
        boolean hasNext,
        Instant fetchedAt,
        boolean stale,
        boolean partial,
        String errorCode) {

    public AdmPageResponse {
        items = items == null ? List.of() : List.copyOf(items);
        fetchedAt = fetchedAt == null ? Instant.now() : fetchedAt;
        errorCode = errorCode == null ? "" : errorCode;
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size < 1 || size > 200) throw new IllegalArgumentException("size must be between 1 and 200");
        if (total < 0) throw new IllegalArgumentException("total must be >= 0");
    }

    public static <T> AdmPageResponse<T> success(List<T> items, int page, int size, long total) {
        return new AdmPageResponse<>(items, page, size, total, (long) (page + 1) * size < total,
                Instant.now(), false, false, "");
    }
}
