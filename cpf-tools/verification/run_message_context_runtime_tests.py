#!/usr/bin/env python3
from pathlib import Path
import shutil,subprocess,tempfile,textwrap
ROOT=Path(__file__).resolve().parents[2]
def fs(*rels):
 out=[]
 for r in rels:
  p=ROOT/r
  if p.is_dir(): out += [str(x) for x in sorted(p.rglob('*.java'))]
  elif p.is_file(): out.append(str(p))
 return out

def main():
 tmp=Path(tempfile.mkdtemp(prefix='cpf-msg-context-'))
 try:
  h=tmp/'CpfMessageContextHarness.java'
  h.write_text(textwrap.dedent('''
  import com.cpf.core.api.context.*;
  import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
    import com.cpf.messaging.context.*;
  import java.time.*;
  import java.util.*;
  import java.util.concurrent.atomic.*;
  public final class CpfMessageContextHarness {
    static void ok(boolean v,String m){if(!v)throw new IllegalStateException(m);}
    static CpfContext root(){Instant n=Instant.now();return new CpfContext(
      new CpfContext.CpfTransactionContext("20260809000000000EDUlocal010000003","20260809000000000EDUlocal010000003",null,"CORR-M","TRACE-M","API","EDU","API","EDU",LocalDate.of(2026,8,9),n,CpfContext.CpfTransactionOriginKind.HTTP,"EDU",null),
      new CpfContext.CpfExecutionContext("OEDU020001","EX-M-ROOT","EX-M-ROOT",null,"SG-M-ROOT",null,CpfContext.CpfExecutionType.API,1,0,n,n.plusSeconds(60),CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),
      new CpfContext.CpfOperationContext("OP-M","publish","CMD-M","IDEM-M",CpfContext.CpfIdempotencyScope.TRANSACTION,CpfContext.CpfIdempotencyMode.REQUIRED,null,null),null,null);}
    public static void main(String[] a)throws Exception{
      AtomicInteger seq=new AtomicInteger();
      CpfExecutionIdGenerator ids=new CpfExecutionIdGenerator(){public String newExecutionId(){return "EX-M-"+seq.incrementAndGet();}public String newSegmentId(){return "SG-M-"+seq.incrementAndGet();}};
      CpfMessageBridgeContextSupport bridge=new CpfMessageBridgeContextSupport(ids);
      try(AutoCloseable rootScope=CpfContexts.bind(CpfContextSnapshot.capture(root()))){
        var outbound=bridge.prepareOutbound("KAFKA","orders","MSG-1",Map.of("x-safe","v"));
        ok("20260809000000000EDUlocal010000003".equals(outbound.headers().get(CpfMessageHeaderNames.TRANSACTION_ID)),"outbound tx");
        ok("IDEM-M".equals(outbound.headers().get(CpfMessageHeaderNames.IDEMPOTENCY_KEY)),"outbound idempotency");
        ok("KAFKA".equals(outbound.context().transport()),"owner message context");
        boolean secretRejected=false;try{bridge.prepareOutbound("JMS","q","MSG-2",Map.of("Authorization","Bearer secret"));}catch(SecurityException e){secretRejected=true;}ok(secretRejected,"secret header rejected");
        var inbound=bridge.extractInbound("RABBIT","MSG-3","orders.in","producer-A","cg-A","2","101",3,true,"schema","1",outbound.headers(),"MEDU010001");
        ok(inbound.message().deliveryAttempt()==3,"retry attempt");
        ok(inbound.message().redelivery(),"redelivery flag");
        ok("20260809000000000EDUlocal010000003".equals(inbound.snapshot().context().transactionId()),"inbound tx relation");
        ok(inbound.snapshot().execution().parentExecutionId().equals("EX-M-ROOT"),"parent execution relation");
        AtomicReference<String> seen=new AtomicReference<>(); bridge.consume(inbound,()->seen.set(CpfContexts.requireCurrent().transactionId()));
        ok("20260809000000000EDUlocal010000003".equals(seen.get()),"consume bind");
        ok("EX-M-ROOT".equals(CpfContexts.requireCurrent().executionId()),"consume restore");
        CpfMessageContext dlq=new CpfMessageContext("IBM_MQ","MSG-4",null,null,"DLQ",null,"cg",null,null,"MSG-4",4,true,null,null,Instant.now(),Instant.now(),null,"MAX_RETRY");
        ok("MAX_RETRY".equals(dlq.deadLetterReason()) && dlq.deliveryAttempt()==4,"dlq metadata");
      }
      ok(CpfContexts.current()==null,"runtime leak");
    }
  }
  '''),encoding='utf-8')
  src=fs('cpf-core/src/main/java/com/cpf/core/api/context/CpfContext.java','cpf-core/src/main/java/com/cpf/core/api/context/CpfContextSnapshot.java','cpf-core/src/main/java/com/cpf/core/api/context/CpfContexts.java','cpf-core/src/main/java/com/cpf/core/spi/context/CpfContextRuntimeProvider.java','cpf-starters/base/runtime/src/main/java/com/cpf/starter/internal/context/CpfStarterContextRuntime.java','cpf-starters/base/runtime/src/main/java/com/cpf/foundation/id/spi/CpfExecutionIdGenerator.java','cpf-starters/messaging/src/main/java/com/cpf/messaging/context')+[str(h)]
  out=tmp/'classes';out.mkdir();cp=subprocess.run(['javac','-encoding','UTF-8','-d',str(out),*src],text=True,capture_output=True)
  if cp.returncode: print('CPF_MESSAGE_CONTEXT_RUNTIME=FAIL compile');print(cp.stdout);print(cp.stderr);return cp.returncode
  service=out/'META-INF/services/com.cpf.core.spi.context.CpfContextRuntimeProvider';service.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(ROOT/'cpf-starters/base/runtime/src/main/resources/META-INF/services/com.cpf.core.spi.context.CpfContextRuntimeProvider',service)
  r=subprocess.run(['java','-cp',str(out),'CpfMessageContextHarness'],text=True,capture_output=True)
  if r.returncode: print('CPF_MESSAGE_CONTEXT_RUNTIME=FAIL runtime');print(r.stdout);print(r.stderr);return r.returncode
  print(f'CPF_MESSAGE_CONTEXT_RUNTIME=PASS sources={len(src)}');return 0
 finally:shutil.rmtree(tmp,ignore_errors=True)
if __name__=='__main__':raise SystemExit(main())
