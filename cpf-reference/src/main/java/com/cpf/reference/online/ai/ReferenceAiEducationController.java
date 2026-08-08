package com.cpf.reference.online.ai;

import com.cpf.core.api.ai.*;import java.time.Duration;import java.util.Map;import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;

/** Actual consumer of the provider-neutral CPF AI API. Does not expose provider native types. */
@RestController @RequestMapping("/api/reference/ai") @ConditionalOnBean(CpfAiOperations.class)
public class ReferenceAiEducationController {
 private final CpfAiOperations ai;
 public ReferenceAiEducationController(CpfAiOperations ai){this.ai=ai;}
 @PostMapping("/invoke") ResponseEntity<CpfAiResponse> invoke(@RequestHeader("X-CPF-Transaction-Id") String tx,@RequestBody Request body){
   var request=new CpfAiRequest(tx,body.model(),body.prompt(),body.risk()==null?CpfAiRisk.LOW:body.risk(),Duration.ofSeconds(5),body.humanApproved(),Map.of("reference","edu"));
   return ResponseEntity.ok(ai.execute(request));
 }
 public record Request(String model,String prompt,CpfAiRisk risk,boolean humanApproved){}
}
