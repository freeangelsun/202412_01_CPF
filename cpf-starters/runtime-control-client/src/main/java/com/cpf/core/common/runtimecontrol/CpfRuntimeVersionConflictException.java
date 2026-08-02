package com.cpf.core.common.runtimecontrol;

/**
 * @deprecated 외부 Consumer는 {@link com.cpf.core.api.runtimecontrol.CpfRuntimeVersionConflictException}을 사용합니다.
 */
@Deprecated(forRemoval = true)
public class CpfRuntimeVersionConflictException extends com.cpf.core.api.runtimecontrol.CpfRuntimeVersionConflictException {
    public CpfRuntimeVersionConflictException(long expectedVersion, long actualVersion) {
        super(expectedVersion, actualVersion);
    }
}
