package com.cpf.starter.messaging.reliability.jdbc.internal;

import com.cpf.core.common.broker.*;
import java.util.Objects;

/** Inbox deduplication, bounded retry, process-kill recovery and atomic DLQ transition engine. */
public class CpfBrokerConsumerWorker {
    private final CpfBrokerInboxPort inboxPort;
    private final CpfBrokerDlqPort dlqPort;
    private final CpfBrokerConsumerRuntimePolicy runtimePolicy;

    public CpfBrokerConsumerWorker(CpfBrokerInboxPort inboxPort, CpfBrokerDlqPort dlqPort) {
        this(inboxPort, dlqPort, new CpfBrokerConsumerRuntimePolicy());
    }
    public CpfBrokerConsumerWorker(CpfBrokerInboxPort inboxPort, CpfBrokerDlqPort dlqPort,
            CpfBrokerConsumerRuntimePolicy runtimePolicy) {
        this.inboxPort=Objects.requireNonNull(inboxPort,"inboxPort");
        this.dlqPort=Objects.requireNonNull(dlqPort,"dlqPort");
        this.runtimePolicy=Objects.requireNonNull(runtimePolicy,"runtimePolicy");
    }

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
            } catch(RuntimeException ex) {
                last=ex;
                if(attempt<policy.maxAttempts() && policy.retryable(ex)){sleep(policy.backoffMillis(attempt));continue;}
                break;
            }
            try {
                inboxPort.markConsumed(consumerIdentity,messageId,result);
                return new ConsumeResult(result.status(),messageId,false,CpfBrokerFailureSanitizer.sanitizeNullable(result.detail()));
            } catch(RuntimeException finalizationFailure) {
                try { inboxPort.markConsumerUnknown(consumerIdentity,messageId,"handler completed but inbox finalization was uncertain: "+safe(finalizationFailure)); }
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

    private void sleep(long millis){if(millis<=0)return;try{Thread.sleep(millis);}catch(InterruptedException ex){Thread.currentThread().interrupt();throw new IllegalStateException("Broker retry interrupted",ex);}}
    private String safe(RuntimeException ex){if(ex==null)return "UNKNOWN_BROKER_CONSUMER_FAILURE";String m=ex.getMessage();return CpfBrokerFailureSanitizer.sanitize(m==null||m.isBlank()?ex.getClass().getSimpleName():m);}
    public record ConsumeResult(String status,String messageId,boolean duplicate,String detail){}
}
