package com.cpf.core.common.runtimecontrol;

/**
 * @deprecated 외부 Consumer는 {@link com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException}을 사용합니다.
 */
@Deprecated(forRemoval = true)
public class CpfRuntimeFenceException extends com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException {
    public CpfRuntimeFenceException(String message) {
        super(message);
    }
}
