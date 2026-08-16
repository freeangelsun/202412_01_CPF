import com.cpf.core.api.context.*;
public final class CpfMessageContextHarness {
  public static void main(String[] args) throws Exception {
    var f=CpfContextHarnessSupport.factory("TX-MSG");
    var root=CpfContextHarnessSupport.root(f,"message.publish",CpfContext.CpfExecutionType.MESSAGE,CpfContext.CpfTransactionOriginKind.MESSAGE,null,null);
    var consume=CpfContextHarnessSupport.child(f,root,"message.consume",CpfContext.CpfExecutionType.MESSAGE,3);
    CpfContextHarnessSupport.check("TX-MSG".equals(consume.context().transactionId()),"message tx lineage");
    CpfContextHarnessSupport.check(consume.context().execution().attempt()==3,"redelivery attempt");
    try(var scope=CpfContexts.bind(consume)){CpfContextHarnessSupport.check("TX-MSG".equals(CpfContexts.transactionId()),"message bind");}
    CpfContextHarnessSupport.check(CpfContexts.current()==null,"message leak");
    System.out.println("MESSAGE_CONTEXT_CURRENT_PASS");
  }
}
