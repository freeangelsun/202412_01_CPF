package com.cpf.core.api.domain;

import com.cpf.core.api.base.CpfRequest;

/** Generator가 Local/Remote 동일 경로를 검증할 때 사용하는 최소 typed Domain 요청입니다. */
public record CpfDomainPingRequest(String requestId) implements CpfRequest {
    public CpfDomainPingRequest {
        if (requestId == null || requestId.isBlank()) throw new IllegalArgumentException("requestId는 필수입니다.");
    }
}
