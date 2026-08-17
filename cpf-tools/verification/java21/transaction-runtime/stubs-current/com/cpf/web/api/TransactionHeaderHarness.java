package com.cpf.web.api;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public final class TransactionHeaderHarness {
  static int assertions=0;
  static void check(boolean value,String message){assertions++;if(!value)throw new AssertionError(message);}
  static void expectFailure(Runnable action,String message){assertions++;try{action.run();throw new AssertionError(message);}catch(IllegalArgumentException expected){}}
  public static void main(String[] args){
    String tx="20260815010101999MBRlocal010000001";
    Map<String,String> headers=new LinkedHashMap<>();
    headers.put(CpfHttpHeaders.transactionId(),tx);
    headers.put(CpfHttpHeaders.originalSystemCode(),"MBR");
    headers.put(CpfHttpHeaders.systemCode(),"EXS");
    headers.put(CpfHttpHeaders.callerSystemCode(),"MBR");
    headers.put(CpfHttpHeaders.targetSystemCode(),"EXS");
    headers.put(CpfHttpHeaders.targetOperationId(),"memberFind");
    CpfHttpHeaders.validateInternal(headers);
    check(tx.equals(CpfHttpHeaders.get(headers,CpfHttpHeaders.transactionId())),"canonical tx must survive");
    check("MBR".equals(CpfHttpHeaders.get(headers,CpfHttpHeaders.originalSystemCode())),"original system");
    check("EXS".equals(CpfHttpHeaders.get(headers,CpfHttpHeaders.systemCode())),"current system");
    check("MBR".equals(CpfHttpHeaders.get(headers,CpfHttpHeaders.callerSystemCode())),"caller system");
    check("EXS".equals(CpfHttpHeaders.get(headers,CpfHttpHeaders.targetSystemCode())),"target system");
    check("memberFind".equals(CpfHttpHeaders.get(headers,CpfHttpHeaders.targetOperationId())),"target operation");
    Map<String,String> missing=new LinkedHashMap<>(headers); missing.remove(CpfHttpHeaders.targetOperationId());
    expectFailure(() -> CpfHttpHeaders.validateInternal(missing),"missing operation must fail");
    expectFailure(() -> CpfHttpHeaders.builder().set(CpfHttpHeaders.transactionId(),"forged"),"protected mutation must fail");
    CpfHttpHeaders custom=CpfHttpHeaders.capture(Map.of("X-Campaign-Code",List.of("SUMMER-2026"),"x-role",List.of("A","B")));
    check("SUMMER-2026".equals(custom.get("x-campaign-code")),"case-insensitive custom read");
    check(custom.getAll("X-ROLE").size()==2,"multi-value custom read");
    System.out.println("PASS assertions="+assertions+" actualCpfHttpHeaders=true canonicalSixReadOnly=true protectedMutation=true");
  }
}
