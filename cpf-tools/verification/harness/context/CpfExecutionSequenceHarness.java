import com.cpf.core.api.context.*;
public final class CpfExecutionSequenceHarness {
  public static void main(String[] args) {
    var f=CpfContextHarnessSupport.factory("TX-SEQ");
    var op1=new CpfContext.CpfOperationContext("op-1","first",null,null,CpfContext.CpfIdempotencyScope.CURRENT_OPERATION,CpfContext.CpfIdempotencyMode.NONE,null,null,1);
    var root=f.newRoot(new com.cpf.foundation.execution.CpfContextExecutionFactory.RootSpec("COR","seq.root",CpfContext.CpfExecutionType.INTERNAL,CpfContext.CpfTransactionOriginKind.INTERNAL,"harness",null,op1,null,null,CpfContextHarnessSupport.CLOCK.instant().plusSeconds(30)));
    var op2=new CpfContext.CpfOperationContext("op-2","second",null,null,CpfContext.CpfIdempotencyScope.CURRENT_OPERATION,CpfContext.CpfIdempotencyMode.NONE,null,"op-1",2);
    var child=f.child(root,new com.cpf.foundation.execution.CpfContextExecutionFactory.ChildSpec("seq.child",CpfContext.CpfExecutionType.INTERNAL,1,root.execution().deadline(),op2));
    CpfContextHarnessSupport.check(root.operation().transactionSequence()==1,"seq1");
    CpfContextHarnessSupport.check(child.operation().transactionSequence()==2,"seq2");
    CpfContextHarnessSupport.check(root.transactionId().equals(child.transactionId()),"sequence tx lineage");
    System.out.println("EXECUTION_SEQUENCE_CURRENT_PASS");
  }
}
