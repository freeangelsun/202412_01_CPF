import com.cpf.core.api.context.*;
public final class CpfGatewayContextHarness {
  public static void main(String[] args) {
    var f=CpfContextHarnessSupport.factory("TX-GW");
    var identity=new CpfContext.CpfIdentityContext("subject",null,CpfContext.CpfPrincipalType.USER);
    var tenant=new CpfContext.CpfTenantContext("tenant","realm");
    var root=CpfContextHarnessSupport.root(f,"gateway.inbound",CpfContext.CpfExecutionType.API,CpfContext.CpfTransactionOriginKind.HTTP,identity,tenant);
    CpfContextHarnessSupport.check("TX-GW".equals(root.transactionId()),"gateway tx");
    CpfContextHarnessSupport.check("subject".equals(root.subjectId()),"gateway identity");
    CpfContextHarnessSupport.check("tenant".equals(root.tenantId()),"gateway tenant");
    System.out.println("GATEWAY_CONTEXT_CURRENT_PASS");
  }
}
