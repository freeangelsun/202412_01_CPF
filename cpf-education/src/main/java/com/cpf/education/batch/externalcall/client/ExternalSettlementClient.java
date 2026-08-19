package com.cpf.education.batch.externalcall.client;

import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.http.CpfRestClient;
import java.util.Map;

/** 기관 호출 Transport를 CPF Boundary Result로 보존하는 Batch Client입니다. */
public final class ExternalSettlementClient {
    private final CpfRestClient rest;
    public ExternalSettlementClient(CpfRestClient rest) { this.rest = rest; }
    public CpfResult<String> settle(Map<String,Object> payload, String idempotencyKey) {
        return rest.exchangeResult("settlement-agency", "POST", b -> b.path("/batch").build(), payload,
                Map.of("X-Idempotency-Key", idempotencyKey), String.class);
    }
}
