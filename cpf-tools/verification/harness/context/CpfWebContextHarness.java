import com.cpf.core.api.context.*;
public final class CpfWebContextHarness {
  public static void main(String[] args) {
    var f=CpfContextHarnessSupport.factory("TX-WEB-TRUSTED");
    var root=CpfContextHarnessSupport.root(f,"web.inbound",CpfContext.CpfExecutionType.API,CpfContext.CpfTransactionOriginKind.HTTP,
      new CpfContext.CpfIdentityContext("auth-user",null,CpfContext.CpfPrincipalType.USER),new CpfContext.CpfTenantContext("trusted-tenant"));
    CpfContextHarnessSupport.check(!"FORGED".equals(root.transactionId()),"forged transaction rejected by boundary owner");
    CpfContextHarnessSupport.check("auth-user".equals(root.subjectId()),"trusted identity");
    CpfContextHarnessSupport.check("trusted-tenant".equals(root.tenantId()),"trusted tenant");
    System.out.println("WEB_CONTEXT_CURRENT_PASS");
  }
}
