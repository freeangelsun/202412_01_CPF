package com.cpf.core.api.page;
/**
 * CPF 공통 0-base 서버 Paging 요청 계약. 값 객체이므로 불변이며 thread-safe하다.
 * <p>생성 시 범위를 검증하며 DB 조회 offset 계산 외 상태 변경이나 transaction side effect가 없다.</p>
 * @param page 0 이상의 페이지 번호
 * @param size 1 이상 {@link #MAX_SIZE} 이하 페이지 크기
 */
public record CpfPageRequest(int page, int size) {
 public static final int DEFAULT_SIZE=20; public static final int MAX_SIZE=200;
 /** Paging 값을 검증한다. @param page 0 이상의 페이지 번호 @param size 1~200 크기 @throws IllegalArgumentException 범위 위반 시. side effect/transaction은 없다. */
 public CpfPageRequest {if(page<0)throw new IllegalArgumentException("page는 0 이상이어야 합니다.");if(size<1||size>MAX_SIZE)throw new IllegalArgumentException("size는 1~"+MAX_SIZE+" 범위여야 합니다.");}
 /** nullable 입력을 기본값으로 정규화한다. @param page null이면 0 @param size null이면 20 @return 검증된 비-null 요청 @throws IllegalArgumentException 명시 값이 범위를 벗어나면 발생. */
 public static CpfPageRequest of(Integer page,Integer size){return new CpfPageRequest(page==null?0:page,size==null?DEFAULT_SIZE:size);}
 /** DB 조회용 offset을 계산한다. @return page*size @throws ArithmeticException int overflow 시. side effect는 없다. */
 public int offset(){return Math.multiplyExact(page,size);}
}
