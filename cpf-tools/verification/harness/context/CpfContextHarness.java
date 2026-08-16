import com.cpf.core.api.context.*;
public final class CpfContextHarness {
  public static void main(String[] args) throws Exception {
    var f = CpfContextHarnessSupport.factory("TX-CTX");
    var root = CpfContextHarnessSupport.root(f,"context.root",CpfContext.CpfExecutionType.INTERNAL,CpfContext.CpfTransactionOriginKind.INTERNAL,null,null);
    CpfContextHarnessSupport.check(CpfContexts.current()==null,"initial leak");
    try (var outer = CpfContexts.bind(CpfContextSnapshot.capture(root))) {
      var child = CpfContextHarnessSupport.child(f,root,"context.child",CpfContext.CpfExecutionType.ASYNC,2);
      try (var inner = CpfContexts.bind(child)) {
        CpfContextHarnessSupport.check("TX-CTX".equals(CpfContexts.transactionId()),"transaction lineage");
        CpfContextHarnessSupport.check(child.context().executionId().equals(CpfContexts.currentExecutionId()),"child bind");
      }
      CpfContextHarnessSupport.check(root.executionId().equals(CpfContexts.currentExecutionId()),"nested restore");
    }
    CpfContextHarnessSupport.check(CpfContexts.current()==null,"final leak");
    System.out.println("CONTEXT_CURRENT_PASS");
  }
}
