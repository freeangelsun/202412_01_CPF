package com.cpf.core.api.domain;

import com.cpf.core.api.base.CpfResponse;
import java.time.Instant;

/** Generator Domain Call Golden Path가 실제 Consumer까지 검증하는 최소 typed 응답입니다. */
public record CpfDomainPingResponse(String systemCode, String requestId, Instant processedAt) implements CpfResponse {
    public CpfDomainPingResponse {
        if (systemCode == null || systemCode.isBlank()) throw new IllegalArgumentException("systemCode는 필수입니다.");
        if (requestId == null || requestId.isBlank()) throw new IllegalArgumentException("requestId는 필수입니다.");
        if (processedAt == null) throw new IllegalArgumentException("processedAt은 필수입니다.");
    }
}
