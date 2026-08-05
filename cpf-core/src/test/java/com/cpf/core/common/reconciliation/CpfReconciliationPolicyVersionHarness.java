package com.cpf.core.common.reconciliation;
import java.util.Set;
public final class CpfReconciliationPolicyVersionHarness {
 private CpfReconciliationPolicyVersionHarness(){}
 public static void main(String[] args){
  CpfReconciliationRuntimePolicy policy=new CpfReconciliationRuntimePolicy();
  var v1=policy.replace(1,true,1000,0,10,30,true,Set.of("payment"),3,2,5000);
  check(policy.replace(1,true,1000,0,10,30,true,Set.of("PAYMENT"),3,2,5000).equals(v1),"same version same content idempotent");
  boolean conflict=false; try{policy.replace(1,true,2000,0,10,30,true,Set.of("PAYMENT"),3,2,5000);}catch(IllegalStateException e){conflict=true;}
  check(conflict,"same version conflict rejected");
  boolean rollback=false; try{policy.replace(0,false,1000,0,10,30,true,Set.of(),3,2,5000);}catch(IllegalArgumentException e){rollback=true;}
  check(rollback,"version rollback rejected");
  boolean invalid=false; try{policy.replace(2,true,1000,0,10,30,true,Set.of("../PAYMENT"),3,2,5000);}catch(IllegalArgumentException e){invalid=true;}
  check(invalid,"unknown type allowlist validated");
  System.out.println("CPF_RECONCILIATION_POLICY_VERSION_HARNESS_PASS");
 }
 private static void check(boolean c,String m){if(!c)throw new AssertionError(m);}
}
