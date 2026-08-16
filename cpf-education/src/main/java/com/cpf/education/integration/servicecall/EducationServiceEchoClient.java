package com.cpf.education.integration.servicecall;

import com.cpf.integration.http.api.CpfServiceClient;

/**
 * EDU 자체 중립 시뮬레이터를 호출하는 공개 typed service client 계약입니다.
 * 업무 코드는 URI, HTTP method, timeout/retry/circuit 같은 transport 세부정보를 알지 않습니다.
 */
public interface EducationServiceEchoClient
        extends CpfServiceClient<EducationServiceEchoRequest, EducationServiceEchoResponse> {
}
