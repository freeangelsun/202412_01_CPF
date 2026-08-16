package com.cpf.testkit.fault;

/** 검증 Profile에서만 발생하는 명시적 Fault Injection Exception입니다. */
public final class CpfInjectedFaultException extends RuntimeException {
    CpfInjectedFaultException(String message) { super(message); }
}
