package com.cpf.starter.messaging.reliability;

import com.cpf.core.common.broker.CpfBrokerEnvelope;
import com.cpf.core.common.broker.CpfBrokerFailureSanitizer;
import com.cpf.core.common.broker.CpfBrokerOutboxPort;
import com.cpf.core.common.broker.CpfBrokerPublishResultProbe;
import com.cpf.core.common.broker.CpfBrokerResult;
import com.cpf.core.common.broker.CpfBrokerUnknownResultPort;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/** Reconciles UNKNOWN broker results without permitting blind duplicate publication. */
public final class CpfBrokerUnknownResultReconciler {
    private final CpfBrokerUnknownResultPort unknownPort;
    private final CpfBrokerOutboxPort outboxPort;
    private final List<CpfBrokerPublishResultProbe> probes;
    private final Clock clock;
    private final Duration retryDelay;

    public CpfBrokerUnknownResultReconciler(CpfBrokerUnknownResultPort unknownPort,
            List<CpfBrokerPublishResultProbe> probes, Clock clock, Duration retryDelay) {
        this.unknownPort=requireFencedUnknown(unknownPort);
        if(!(unknownPort instanceof CpfBrokerOutboxPort outbox)) throw new IllegalArgumentException("UNKNOWN port must also implement outbox");
        if(!outbox.supportsFencedPublishMutation()) {
            throw new IllegalArgumentException("UNKNOWN outbox adapter must support fenced publish mutation");
        }
        this.outboxPort=outbox; this.probes=probes==null?List.of():List.copyOf(probes);
        this.clock=Objects.requireNonNull(clock,"clock");
        if(retryDelay==null||retryDelay.isZero()||retryDelay.isNegative())throw new IllegalArgumentException("retryDelay must be positive");
        this.retryDelay=retryDelay;
    }

    public Result runOnce(String workerId,int limit){
        String owner=requireWorker(workerId);
        List<CpfBrokerEnvelope> claimed=unknownPort.claimUnknown(owner,limit);
        int ok=0,failed=0,pending=0;
        for(CpfBrokerEnvelope envelope:claimed){
            CpfBrokerResult result=probe(envelope);
            if(result!=null && !isUnknown(result)){
                outboxPort.markPublished(owner,envelope.message().messageId(),result);
                if(isPublished(result))ok++;else failed++;
            }else{
                String detail=result==null?"No provider reconciliation evidence":"Provider result remains UNKNOWN: "+safe(result.detail());
                unknownPort.releaseUnknown(owner,envelope.message().messageId(),detail,clock.instant().plus(retryDelay));
                pending++;
            }
        }
        return new Result(claimed.size(),ok,failed,pending);
    }

    private CpfBrokerResult probe(CpfBrokerEnvelope envelope){
        CpfBrokerResult last=null;
        for(CpfBrokerPublishResultProbe probe:probes){
            try{
                CpfBrokerResult r=probe.probe(envelope);
                if(r==null)continue;
                if(r.messageId()!=null&&!envelope.message().messageId().equals(r.messageId())){
                    last=unknown(envelope,"Provider probe correlation mismatch");
                    continue;
                }
                last=r;if(!isUnknown(r))return r;
            }catch(RuntimeException ex){last=unknown(envelope,safe(ex.getMessage()));}
        }
        return last;
    }
    private CpfBrokerResult unknown(CpfBrokerEnvelope e,String d){return new CpfBrokerResult("UNKNOWN",e.message().messageId(),"PROBE",null,clock.instant(),safe(d));}
    private static boolean isUnknown(CpfBrokerResult r){return "UNKNOWN".equalsIgnoreCase(r.status())||"RESULT_UNKNOWN".equalsIgnoreCase(r.status());}
    private static boolean isPublished(CpfBrokerResult r){return "PUBLISHED".equalsIgnoreCase(r.status())||"SUCCESS".equalsIgnoreCase(r.status())||"ACCEPTED".equalsIgnoreCase(r.status());}
    private static String safe(String v){return CpfBrokerFailureSanitizer.sanitize(v==null||v.isBlank()?"UNKNOWN":v);}
    private static CpfBrokerUnknownResultPort requireFencedUnknown(CpfBrokerUnknownResultPort value) {
        CpfBrokerUnknownResultPort port=Objects.requireNonNull(value,"unknownPort");
        if(!port.supportsFencedUnknownMutation()) {
            throw new IllegalArgumentException("UNKNOWN adapter must support fenced state mutation");
        }
        return port;
    }
    private static String requireWorker(String v){if(v==null||v.isBlank())throw new IllegalArgumentException("workerId is required");return v.trim();}
    public record Result(int claimed,int resolvedSuccess,int resolvedFailure,int pending){}
}
