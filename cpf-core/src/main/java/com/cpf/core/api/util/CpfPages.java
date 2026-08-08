package com.cpf.core.api.util;

import com.cpf.core.api.page.CpfPage;
import com.cpf.core.api.page.CpfPageRequest;
import com.cpf.core.api.page.CpfSlice;
import java.util.List;

/** CPF 표준 Page/Slice 생성 편의 API입니다. */
public final class CpfPages {
    private CpfPages() {}
    /** 공통 0-base Paging 요청을 생성하고 범위를 검증합니다.
     * @param page 0 이상의 page 번호
     * @param size 1 이상의 page/chunk 크기
     * @return 검증된 CPF 값 객체
     * @throws IllegalArgumentException page/size 범위가 유효하지 않은 경우
     */
    public static CpfPageRequest request(int page, int size) { return CpfPageRequest.of(page, size); }
    /** 조회 목록과 전체 건수로 표준 CpfPage를 조립합니다.
     * @param items 조회 결과 목록
     * @param request null이 아닌 검증된 Paging 요청
     * @param total 0 이상의 전체 건수
     * @return 검증된 CPF 값 객체
     * @throws IllegalArgumentException request 또는 metadata가 유효하지 않은 경우
     */
    public static <T> CpfPage<T> page(List<T> items, CpfPageRequest request, long total) { return CpfPage.of(items, request, total); }
        /** EDU/메모리 목록을 overflow-safe offset 방식으로 잘라 표준 Page를 만듭니다.
     * @param all 메모리 전체 목록. null이면 빈 목록
     * @param request null이 아닌 검증된 Paging 요청
     * @return 검증된 CPF 값 객체
     * @throws ArithmeticException offset이 int 범위를 넘는 경우
     */
    public static <T> CpfPage<T> offsetPage(List<T> all, CpfPageRequest request) {
        List<T> source = all == null ? List.of() : List.copyOf(all);
        int from = Math.min(source.size(), Math.toIntExact(request.offset()));
        int to = Math.min(source.size(), from + request.size());
        return CpfPage.of(source.subList(from, to), request, source.size());
    }
    /** look-ahead 조회 결과로 COUNT 없는 Slice를 만듭니다.
     * @param rowsWithLookAhead size+1건을 포함할 수 있는 목록
     * @param page 0 이상의 page 번호
     * @param size 1 이상의 page/chunk 크기
     * @return 검증된 CPF 값 객체
     * @throws IllegalArgumentException page/size가 유효하지 않은 경우
     */
    public static <T> CpfSlice<T> slice(List<T> rowsWithLookAhead, int page, int size) { return CpfSlice.fromLookAhead(rowsWithLookAhead, page, size); }
}
