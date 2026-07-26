package com.cpf.core.api.page;

/**
 * CPF 공통 서버 Paging 요청 계약입니다.
 *
 * <p>화면/업무 모듈이 제각각 page/size 규칙을 만들지 않도록 0-base page와 제한된 size를 표준화합니다.</p>
 */
public record CpfPageRequest(int page, int size) {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 200;

    public CpfPageRequest {
        if (page < 0) throw new IllegalArgumentException("page는 0 이상이어야 합니다.");
        if (size < 1 || size > MAX_SIZE) throw new IllegalArgumentException("size는 1~" + MAX_SIZE + " 범위여야 합니다.");
    }

    public static CpfPageRequest of(Integer page, Integer size) {
        return new CpfPageRequest(page == null ? 0 : page, size == null ? DEFAULT_SIZE : size);
    }

    public int offset() {
        return Math.multiplyExact(page, size);
    }
}
