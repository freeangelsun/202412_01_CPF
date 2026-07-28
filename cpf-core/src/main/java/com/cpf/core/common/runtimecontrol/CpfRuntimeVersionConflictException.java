package com.cpf.core.common.runtimecontrol;

/** Runtime Control Plane optimistic version 충돌입니다. */
public class CpfRuntimeVersionConflictException extends RuntimeException {
    private final long expectedVersion;
    private final long actualVersion;
    public CpfRuntimeVersionConflictException(long expectedVersion,long actualVersion){
        super("Runtime version conflict. expected="+expectedVersion+", actual="+actualVersion);
        this.expectedVersion=expectedVersion;this.actualVersion=actualVersion;
    }
    public long expectedVersion(){return expectedVersion;}
    public long actualVersion(){return actualVersion;}
}
