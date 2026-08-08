package com.cpf.core.api.page;

import java.util.List;

/** CPF 공통 서버 Paging 응답 계약. */
public record CpfPage<T>(List<T> content, long totalElements, int page, int size) {
    public CpfPage {
        content = content == null ? List.of() : List.copyOf(content);
        if (totalElements < 0) throw new IllegalArgumentException("totalElements는 0 이상이어야 합니다.");
        if (page < 0) throw new IllegalArgumentException("page는 0 이상이어야 합니다.");
        if (size < 1) throw new IllegalArgumentException("size는 1 이상이어야 합니다.");
    }

    /** 전체 건수와 page size로 전체 페이지 수를 계산합니다.
     * @return totalElements가 0이면 0, 아니면 올림 계산된 페이지 수
     */
    public long totalPages() {
        return totalElements == 0 ? 0 : (totalElements + size - 1) / size;
    }

    /** 현재 페이지 뒤에 데이터가 남아 있는지 overflow 없이 판단합니다.
     * @return 다음 페이지가 있으면 true
     */
    public boolean hasNext() {
        return ((long) page + 1L) * size < totalElements;
    }

    /** JSON/API 소비자가 사용하는 표준 목록 이름입니다.
     * @return null이 아닌 불변 목록
     */
    public List<T> items() {
        return content;
    }

    /** 첫 Page가 아닌지 반환합니다.
     * @return page가 0보다 크면 true
     */
    public boolean hasPrevious() {
        return page > 0;
    }

    /** 표준 Page 요청과 조회 결과를 응답 계약으로 조립합니다.
     * @param content 조회 결과. null이면 빈 목록으로 정규화됩니다.
     * @param request null이 아닌 검증된 page/size 요청
     * @param totalElements 0 이상의 전체 건수
     * @return 불변 Paging 응답
     * @throws IllegalArgumentException request가 null이거나 metadata 범위가 잘못된 경우
     */
    public static <T> CpfPage<T> of(List<T> content, CpfPageRequest request, long totalElements) {
        if (request == null) {
            throw new IllegalArgumentException("request는 필수입니다.");
        }
        return new CpfPage<>(content, totalElements, request.page(), request.size());
    }
}
