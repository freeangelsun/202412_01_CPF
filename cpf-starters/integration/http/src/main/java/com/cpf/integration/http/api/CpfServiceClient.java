package com.cpf.integration.http.api;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;

/**
 * @deprecated Public 업무 계약은 {@link com.cpf.integration.api.http.CpfServiceClient}를 사용합니다.
 * 이 Alias는 기존 Provider/Consumer 이전 기간에만 유지하며 신규 Source에서 직접 사용하지 않습니다.
 */
@Deprecated(forRemoval = true)
@FunctionalInterface
public interface CpfServiceClient<I extends CpfRequest, O extends CpfResponse>
        extends com.cpf.integration.api.http.CpfServiceClient<I, O> {
}
