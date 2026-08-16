package com.cpf.tools.audit;
import java.nio.file.Files; import java.nio.file.Path;
public final class AuditFailureProbe {
 public static void main(String[] args)throws Exception{
   Path base=Path.of(args[0]); Files.createDirectories(base); Path blocker=base.resolve("not-a-directory"); Files.writeString(blocker,"x");
   boolean writeFailed=false; try{new AuditRuntimeStore(blocker).append(new AuditRuntimeStore.AuditCommand("d","tx","tr","ex","i",1,"x",args[1]));}catch(IllegalStateException e){writeFailed=e.getMessage().startsWith("AUDIT_WRITE_FAILED");System.out.println("WRITE_FAILURE="+e.getMessage());}
   boolean readFailed=false; try{new AuditRuntimeStore(base.resolve("missing")).readAll();}catch(IllegalStateException e){readFailed=e.getMessage().startsWith("AUDIT_READ_FAILED");System.out.println("READ_FAILURE="+e.getMessage());}
   if(!writeFailed||!readFailed)System.exit(3); System.out.println("PASS fail-closed probes=2");
 }
}
