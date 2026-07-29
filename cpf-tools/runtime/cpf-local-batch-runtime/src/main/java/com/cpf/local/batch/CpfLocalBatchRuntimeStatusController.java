package com.cpf.local.batch;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
/** Local Batch Launcher의 역할 Context, Port, 안전 상태를 Typed 응답으로 제공합니다. */
@RestController
public class CpfLocalBatchRuntimeStatusController {private final Environment e;
public CpfLocalBatchRuntimeStatusController(Environment e){this.e=e;}
 @GetMapping("/cpf/local/batch/status")
 public Status status(){return new Status("CPF_LOCAL_BATCH",true,true,List.of(role("control-server",true,8090),role("scheduler",true,8091),role("worker",true,8092),role("center-cut",false,8093),
         role("host-agent",false,8094)),Instant.now());}
 private Role role(String n,boolean enabled,int port){return new Role(n,e.getProperty("cpf.local.batch.modules."+n,Boolean.class,enabled),e.getProperty("cpf.local.batch.ports."+n,Integer.class,port));}
 public record Status(String runtime,boolean developmentOnly,boolean singleJvm,List<Role> roles,Instant observedAt){}
    public record Role(String name,boolean enabled,int port){}
}
