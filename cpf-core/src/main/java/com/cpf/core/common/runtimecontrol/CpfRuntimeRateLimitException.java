package com.cpf.core.common.runtimecontrol;

/**
 * @deprecated 외부 Consumer는 {@link com.cpf.core.api.runtimecontrol.CpfRuntimeRateLimitException}을 사용합니다.
 */
@Deprecated(forRemoval = true)
public class CpfRuntimeRateLimitException extends com.cpf.core.api.runtimecontrol.CpfRuntimeRateLimitException {
    public CpfRuntimeRateLimitException(int limit) {
        super(limit);
    }
}
