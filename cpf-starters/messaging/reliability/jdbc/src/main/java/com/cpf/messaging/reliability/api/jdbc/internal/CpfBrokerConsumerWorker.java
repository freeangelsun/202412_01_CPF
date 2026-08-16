package com.cpf.messaging.reliability.api.jdbc.internal;

import com.cpf.messaging.spi.broker.*;
import java.util.Objects;

/** Inbox deduplication, bounded retry, process-kill recovery and atomic DLQ transition engine. */
/** CpfBrokerConsumerWorker는 메시징 신뢰성 경계에서 중복 방지·재시도·결과불명 복구 책임을 명확히 수행합니다. */
public class CpfBrokerConsumerWorker {
    private final CpfBrokerInboxPort inboxPort;
    private final CpfBrokerDlqPort dlqPort;
    private final CpfBrokerConsumerRuntimePolicy runtimePolicy;

    /** CpfBrokerConsumerWorker 작업을 CPF 메시징 신뢰성 정책과 상태 전이 규칙에 따라 수행합니다. */
    public CpfBrokerConsumerWorker(CpfBrokerInboxPort inboxPort, CpfBrokerDlqPort dlqPort) {
        this(inboxPort, dlqPort, new CpfBrokerConsumerRuntimePolicy());
    }
    /** CpfBrokerConsumerWorker 작업을 CPF 메시징 신뢰성 정책과 상태 전이 규칙에 따라 수행합니다. */
    public CpfBrokerConsumerWorker(CpfBrokerInboxPort inboxPort, CpfBrokerDlqPort dlqPort,
            CpfBrokerConsumerRuntimePolicy runtimePolicy) {
        this.inboxPort=Objects.requireNonNull(inboxPort,"inboxPort");
        this.dlqPort=Objects.requireNonNull(dlqPort,"dlqPort");
        this.runtimePolicy=Objects.requireNonNull(runtimePolicy,"runtimePolicy");
    }

    /** consume 작업을 CPF 메시징 신뢰성 정책과 상태 전이 규칙에 따라 수행합니다. */
    public ConsumeResult consume(CpfBrokerEnvelope envelope, CpfBrokerMessageHandler handler) {
        Objects.requireNonNull(envelope,"envelope"); Objects.requireNonNull(handler,"handler");
        var policy=runtimePolicy.current(); String messageId=envelope.message().messageId(); String consumerIdentity=consumerIdentity(envelope);
        if(policy.paused()) return new ConsumeResult("PAUSED",messageId,false,"consumer runtime policy paused");
        if(!inboxPort.markReceived(consumerIdentity,messageId,envelope.idempotencyKey())) return new ConsumeResult("DUPLICATE",messageId,true,null);
        RuntimeException last=null;
        for(int attempt=1;attempt<=policy.maxAttempts();attempt++) {
            CpfBrokerResult result;
            try {
                result=Objects.requireNonNull(handler.handle(envelope),"consumer handler result");
            // 실패를 성공으로 오인하지 않고 재시도 가능 여부와 결과불명 복구 경로를 보존합니다.
            } catch(RuntimeException ex) {
                last=ex;
                if(attempt<policy.maxAttempts() && policy.retryable(ex)){sleep(policy.backoffMillis(attempt));continue;}
                break;
            }
            try {
                inboxPort.markConsumed(consumerIdentity,messageId,result);
                return new ConsumeResult(result.status(),messageId,false,CpfBrokerFailureSanitizer.sanitizeNullable(result.detail()));
            // 실패를 성공으로 오인하지 않고 재시도 가능 여부와 결과불명 복구 경로를 보존합니다.
            } catch(RuntimeException finalizationFailure) {
                try { inboxPort.markConsumerUnknown(consumerIdentity,messageId,"handler completed but inbox finalization was uncertain: "+safe(finalizationFailure)); }
                // 실패를 성공으로 오인하지 않고 재시도 가능 여부와 결과불명 복구 경로를 보존합니다.
                catch(RuntimeException unknownFailure){ finalizationFailure.addSuppressed(unknownFailure); }
                return new ConsumeResult("UNKNOWN",messageId,false,safe(finalizationFailure));
            }
        }
        String reason=safe(last);
        CpfBrokerResult dlqResult;
        if(inboxPort instanceof CpfBrokerFailureTransitionPort transition) {
            dlqResult=transition.moveToDlq(consumerIdentity,envelope,reason);
        } else {
            dlqResult=dlqPort.sendToDlq(envelope,reason);
            inboxPort.markConsumed(consumerIdentity,messageId,dlqResult);
        }
        return new ConsumeResult("DLQ",messageId,false,dlqResult.detail());
    }

    private static String consumerIdentity(CpfBrokerEnvelope envelope){ String value=envelope.consumerModule(); return value==null||value.isBlank()?"default":value; }

    // 실패를 성공으로 오인하지 않고 재시도 가능 여부와 결과불명 복구 경로를 보존합니다.
    private void sleep(long millis){if(millis<=0)return;try{Thread.sleep(millis);}catch(InterruptedException ex){Thread.currentThread().interrupt();throw new IllegalStateException("Broker retry interrupted",ex);}}
    private String safe(RuntimeException ex){if(ex==null)return "UNKNOWN_BROKER_CONSUMER_FAILURE";String m=ex.getMessage();return CpfBrokerFailureSanitizer.sanitize(m==null||m.isBlank()?ex.getClass().getSimpleName():m);}
    /** ConsumeResult는 메시징 신뢰성 경계에서 중복 방지·재시도·결과불명 복구 책임을 명확히 수행합니다. */
    public record ConsumeResult(String status,String messageId,boolean duplicate,String detail){}
}
