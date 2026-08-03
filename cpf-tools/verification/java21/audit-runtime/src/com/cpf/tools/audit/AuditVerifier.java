package com.cpf.tools.audit;
import java.nio.file.Path;
public final class AuditVerifier {
 public static void main(String[] args){
   if(args.length!=3)throw new IllegalArgumentException("root expected sourceHead");
   var records=new AuditRuntimeStore(Path.of(args[0])).readAll();
   var v=AuditRuntimeStore.validate(records,Integer.parseInt(args[1]));
   long headMismatch=records.stream().filter(r->!r.sourceHead().equals(args[2])).count();
   System.out.printf("VERIFY count=%d unique=%d headMismatch=%d passed=%s failures=%s%n",records.size(),v.uniqueCount(),headMismatch,v.passed()&&headMismatch==0,v.failures());
   if(!v.passed()||headMismatch!=0)System.exit(2);
 }
}
