import com.cpf.core.api.context.*;
public final class CpfSecurityContextHarness {
  public static void main(String[] args) {
    var f=CpfContextHarnessSupport.factory("TX-SEC");
    var identity=new CpfContext.CpfIdentityContext("user-1","operator-1",CpfContext.CpfPrincipalType.OPERATOR,"auth-1",null,"AAL2",CpfContextHarnessSupport.CLOCK.instant());
    var root=CpfContextHarnessSupport.root(f,"security.boundary",CpfContext.CpfExecutionType.API,CpfContext.CpfTransactionOriginKind.HTTP,identity,new CpfContext.CpfTenantContext("TEN"));
    CpfContextHarnessSupport.check("user-1".equals(root.subjectId()),"subject");
    CpfContextHarnessSupport.check("operator-1".equals(root.actorId()),"actor");
    boolean blocked=false; try { new CpfContext.CpfIdentityContext(null,null,CpfContext.CpfPrincipalType.USER); } catch(IllegalArgumentException expected){ blocked=true; }
    CpfContextHarnessSupport.check(blocked,"non-anonymous subject required");
    System.out.println("SECURITY_CONTEXT_CURRENT_PASS");
  }
}
