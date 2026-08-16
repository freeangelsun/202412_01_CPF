#!/usr/bin/env python3
from pathlib import Path
import shutil,subprocess,tempfile,textwrap
ROOT=Path(__file__).resolve().parents[2]
def fs(*rels):
 out=[]
 for rel in rels:
  p=ROOT/rel
  if p.is_dir(): out += [str(x) for x in sorted(p.rglob('*.java'))]
  elif p.is_file(): out.append(str(p))
 return out

def main():
 tmp=Path(tempfile.mkdtemp(prefix='cpf-batch-context-'))
 try:
  h=tmp/'CpfBatchContextHarness.java'
  h.write_text(textwrap.dedent('''
  import com.cpf.batch.context.*;
  import com.cpf.batch.scheduler.internal.context.CpfBatchContextFactory;
  import com.cpf.batch.execution.context.CpfBatchContextCarrier;
  import com.cpf.batch.execution.internal.context.CpfBatchRuntimeContexts;
  import com.cpf.foundation.id.spi.*;
  import com.cpf.foundation.time.spi.CpfBusinessDateProvider;
  import java.time.*;
  import java.util.*;
  import java.util.concurrent.atomic.AtomicInteger;
  public final class CpfBatchContextHarness {
    static void ok(boolean v,String m){if(!v)throw new IllegalStateException(m);}
    public static void main(String[] a)throws Exception{
      AtomicInteger n=new AtomicInteger();
      CpfTransactionIdGenerator tx=()->"20260809010101001BATlocal01"+String.format("%07d",n.incrementAndGet());
      CpfExecutionIdGenerator ex=new CpfExecutionIdGenerator(){public String newExecutionId(){return "EX-B-"+n.incrementAndGet();}public String newSegmentId(){return "SG-B-"+n.incrementAndGet();}};
      CpfBusinessDateProvider bd=()->LocalDate.of(2026,8,9);
      CpfBatchContextFactory f=new CpfBatchContextFactory(tx,ex,bd);
      CpfBatchContextBundle root=f.newSchedulerRoot("dailyClose","SCH-1",null,"BFIN010001","TRG-1",Instant.now().plusSeconds(600));
      ok(root.snapshot().context().transactionId().matches("[0-9]{17}BATlocal01[0-9]{7}"),"root canonical tx");
      ok(root.batch().businessDate().equals(LocalDate.of(2026,8,9)),"business date provider");
      ok(root.batch().launchMode()==CpfBatchLaunchMode.SCHEDULED,"launch mode");
      String rootTx=root.snapshot().context().transactionId(); String rootExec=root.snapshot().execution().rootExecutionId();
      CpfBatchContextBundle restarted=f.restart(root,"JI-1","JE-2","JE-1",1,"REC-1",11L);
      ok(restarted.snapshot().context().transactionId().equals(rootTx),"restart transaction correlation");
      ok(restarted.snapshot().execution().rootExecutionId().equals(rootExec),"restart root execution");
      ok(!restarted.snapshot().execution().executionId().equals(root.snapshot().execution().executionId()),"restart child execution");
      ok(restarted.batch().launchMode()==CpfBatchLaunchMode.RESTART,"restart launch mode");
      ok("REC-1".equals(restarted.batch().recoveryId()),"restart recovery id");
      ok("JE-1".equals(restarted.batch().originalJobExecutionId()),"original job execution");
      CpfBatchContextBundle step=f.childStep(restarted,"stepA","SE-1","P-1","W-1","CP-1",3,12L);
      ok(step.snapshot().execution().parentExecutionId().equals(restarted.snapshot().execution().executionId()),"step parent");
      ok("CP-1".equals(step.batch().checkpointId()),"checkpoint");
      CpfBatchContextBundle unknown=f.unknown(step,"UNK-1","REC-2",4);
      ok("UNK-1".equals(unknown.batch().unknownOutcomeId()),"unknown id");
      ok("REC-2".equals(unknown.batch().recoveryId()),"unknown recovery id");
      ok(unknown.snapshot().context().transactionId().equals(rootTx),"unknown tx relation");
      CpfBatchContextCarrier carrier=new CpfBatchContextCarrier(ex);
      Map<String,String> wire=carrier.inject(unknown);
      ok(wire.size()<=32,"wire key budget");
      CpfBatchContextBundle restored=carrier.restore(wire);
      ok(restored.snapshot().context().transactionId().equals(rootTx),"wire tx");
      ok("UNK-1".equals(restored.batch().unknownOutcomeId()),"wire unknown");
      ok("REC-2".equals(restored.batch().recoveryId()),"wire recovery");
      ok(restored.batch().businessDate().equals(LocalDate.of(2026,8,9)),"wire business date");
      ok(CpfBatchRuntimeContexts.current()==null,"runtime initial leak");
      try(AutoCloseable s1=CpfBatchRuntimeContexts.bind(root)){
        ok(CpfBatchRuntimeContexts.current()==root,"runtime bind");
        try(AutoCloseable s2=CpfBatchRuntimeContexts.bind(unknown)){ok(CpfBatchRuntimeContexts.current()==unknown,"runtime nested");}
        ok(CpfBatchRuntimeContexts.current()==root,"runtime restore");
      }
      ok(CpfBatchRuntimeContexts.current()==null,"runtime close leak");
    }
  }
  '''),encoding='utf-8')
  src=fs('cpf-core/src/main/java/com/cpf/core/api/context/CpfContext.java','cpf-core/src/main/java/com/cpf/core/api/context/CpfContextSnapshot.java','cpf-batch/api/src/main/java/com/cpf/batch/context','cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/internal/context/CpfBatchContextFactory.java','cpf-batch/runtime/src/main/java/com/cpf/batch/execution/context/CpfBatchContextCarrier.java','cpf-batch/runtime/src/main/java/com/cpf/batch/execution/internal/context/CpfBatchRuntimeContexts.java','cpf-starters/base/runtime/src/main/java/com/cpf/foundation/id/spi','cpf-starters/base/runtime/src/main/java/com/cpf/foundation/time/spi')+[str(h)]
  out=tmp/'classes';out.mkdir();cp=subprocess.run(['javac','-encoding','UTF-8','-d',str(out),*src],text=True,capture_output=True)
  if cp.returncode:
   print('CPF_BATCH_CONTEXT_RUNTIME=FAIL compile');print(cp.stdout);print(cp.stderr);return cp.returncode
  run=subprocess.run(['java','-cp',str(out),'CpfBatchContextHarness'],text=True,capture_output=True)
  if run.returncode:
   print('CPF_BATCH_CONTEXT_RUNTIME=FAIL runtime');print(run.stdout);print(run.stderr);return run.returncode
  print(f'CPF_BATCH_CONTEXT_RUNTIME=PASS sources={len(src)}');return 0
 finally:shutil.rmtree(tmp,ignore_errors=True)
if __name__=='__main__':raise SystemExit(main())
