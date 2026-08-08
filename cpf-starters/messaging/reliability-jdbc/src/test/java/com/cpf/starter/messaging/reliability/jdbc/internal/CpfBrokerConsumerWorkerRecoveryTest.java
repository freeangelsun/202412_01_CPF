package com.cpf.starter.messaging.reliability.jdbc.internal;

import com.cpf.core.common.broker.*;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CpfBrokerConsumerWorkerRecoveryTest {
    @Test void handlerIsNotRepeatedWhenDurableFinalizationIsUnknown(){
        Ports ports=new Ports();ports.failFinalization=true;AtomicInteger calls=new AtomicInteger();
        var result=new CpfBrokerConsumerWorker(ports,ports).consume(envelope(),e->{calls.incrementAndGet();return CpfBrokerResult.consumed("m1","APP","ok");});
        assertThat(result.status()).isEqualTo("UNKNOWN");assertThat(calls).hasValue(1);assertThat(ports.unknownStored).isTrue();
    }
    @Test void exhaustedFailureUsesAtomicDlqAndMasksSecrets(){
        Ports ports=new Ports();var result=new CpfBrokerConsumerWorker(ports,ports).consume(envelope(),e->{throw new IllegalStateException("token=secret password=hunter2");});
        assertThat(result.status()).isEqualTo("DLQ");assertThat(ports.atomicDlq).isTrue();assertThat(ports.reason).doesNotContain("secret","hunter2");
    }
    private static CpfBrokerEnvelope envelope(){return new CpfBrokerEnvelope("tx","seg","p","c","idem",Instant.EPOCH,new CpfBrokerMessage("m1","topic","key",new byte[0],"x",Map.of()),Map.of());}
    static final class Ports implements CpfBrokerInboxPort,CpfBrokerDlqPort,CpfBrokerFailureTransitionPort{
        boolean failFinalization,unknownStored,atomicDlq;String reason;
        public boolean markReceived(String m,String i){return true;}
        public void markConsumed(String m,CpfBrokerResult r){if(failFinalization)throw new IllegalStateException("db timeout");}
        public void markConsumerUnknown(String m,String d){unknownStored=true;reason=d;}
        public CpfBrokerResult sendToDlq(CpfBrokerEnvelope e,String r){reason=r;return CpfBrokerResult.failed(e.message().messageId(),"DLQ",r);}
        public CpfBrokerResult moveToDlq(CpfBrokerEnvelope e,String r){atomicDlq=true;return sendToDlq(e,r);}
        public List<CpfBrokerEnvelope> findDlqMessages(String t,int l){return List.of();}
    }
}
