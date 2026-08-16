package com.cpf.integration.http.api;

/**
 * transport 세부정보를 업무 코드에서 숨기는 typed 서비스 Client 계약입니다.
 *
 * @param <I> 요청 형식
 * @param <O> 응답 형식
 * @since 1.0.0
 */
@FunctionalInterface
public interface CpfServiceClient<
        I extends com.cpf.core.api.base.CpfRequest,
        O extends com.cpf.core.api.base.CpfResponse> {

    /**
     * 중앙 policy와 실행 context를 사용해 계약을 실행합니다.
     *
     * @param request 업무 요청
     * @return typed 업무 응답
     */
    O execute(I request);
}
