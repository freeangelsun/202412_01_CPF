package com.cpf.testkit.context;
import com.cpf.core.api.context.CpfContext;
import java.time.Instant;
import java.time.LocalDate;

public final class CpfTestkitRuntimeHarness {
    public static void main(String[] args) throws Exception {
        CpfContext root=context("TX-1","EX-1","SEG-1");
        CpfContext child=context("TX-1","EX-2","SEG-2");
        CpfTestContextRuntime runtime=CpfTestContextRuntime.install();
        if(runtime.current()!=null) fail("initial context leak");
        try(AutoCloseable a=runtime.bind(root)) {
            if(runtime.current()!=root) fail("root bind failed");
            try(AutoCloseable b=runtime.bind(child)) { if(runtime.current()!=child) fail("nested bind failed"); }
            if(runtime.current()!=root) fail("nested restore failed");
        }
        if(runtime.current()!=null) fail("final context leak");
        runtime.close();
        boolean orderFail=false;
        CpfTestContextRuntime invalid=CpfTestContextRuntime.install();
        AutoCloseable first=invalid.bind(root); AutoCloseable second=invalid.bind(child);
        try { first.close(); } catch(IllegalStateException expected){ orderFail=true; }
        if(!orderFail) fail("close-order guard missing");
        try { second.close(); } catch(Exception ignored) { }
        try { invalid.close(); } catch(IllegalStateException ignored) { }
        System.out.println("CPF_TESTKIT_RUNTIME_PASS bind=nested+restore+leak orderGuard=1");
    }
    static CpfContext context(String tx,String ex,String seg){
        Instant now=Instant.parse("2026-08-10T00:00:00Z");
        var t=new CpfContext.CpfTransactionContext(
                tx,tx,null,tx,null,
                "SYS","SYS",null,null,
                "WEB","WEB",null,null,
                LocalDate.of(2026,8,10),now,CpfContext.CpfTransactionOriginKind.HTTP,"SYS",tx);
        var e=new CpfContext.CpfExecutionContext(ex,ex,ex,null,seg,null,CpfContext.CpfExecutionType.API,1,0,now,null,CpfContext.CpfCancellationMode.DEADLINE_ENFORCED);
        return new CpfContext(t,e,null,null,null);
    }
    static void fail(String m){throw new IllegalStateException(m);}
}
