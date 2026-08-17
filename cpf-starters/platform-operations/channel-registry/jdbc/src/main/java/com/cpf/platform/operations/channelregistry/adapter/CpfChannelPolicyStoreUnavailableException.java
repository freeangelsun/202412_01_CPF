package com.cpf.platform.operations.channelregistry.adapter;

/** Channel Policy canonical store를 읽을 수 없을 때 fail-open을 방지하는 명시적 오류입니다. */
public final class CpfChannelPolicyStoreUnavailableException extends IllegalStateException {
    public CpfChannelPolicyStoreUnavailableException(String message) { super(message); }
    public CpfChannelPolicyStoreUnavailableException(String message, Throwable cause) { super(message, cause); }
}
