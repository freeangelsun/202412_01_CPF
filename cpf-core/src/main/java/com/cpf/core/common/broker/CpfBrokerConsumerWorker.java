package cpf.pfw.common.broker;

import java.util.Objects;

/**
 * inbox 중복 방지와 실패 메시지 DLQ 이동을 묶은 PFW consumer 실행 엔진입니다.
 */
public class CpfBrokerConsumerWorker {
    private final CpfBrokerInboxPort inboxPort;
    private final CpfBrokerDlqPort dlqPort;

    public CpfBrokerConsumerWorker(CpfBrokerInboxPort inboxPort, CpfBrokerDlqPort dlqPort) {
        this.inboxPort = Objects.requireNonNull(inboxPort, "inboxPort는 필수입니다.");
        this.dlqPort = Objects.requireNonNull(dlqPort, "dlqPort는 필수입니다.");
    }

    public ConsumeResult consume(CpfBrokerEnvelope envelope, CpfBrokerMessageHandler handler) {
        Objects.requireNonNull(envelope, "envelope는 필수입니다.");
        Objects.requireNonNull(handler, "handler는 필수입니다.");
        String messageId = envelope.message().messageId();
        if (!inboxPort.markReceived(messageId, envelope.idempotencyKey())) {
            return new ConsumeResult("DUPLICATE", messageId, true, null);
        }

        try {
            CpfBrokerResult result = handler.handle(envelope);
            if (result == null) {
                throw new IllegalStateException("consumer handler가 결과를 반환하지 않았습니다.");
            }
            inboxPort.markConsumed(messageId, result);
            return new ConsumeResult(result.status(), messageId, false, result.detail());
        } catch (RuntimeException ex) {
            CpfBrokerResult dlqResult = dlqPort.sendToDlq(envelope, safeMessage(ex));
            inboxPort.markConsumed(messageId, dlqResult);
            return new ConsumeResult("DLQ", messageId, false, dlqResult.detail());
        }
    }

    private String safeMessage(RuntimeException ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? ex.getClass().getSimpleName() : message;
    }

    public record ConsumeResult(String status, String messageId, boolean duplicate, String detail) {
    }
}
