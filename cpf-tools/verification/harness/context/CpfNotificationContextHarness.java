import com.cpf.core.api.context.*;
public final class CpfNotificationContextHarness {
  public static void main(String[] args) throws Exception {
    var f=CpfContextHarnessSupport.factory("TX-NOTIFY");
    var tenant=new CpfContext.CpfTenantContext("TEN-1","REALM-1");
    var root=CpfContextHarnessSupport.root(f,"notification.enqueue",CpfContext.CpfExecutionType.INTERNAL,CpfContext.CpfTransactionOriginKind.INTERNAL,null,tenant);
    var dispatch=CpfContextHarnessSupport.child(f,root,"notification.email",CpfContext.CpfExecutionType.INTEGRATION,3);
    CpfContextHarnessSupport.check(root.transactionId().equals(dispatch.context().transactionId()),"notification tx");
    CpfContextHarnessSupport.check("TEN-1".equals(dispatch.context().tenantId()),"notification tenant");
    CpfContextHarnessSupport.check(dispatch.context().execution().attempt()==3,"notification attempt");
    System.out.println("NOTIFICATION_CONTEXT_CURRENT_PASS");
  }
}
