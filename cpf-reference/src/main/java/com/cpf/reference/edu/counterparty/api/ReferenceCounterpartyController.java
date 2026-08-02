package com.cpf.reference.edu.counterparty.api;

import com.cpf.reference.edu.counterparty.application.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/** HTTP boundary used only by cpf-reference integration education scenarios. */
@RestController
@RequestMapping({"/api/reference/edu-counterparty", "/reference/edu/counterparty"})
@Tag(name = "REF Reference Counterparty", description = "REF 외부기관 연계와 결과불명 대사 교육 시뮬레이터")
public class ReferenceCounterpartyController {
    private final ReferenceCounterpartyService service;
    public ReferenceCounterpartyController(ReferenceCounterpartyService service){this.service=service;}
    @PostMapping("/{family}/{scenario}")
    public ResponseEntity<Map<String,Object>> exchange(@PathVariable String family,@PathVariable String scenario,
            @RequestHeader("X-Cpf-Requirement-Id") String requirementId,
            @RequestHeader("X-Cpf-Idempotency-Key") String idempotencyKey,
            @RequestHeader("X-Cpf-Trace-Id") String traceId,
            @RequestBody Map<String,Object> request){
        var result=service.exchange(family,scenario,requirementId,idempotencyKey,traceId,request);
        return ResponseEntity.status(result.httpStatus()).header("X-Cpf-Replayed",String.valueOf(result.replayed())).body(result.body());
    }
}
