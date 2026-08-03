package com.cpf.core.common.broker;

import com.cpf.core.common.logging.SensitiveDataMasker;

import java.util.Objects;

/** inbox 중복 방지, bounded retry, pause, DLQ를 묶은 CPF consumer 실행 엔진입니다. */
public class CpfBrokerConsumerWorker {
    private final CpfBrokerInboxPort inboxPort;
    private final CpfBrokerDlqPort dlqPort;
    private final CpfBrokerConsumerRuntimePolicy runtimePolicy;

    /** 기존 직접 생성 코드 호환. Runtime Control을 사용하려면 3-인자 생성자를 사용합니다. */
    public CpfBrokerConsumerWorker(CpfBrokerInboxPort inboxPort, CpfBrokerDlqPort dlqPort) {
        this(inboxPort, dlqPort, new CpfBrokerConsumerRuntimePolicy());
    }

    public CpfBrokerConsumerWorker(
            CpfBrokerInboxPort inboxPort,
            CpfBrokerDlqPort dlqPort,
            CpfBrokerConsumerRuntimePolicy runtimePolicy) {
        this.inboxPort = Objects.requireNonNull(inboxPort, "inboxPort는 필수입니다.");
        this.dlqPort = Objects.requireNonNull(dlqPort, "dlqPort는 필수입니다.");
        this.runtimePolicy = Objects.requireNonNull(runtimePolicy, "runtimePolicy는 필수입니다.");
    }

    public ConsumeResult consume(CpfBrokerEnvelope envelope, CpfBrokerMessageHandler handler) {
        Objects.requireNonNull(envelope, "envelope는 필수입니다.");
        Objects.requireNonNull(handler, "handler는 필수입니다.");
        CpfBrokerConsumerRuntimePolicy.Snapshot policy = runtimePolicy.current();
        String messageId = envelope.message().messageId();
        if (policy.paused()) {
            return new ConsumeResult("PAUSED", messageId, false, "consumer runtime policy paused");
        }
        if (!inboxPort.markReceived(messageId, envelope.idempotencyKey())) {
            return new ConsumeResult("DUPLICATE", messageId, true, null);
        }

        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
            try {
                CpfBrokerResult result = handler.handle(envelope);
                if (result == null) {
                    throw new IllegalStateException("consumer handler가 결과를 반환하지 않았습니다.");
                }
                inboxPort.markConsumed(messageId, result);
                return new ConsumeResult(result.status(), messageId, false, result.detail());
            } catch (RuntimeException ex) {
                lastFailure = ex;
                if (attempt >= policy.maxAttempts() || !policy.retryable(ex)) break;
                sleep(policy.backoffMillis(attempt));
            }
        }

        CpfBrokerResult dlqResult = dlqPort.sendToDlq(envelope, safeMessage(lastFailure));
        inboxPort.markConsumed(messageId, dlqResult);
        return new ConsumeResult("DLQ", messageId, false, dlqResult.detail());
    }

    private void sleep(long millis) {
        if (millis <= 0) return;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Broker retry backoff가 중단되었습니다.", ex);
        }
    }

    private String safeMessage(RuntimeException ex) {
        if (ex == null) return "UNKNOWN_BROKER_CONSUMER_FAILURE";
        String message = ex.getMessage();
        return SensitiveDataMasker.mask(message == null || message.isBlank() ? ex.getClass().getSimpleName() : message, 1000);
    }

    public record ConsumeResult(String status, String messageId, boolean duplicate, String detail) {
    }
}
