package com.cpf.core.common.runtimecontrol;

/** 오래된 leader/agent의 적용을 차단하기 위한 fencing 예외입니다. */
public class CpfRuntimeFenceException extends RuntimeException {
    public CpfRuntimeFenceException(String message){super(message);}
}
