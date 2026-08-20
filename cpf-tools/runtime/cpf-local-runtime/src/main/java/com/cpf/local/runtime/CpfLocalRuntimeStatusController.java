package com.cpf.local.runtime;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
/** Local Web Runtime의 Profile, Module, Simulator, 안전 상태를 Typed 응답으로 제공합니다. */
@RestController
public class CpfLocalRuntimeStatusController {private final Environment e;
public CpfLocalRuntimeStatusController(Environment e){this.e=e;}
 @GetMapping("/cpf/local/runtime/status")
 public Status status(){return new Status("CPF_LOCAL_WEB",true,true,e.getProperty("server.port",Integer.class,8080),List.of(e.getActiveProfiles()),modules(),simulators(),Runtime.getRuntime()
         .maxMemory()/1024/1024,Instant.now());}
 private List<Module> modules(){return List.of(module("core",true),module("common",true),module("gateway",true),module("admin",true),module("backoffice",false),new Module("domains",e
         .getProperty("cpf.local.modules.domains.enabled",Boolean.class,false),e.getProperty("cpf.local.modules.domains.base-packages","")));}
 private Module module(String n,boolean d){return new Module(n,e.getProperty("cpf.local.modules."+n,Boolean.class,d),"");}
 private List<Simulator> simulators(){return List.of(sim("redis"),sim("broker"),sim("external"),sim("file-transfer"));}private Simulator sim(String n){return new Simulator(n,e
         .getProperty("cpf.local.simulators."+n+".enabled",Boolean.class,false),e.getProperty("cpf.local.simulators."+n+".provider","DISABLED"));}
 public record Status(String runtime,boolean developmentOnly,boolean singleJvm,int port,List<String> profiles,List<Module> modules,List<Simulator> simulators,long maxMemoryMb,Instant observedAt){}
         public record Module(String name,boolean enabled,String detail){}
    public record Simulator(String name,boolean enabled,String provider){}
}
