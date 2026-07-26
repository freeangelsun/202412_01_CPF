package com.cpf.reference.servicecall;

import com.cpf.core.common.servicecall.CpfTypedServiceClient;

/**
 * REF 자체 중립 시뮬레이터를 호출하는 typed client입니다.
 *
 * <p>업무 코드는 대상 URI, HTTP method, timeout, retry와 transport adapter를 알지 않습니다.
 * 특정 Generated Domain 없이도 같은 공개 호출 계약을 학습할 수 있습니다.</p>
 */
public interface ReferenceServiceEchoClient
        extends CpfTypedServiceClient<ReferenceServiceEchoRequest, ReferenceServiceEchoResponse> {
}
