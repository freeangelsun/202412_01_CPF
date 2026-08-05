package com.cpf.gateway.internal.resilience;

import com.cpf.core.api.resilience.CpfResilienceCallContext;
import com.cpf.core.api.resilience.CpfResilienceExecutor;
import com.cpf.core.api.resilience.CpfResilienceOutcome;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.function.Supplier;

public final class S03GatewayResilienceHarness {
    private static final Instant NOW = Instant.parse("2026-08-05T02:00:00Z");
    public static void main(String[] args) {
        defaultUnknownFailsClosed();
        readRetryAllowed();
        writeRetryDeniedByDefault();
        retryableWriteRequiresIdempotency();
        deterministicClockAndTraceAttributes();
        invalidRouteRejectedBeforeExecutor();
        System.out.println("S03_GATEWAY_RESILIENCE_HARNESS PASS cases=6");
    }
    private static void defaultUnknownFailsClosed() {
        RecordingExecutor e=new RecordingExecutor();
        new CpfGatewayResilientInvoker(e,fixed()).invoke("member","tx-1",null,()->"ok");
        check(e.context.operationKind()==CpfResilienceCallContext.OperationKind.UNKNOWN,"kind");
        check(!e.context.timeoutRetryAllowed(),"unknown retry must be false");
    }
    private static void readRetryAllowed() {
        RecordingExecutor e=new RecordingExecutor();
        new CpfGatewayResilientInvoker(e,fixed()).invokeRead("catalog","tx-r",()->"ok");
        check(e.context.operationKind()==CpfResilienceCallContext.OperationKind.READ,"read kind");
        check(e.context.timeoutRetryAllowed(),"read retry");
    }
    private static void writeRetryDeniedByDefault() {
        RecordingExecutor e=new RecordingExecutor();
        new CpfGatewayResilientInvoker(e,fixed()).invokeWrite("payment","tx-w","idem",false,()->"ok");
        check(e.context.operationKind()==CpfResilienceCallContext.OperationKind.WRITE,"write kind");
        check(!e.context.timeoutRetryAllowed(),"write retry");
    }
    private static void retryableWriteRequiresIdempotency() {
        RecordingExecutor e=new RecordingExecutor();
        expect(IllegalArgumentException.class,()->new CpfGatewayResilientInvoker(e,fixed())
                .invokeWrite("payment","tx-w",null,true,()->"ok"));
        check(e.calls==0,"executor called");
    }
    private static void deterministicClockAndTraceAttributes() {
        RecordingExecutor e=new RecordingExecutor();
        new CpfGatewayResilientInvoker(e,fixed()).invokeRead("catalog","tx-r",()->"ok");
        check(NOW.equals(e.context.requestedAt()),"clock");
        check("CLIENT".equals(e.context.attributes().get(CpfResilienceCallContext.TRACE_SPAN_KIND_ATTRIBUTE)),"span");
        check("gateway.catalog".equals(e.context.attributes().get(CpfResilienceCallContext.TRACE_SEGMENT_ATTRIBUTE)),"segment");
    }
    private static void invalidRouteRejectedBeforeExecutor() {
        RecordingExecutor e=new RecordingExecutor();
        expect(IllegalArgumentException.class,()->new CpfGatewayResilientInvoker(e,fixed()).invoke(" ","tx",null,()->"ok"));
        check(e.calls==0,"invalid route reached executor");
    }
    private static Clock fixed(){return Clock.fixed(NOW,ZoneOffset.UTC);}
    private static void check(boolean c,String m){if(!c)throw new AssertionError(m);}
    private static <T extends Throwable> void expect(Class<T> t,Runnable r){try{r.run();throw new AssertionError("expected "+t);}catch(Throwable x){if(!t.isInstance(x))throw new AssertionError(x);}}
    private static final class RecordingExecutor implements CpfResilienceExecutor {
        CpfResilienceCallContext context; int calls;
        public <T> CpfResilienceOutcome<T> execute(CpfResilienceCallContext c,Supplier<T> a){context=c;calls++;return new CpfResilienceOutcome<>(CpfResilienceOutcome.Status.SUCCESS,a.get(),null,1,1,NOW);}
        public <T> CpfResilienceOutcome<T> reconcile(CpfResilienceCallContext c,Supplier<T> p){throw new UnsupportedOperationException();}
    }
}
