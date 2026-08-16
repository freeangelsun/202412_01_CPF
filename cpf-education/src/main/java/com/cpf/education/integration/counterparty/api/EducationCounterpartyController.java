package com.cpf.education.integration.counterparty.api;
import com.cpf.education.integration.counterparty.application.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/** HTTP boundary used only by cpf-education integration education scenarios. */
@RestController
@RequestMapping({"/api/education/edu-counterparty", "/education/edu/counterparty"})
@Tag(name = "EDU Education Counterparty", description = "EDU 외부기관 연계와 결과불명 대사 교육 시뮬레이터")
public class EducationCounterpartyController {
    private final EducationCounterpartyService service;
    public EducationCounterpartyController(EducationCounterpartyService service){this.service=service;}
    @PostMapping("/{family}/{scenario}")
    /** exchange 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String,Object>> exchange(@PathVariable String family,@PathVariable String scenario,
            @RequestHeader("X-Cpf-Requirement-Id") String requirementId,
            @RequestHeader("X-Cpf-Idempotency-Key") String idempotencyKey,
            @RequestHeader("X-Cpf-Trace-Id") String traceId,
            @RequestBody Map<String,Object> request){
        var result=service.exchange(family,scenario,requirementId,idempotencyKey,traceId,request);
        return ResponseEntity.status(result.httpStatus()).header("X-Cpf-Replayed",String.valueOf(result.replayed())).body(result.body());
    }
}
