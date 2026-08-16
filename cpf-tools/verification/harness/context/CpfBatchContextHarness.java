import com.cpf.core.api.context.*;
public final class CpfBatchContextHarness {
  public static void main(String[] args) throws Exception {
    var f=CpfContextHarnessSupport.factory("TX-BATCH");
    var root=CpfContextHarnessSupport.root(f,"batch.job",CpfContext.CpfExecutionType.BATCH,CpfContext.CpfTransactionOriginKind.BATCH,null,null);
    var step=CpfContextHarnessSupport.child(f,root,"batch.step",CpfContext.CpfExecutionType.BATCH,2);
    CpfContextHarnessSupport.check(root.transactionId().equals(step.context().transactionId()),"batch tx lineage");
    CpfContextHarnessSupport.check(step.context().execution().attempt()==2,"batch attempt");
    CpfContextHarnessSupport.check(step.context().execution().parentExecutionId().equals(root.executionId()),"batch parent execution");
    System.out.println("BATCH_CONTEXT_CURRENT_PASS");
  }
}
