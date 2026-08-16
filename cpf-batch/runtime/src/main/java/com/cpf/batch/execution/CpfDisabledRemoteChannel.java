package com.cpf.batch.execution;

import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;

/** 승인된 Remote Transport가 없을 때 같은 JVM으로 조용히 fallback하지 않는 fail-closed Channel입니다. */
final class CpfDisabledRemoteChannel implements PollableChannel {
    @Override public boolean send(Message<?> message) {
        throw new IllegalStateException("CPF_BATCH_REMOTE_TRANSPORT_NOT_CONFIGURED");
    }
    @Override public boolean send(Message<?> message, long timeout) {
        throw new IllegalStateException("CPF_BATCH_REMOTE_TRANSPORT_NOT_CONFIGURED");
    }
    @Override public Message<?> receive() {
        throw new IllegalStateException("CPF_BATCH_REMOTE_TRANSPORT_NOT_CONFIGURED");
    }
    @Override public Message<?> receive(long timeout) {
        throw new IllegalStateException("CPF_BATCH_REMOTE_TRANSPORT_NOT_CONFIGURED");
    }
}
