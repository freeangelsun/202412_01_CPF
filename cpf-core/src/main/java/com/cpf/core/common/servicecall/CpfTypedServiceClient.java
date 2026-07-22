package com.cpf.core.common.servicecall;

import com.cpf.core.common.base.CpfRequest;
import com.cpf.core.common.base.CpfResponse;
import com.cpf.core.common.base.CpfServiceClient;

/**
 * local facade와 remote adapter가 동일하게 구현하는 typed 서비스 Client 계약입니다.
 *
 * @param <I> 요청 형식
 * @param <O> 응답 형식
 * @since 1.0.0
 */
public interface CpfTypedServiceClient<I extends CpfRequest, O extends CpfResponse>
        extends CpfServiceClient<I, O> {

    /**
     * 승인된 named policy를 사용해 호출합니다.
     *
     * @param request 업무 요청
     * @param options 호출 옵션
     * @return typed 업무 응답
     */
    O execute(I request, CpfServiceCallOptions options);

    /**
     * 제품 기본 조회 정책으로 호출합니다.
     *
     * @param request 업무 요청
     * @return typed 업무 응답
     */
    @Override
    default O execute(I request) {
        return execute(request, CpfServiceCallOptions.defaultQuery());
    }
}
