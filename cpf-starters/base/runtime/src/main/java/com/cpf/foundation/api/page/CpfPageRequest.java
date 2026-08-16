package com.cpf.foundation.api.page;

/** Framework 공통 서버 Paging 요청 Value입니다. 기술/transport에 독립적이므로 Base Runtime이 소유합니다. */
public record CpfPageRequest(int page, int size) {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 500;
    public CpfPageRequest {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size <= 0 || size > MAX_SIZE) throw new IllegalArgumentException("size must be between 1 and " + MAX_SIZE);
    }
    /** of 작업을 CPF 표준 계약에 따라 수행한다. */
    public static CpfPageRequest of(Integer page, Integer size) {
        return new CpfPageRequest(page == null ? 0 : Math.max(0, page), size == null ? DEFAULT_SIZE : Math.max(1, Math.min(size, MAX_SIZE)));
    }
    public long offset() { return (long) page * size; }
}
