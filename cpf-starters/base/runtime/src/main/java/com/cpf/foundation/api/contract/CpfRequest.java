package com.cpf.foundation.api.contract;

/**
 * 기존 Foundation 요청 표식의 호환 계약입니다.
 *
 * @deprecated 신규 Public 계약은 {@link com.cpf.core.api.base.CpfRequest}를 사용합니다.
 */
@Deprecated(forRemoval = false)
public interface CpfRequest extends com.cpf.core.api.base.CpfRequest {}
