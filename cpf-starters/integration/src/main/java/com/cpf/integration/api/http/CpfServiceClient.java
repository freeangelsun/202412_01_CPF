package com.cpf.integration.api.http;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;

/**
 * 배포 위치나 HTTP Provider 구현에 종속되지 않는 typed 외부 서비스 Client 계약입니다.
 * Generated Domain은 이 Public 계약만 컴파일하며 실제 HTTP 전송은 Starter가 조립합니다.
 *
 * @param <I> 요청 형식
 * @param <O> 응답 형식
 * @since 1.0.0
 */
@FunctionalInterface
public interface CpfServiceClient<I extends CpfRequest, O extends CpfResponse> {
    /** 중앙 정책과 실행 Context를 사용해 외부 계약을 실행합니다. */
    O execute(I request);
}
