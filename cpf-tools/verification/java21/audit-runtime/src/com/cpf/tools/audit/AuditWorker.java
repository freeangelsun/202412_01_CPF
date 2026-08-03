package com.cpf.tools.audit;
import java.nio.file.Path;
public final class AuditWorker {
    public static void main(String[] args) throws Exception {
        if (args.length != 7) throw new IllegalArgumentException("root instance start end delayMs sourceHead executionId");
        Path root=Path.of(args[0]); String instance=args[1]; int start=Integer.parseInt(args[2]); int end=Integer.parseInt(args[3]);
        long delay=Long.parseLong(args[4]); String head=args[5]; String execution=args[6];
        AuditRuntimeStore store=new AuditRuntimeStore(root);
        for(int i=start;i<=end;i++){
            String delivery="delivery-"+i;
            String reason="operation="+i+" password=superSecret token=rawToken ssn=900101-1234567";
            var result=store.append(new AuditRuntimeStore.AuditCommand(delivery,"tx-"+i,"trace-"+i,execution,instance,i,reason,head));
            System.out.printf("WRITE instance=%s pid=%d sequence=%d auditId=%d inserted=%s%n",instance,ProcessHandle.current().pid(),i,result.auditId(),result.inserted());
            System.out.flush();
            if(delay>0)Thread.sleep(delay);
        }
    }
}
