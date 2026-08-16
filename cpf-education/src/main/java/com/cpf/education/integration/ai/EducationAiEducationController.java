package com.cpf.education.integration.ai;
import com.cpf.integration.ai.api.*;import java.time.Duration;import java.util.Map;import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;

/** Actual consumer of the provider-neutral CPF AI API. Does not expose provider native types. */
@RestController @RequestMapping("/api/education/ai") @ConditionalOnBean(CpfAiOperations.class)
/** EducationAiEducationController 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class EducationAiEducationController {
 private final CpfAiOperations ai;
 public EducationAiEducationController(CpfAiOperations ai){this.ai=ai;}
 @PostMapping("/invoke") ResponseEntity<CpfAiResponse> invoke(@RequestHeader("X-CPF-Transaction-Id") String tx,@RequestBody Request body){
   var request=new CpfAiRequest(tx,body.model(),body.prompt(),body.risk()==null?CpfAiRisk.LOW:body.risk(),Duration.ofSeconds(5),body.humanApproved(),Map.of("education","edu"));
   return ResponseEntity.ok(ai.execute(request));
 }
 public record Request(String model,String prompt,CpfAiRisk risk,boolean humanApproved){}
}
