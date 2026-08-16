#!/usr/bin/env python3
from pathlib import Path
import shutil, subprocess, tempfile, textwrap, sys
ROOT=Path(__file__).resolve().parents[2]

def files(*items):
    out=[]
    for rel in items:
        p=ROOT/rel
        if p.is_dir(): out += [str(x) for x in sorted(p.rglob('*.java'))]
        elif p.is_file(): out.append(str(p))
    return out

def main():
    tmp=Path(tempfile.mkdtemp(prefix='cpf-context-runtime-'))
    try:
        harness=tmp/'CpfContextRuntimeHarness.java'
        harness.write_text(textwrap.dedent('''
        import com.cpf.core.api.context.*;
        import com.cpf.starter.async.*;
        import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
                import java.time.*;
        import java.util.concurrent.*;
        import java.util.concurrent.atomic.*;

        public final class CpfContextRuntimeHarness {
          static void ok(boolean v,String m){if(!v)throw new IllegalStateException(m);}
          static CpfContext root(String tx,String ex,String seg){
            Instant now=Instant.now();
            return new CpfContext(
              new CpfContext.CpfTransactionContext(tx,tx,null,"CORR-1","TRACE-1","WEB","CALLER-A",LocalDate.of(2026,8,9),now,CpfContext.CpfTransactionOriginKind.HTTP,"CALLER-A",null),
              new CpfContext.CpfExecutionContext("OEDU010001",ex,ex,null,seg,null,CpfContext.CpfExecutionType.API,1,0,now,now.plusSeconds(60),CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),
              new CpfContext.CpfOperationContext("OP-1","edu","CMD-1","IDEM-1",CpfContext.CpfIdempotencyScope.TRANSACTION,CpfContext.CpfIdempotencyMode.REQUIRED,null,null),
              new CpfContext.CpfIdentityContext("USER-1","OPERATOR-1",CpfContext.CpfPrincipalType.USER),
              new CpfContext.CpfTenantContext("TENANT-1"));
          }
          public static void main(String[] args) throws Exception {
            {
              CpfContext root=root("20260809000000000EDUlocal010000001","EX-ROOT","SG-ROOT");
              ok(CpfContexts.current()==null,"initial context leak");
              try(AutoCloseable outer=CpfContexts.bind(CpfContextSnapshot.capture(root))){
                ok(CpfContexts.requireCurrent().transactionId().equals("20260809000000000EDUlocal010000001"),"root bind");
                CpfContext.CpfExecutionContext childEx=root.execution().child("S-CHILD","EX-CHILD","SG-CHILD",CpfContext.CpfExecutionType.ASYNC,1,Instant.now(),root.execution().deadline());
                CpfContext child=root.child(childEx,root.operation());
                try(AutoCloseable inner=CpfContexts.bind(CpfContextSnapshot.capture(child))){
                  ok(CpfContexts.requireCurrent().executionId().equals("EX-CHILD"),"nested child");
                }
                ok(CpfContexts.requireCurrent().executionId().equals("EX-ROOT"),"nested restore");
                try { CpfContexts.run(CpfContextSnapshot.capture(child),()->{throw new IllegalStateException("expected");}); }
                catch(IllegalStateException expected){ ok("expected".equals(expected.getMessage()),"exception identity"); }
                ok(CpfContexts.requireCurrent().executionId().equals("EX-ROOT"),"exception restore");

                AtomicInteger seq=new AtomicInteger();
                CpfExecutionIdGenerator ids=new CpfExecutionIdGenerator(){
                  public String newExecutionId(){return "EX-A-"+seq.incrementAndGet();}
                  public String newSegmentId(){return "SG-A-"+seq.incrementAndGet();}
                };
                CpfAsyncContextPropagation propagation=new CpfAsyncContextPropagation(ids,"edu-executor");
                ExecutorService pool=Executors.newSingleThreadExecutor();
                try{
                  AtomicReference<String> seenTx=new AtomicReference<>();
                  AtomicReference<String> seenParent=new AtomicReference<>();
                  Runnable wrapped=propagation.wrap(()->{
                    var c=CpfContexts.requireCurrent(); seenTx.set(c.transactionId()); seenParent.set(c.execution().parentExecutionId());
                  },CpfAsyncForkType.EXECUTOR);
                  pool.submit(wrapped).get(5,TimeUnit.SECONDS);
                  ok("20260809000000000EDUlocal010000001".equals(seenTx.get()),"async tx propagation");
                  ok("EX-ROOT".equals(seenParent.get()),"async parent relation");
                  ok(pool.submit(()->CpfContexts.current()==null).get(5,TimeUnit.SECONDS),"executor thread reuse leak");

                  Runnable second=propagation.wrap(()->ok(CpfContexts.requireCurrent().transactionId().equals("20260809000000000EDUlocal010000001"),"second reuse"),CpfAsyncForkType.COMPLETABLE_FUTURE);
                  pool.submit(second).get(5,TimeUnit.SECONDS);
                  ok(pool.submit(()->CpfContexts.current()==null).get(5,TimeUnit.SECONDS),"second reuse leak");
                } finally { pool.shutdownNow(); }

                AtomicReference<String> vt=new AtomicReference<>();
                Thread virtual=Thread.ofVirtual().start(propagation.wrap(()->vt.set(CpfContexts.requireCurrent().transactionId()),CpfAsyncForkType.VIRTUAL_THREAD));
                virtual.join();
                ok("20260809000000000EDUlocal010000001".equals(vt.get()),"virtual thread propagation");
              }
              ok(CpfContexts.current()==null,"outer close leak");
            }
            ok(CpfContexts.current()==null,"runtime close leak");
          }
        }
        '''),encoding='utf-8')
        src=files(
          'cpf-core/src/main/java/com/cpf/core/api/context/CpfContext.java',
          'cpf-core/src/main/java/com/cpf/core/api/context/CpfContextSnapshot.java',
          'cpf-core/src/main/java/com/cpf/core/api/context/CpfContexts.java',
          'cpf-core/src/main/java/com/cpf/core/spi/context/CpfContextRuntimeProvider.java',
          'cpf-starters/base/runtime/src/main/java/com/cpf/starter/internal/context/CpfStarterContextRuntime.java',
          'cpf-starters/base/runtime/src/main/java/com/cpf/foundation/id/spi/CpfExecutionIdGenerator.java',
          'cpf-starters/base/runtime/src/main/java/com/cpf/starter/async/CpfAsyncContext.java',
          'cpf-starters/base/runtime/src/main/java/com/cpf/starter/async/CpfAsyncForkType.java',
          'cpf-starters/base/runtime/src/main/java/com/cpf/starter/async/CpfAsyncContextPropagation.java')+[str(harness)]
        out=tmp/'classes';out.mkdir()
        cp=subprocess.run(['javac','-encoding','UTF-8','-d',str(out),*src],text=True,capture_output=True)
        if cp.returncode:
          print('CPF_CONTEXT_RUNTIME_LIFECYCLE=FAIL compile');print(cp.stdout);print(cp.stderr);return cp.returncode
        service=out/'META-INF/services/com.cpf.core.spi.context.CpfContextRuntimeProvider'
        service.parent.mkdir(parents=True,exist_ok=True)
        shutil.copyfile(ROOT/'cpf-starters/base/runtime/src/main/resources/META-INF/services/com.cpf.core.spi.context.CpfContextRuntimeProvider',service)
        run=subprocess.run(['java','-cp',str(out),'CpfContextRuntimeHarness'],text=True,capture_output=True)
        if run.returncode:
          print('CPF_CONTEXT_RUNTIME_LIFECYCLE=FAIL runtime');print(run.stdout);print(run.stderr);return run.returncode
        print(f'CPF_CONTEXT_RUNTIME_LIFECYCLE=PASS sources={len(src)}')
        return 0
    finally: shutil.rmtree(tmp,ignore_errors=True)
if __name__=='__main__': raise SystemExit(main())
