package com.cpf.web.api;
import java.util.Map;
public final class TransactionHeaderHarness {
  static int assertions=0;
  static void check(boolean value,String message){assertions++;if(!value)throw new AssertionError(message);}
  static void expectFailure(Runnable action,String message){assertions++;try{action.run();throw new AssertionError(message);}catch(IllegalArgumentException expected){}}
  public static void main(String[] args){
    String tx="20260815010101999MBRlocal010000001";
    Map<String,String> headers=CpfHeaders.builder().txId(tx).execId("EX-1").caller("MBR").target("EXS").buildInternal();
    check(tx.equals(CpfHeaders.get(headers,CpfHeaders.transactionId())),"canonical tx must survive");
    check("EX-1".equals(CpfHeaders.get(headers,CpfHeaders.executionId())),"execution id");
    check("MBR".equals(CpfHeaders.get(headers,CpfHeaders.caller())),"caller");
    check("EXS".equals(CpfHeaders.get(headers,CpfHeaders.target())),"target");
    expectFailure(() -> CpfHeaders.builder().txId("TX-INVALID").execId("EX-1").caller("MBR").target("EXS").buildInternal(),"non canonical tx must fail");
    expectFailure(() -> CpfHeaders.builder().txId(tx).execId("EX-1").caller("MBR").buildInternal(),"missing target must fail");
    expectFailure(() -> CpfHeaders.builder().txId(tx).caller("MBR").target("EXS").buildInternal(),"missing execution must fail");
    Map<String,String> changed=CpfHeaders.from(headers).target("ACC").buildInternal();
    check("ACC".equals(CpfHeaders.get(changed,CpfHeaders.target())),"typed update");
    System.out.println("PASS assertions="+assertions+" actualCpfHeaders=true canonical34=true internalRequired=true");
  }
}
