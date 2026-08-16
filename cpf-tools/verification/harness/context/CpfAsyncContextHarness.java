import com.cpf.core.api.context.*;
import java.util.concurrent.*;
public final class CpfAsyncContextHarness {
  public static void main(String[] args) throws Exception {
    var f=CpfContextHarnessSupport.factory("TX-ASYNC");
    var root=CpfContextHarnessSupport.root(f,"async.root",CpfContext.CpfExecutionType.ASYNC,CpfContext.CpfTransactionOriginKind.INTERNAL,null,null);
    var snap=CpfContextSnapshot.capture(root);
    try (var executor=Executors.newVirtualThreadPerTaskExecutor()) {
      Future<String> v=executor.submit(() -> CpfContexts.call(snap, CpfContexts::transactionId));
      CpfContextHarnessSupport.check("TX-ASYNC".equals(v.get(3,TimeUnit.SECONDS)),"async propagation");
    }
    CpfContextHarnessSupport.check(CpfContexts.current()==null,"async leak");
    System.out.println("ASYNC_CONTEXT_CURRENT_PASS");
  }
}
