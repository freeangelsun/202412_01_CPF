package com.cpf.batch.control;
import com.cpf.batch.api.*;import com.cpf.batch.control.internal.*;import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import java.time.Duration;import java.util.*;
@RestController @RequestMapping("/api/v1/batch/runtime")
public class RuntimeControlController {
 private final JdbcRuntimeRegistry registry;private final JdbcRuntimeCommandRepository commands;private final RuntimeCommandExecutor executor;
 public RuntimeControlController(JdbcRuntimeRegistry registry,JdbcRuntimeCommandRepository commands,RuntimeCommandExecutor executor){this.registry=registry;this.commands=commands;this.executor=executor;}
 @PostMapping("/registrations") ResponseEntity<Void> register(@RequestBody RuntimeRegistration r){registry.register(r);return ResponseEntity.accepted().build();}
 @PostMapping("/heartbeats") ResponseEntity<Void> heartbeat(@RequestBody RuntimeHeartbeat h){registry.heartbeat(h);return ResponseEntity.accepted().build();}
 @GetMapping("/instances") List<Map<String,Object>> instances(@RequestParam(defaultValue="30")long seconds){return registry.list(Duration.ofSeconds(Math.max(5,seconds)));}
 @PostMapping("/commands") ResponseEntity<Map<String,Object>> command(@RequestBody RuntimeCommand c){return ResponseEntity.accepted().body(executor.execute(c));}
 @GetMapping("/commands/{key}") ResponseEntity<Map<String,Object>> state(@PathVariable String key){return commands.find(key).map(ResponseEntity::ok).orElseGet(()->ResponseEntity.notFound().build());}
}
