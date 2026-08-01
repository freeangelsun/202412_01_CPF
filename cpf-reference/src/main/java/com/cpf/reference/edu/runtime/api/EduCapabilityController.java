package com.cpf.reference.edu.runtime.api;
import com.cpf.reference.edu.runtime.application.*;
import com.cpf.reference.edu.runtime.model.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api/reference/edu-capabilities")
public class EduCapabilityController {
    private final EduCapabilityRegistry registry; private final EduExecutionService service;
    public EduCapabilityController(EduCapabilityRegistry registry,EduExecutionService service){this.registry=registry;this.service=service;}
    @GetMapping public List<EduCapabilityDefinition> capabilities(){return registry.all().stream().map(AbstractEduCapabilityHandler::definition).toList();}
    @PostMapping("/{requirementId}/executions") public ResponseEntity<EduOperationRecord> execute(@PathVariable String requirementId,@RequestBody EduExecutionApiRequest body,
            @RequestHeader("X-Cpf-Actor-Id") String actor,@RequestHeader("X-Cpf-Roles") String roles,
            @RequestHeader("X-Cpf-Data-Scope") String scope,@RequestHeader(value="X-Cpf-Request-Id",required=false) String requestId,
            @RequestHeader(value="X-Cpf-Trace-Id",required=false) String traceId){
        EduExecutionCommand c=new EduExecutionCommand(body.businessKey(),body.idempotencyKey(),body.expectedVersion(),actor,new HashSet<>(Arrays.asList(roles.split(","))),scope,body.requestReason(),blankToUuid(requestId),blankToUuid(traceId),body.payload()==null?Map.of():body.payload(),body.failurePoint()==null?EduFailurePoint.NONE:body.failurePoint(),body.autoApprove(),body.autoAcknowledge());
        EduOperationRecord r=service.execute(requirementId,c);return ResponseEntity.status(r.state()==EduExecutionState.WAITING_APPROVAL||r.state()==EduExecutionState.WAITING_EXTERNAL?HttpStatus.ACCEPTED:HttpStatus.OK).body(r);
    }
    @GetMapping("/executions/{operationId}") public EduOperationRecord get(@PathVariable String operationId){return service.require(operationId);}
    @GetMapping("/{requirementId}/executions") public List<EduOperationRecord> find(@PathVariable String requirementId,@RequestParam(defaultValue="100") int limit){return service.find(requirementId,Math.min(Math.max(limit,1),1000));}
    @GetMapping("/executions/{operationId}/audit") public List<EduAuditRecord> audit(@PathVariable String operationId){return service.audits(operationId);}
    @GetMapping("/executions/{operationId}/targets") public List<EduTargetRecord> targets(@PathVariable String operationId){return service.targets(operationId);}
    @GetMapping("/executions/{operationId}/outbox") public List<EduOutboxRecord> outbox(@PathVariable String operationId){return service.outbox(operationId);}
    @PostMapping("/executions/{operationId}/retry") public EduOperationRecord retry(@PathVariable String operationId,@RequestHeader("X-Cpf-Actor-Id") String actor,@RequestParam String reason){return service.retry(operationId,actor,reason);}
    @PostMapping("/executions/{operationId}/reconcile") public EduOperationRecord reconcile(@PathVariable String operationId,@RequestHeader("X-Cpf-Actor-Id") String actor,@RequestParam String reason){return service.reconcile(operationId,actor,reason);}
    @PostMapping("/executions/{operationId}/compensate") public EduOperationRecord compensate(@PathVariable String operationId,@RequestHeader("X-Cpf-Actor-Id") String actor,@RequestParam String reason){return service.compensate(operationId,actor,reason);}
    @PostMapping("/executions/{operationId}/cancel") public EduOperationRecord cancel(@PathVariable String operationId,@RequestHeader("X-Cpf-Actor-Id") String actor,@RequestParam String reason){return service.cancel(operationId,actor,reason);}
    private static String blankToUuid(String v){return v==null||v.isBlank()?UUID.randomUUID().toString():v;}
}
