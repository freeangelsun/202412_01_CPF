package com.cpf.reference.edu.runtime.api;
import com.cpf.reference.edu.runtime.application.*;
import com.cpf.reference.edu.runtime.model.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import java.util.*;
@RestController
@RequestMapping({"/api/reference/edu-capabilities", "/reference/edu/capabilities"})
@Tag(name = "REF Reference Capabilities", description = "REF 교육 기능 카탈로그와 실행·대사 API")
public class EduCapabilityController {
    private final EduCapabilityRegistry registry; private final EduExecutionService service;
    public EduCapabilityController(EduCapabilityRegistry registry,EduExecutionService service){this.registry=registry;this.service=service;}
    @GetMapping public List<EduCapabilityDefinition> capabilities(){return registry.all().stream().map(AbstractEduCapabilityHandler::definition).toList();}
    @PostMapping("/{requirementId}/executions") public ResponseEntity<EduOperationRecord> execute(@PathVariable String requirementId,@RequestBody EduExecutionApiRequest body, Authentication authentication,
            @RequestHeader(value="X-Cpf-Request-Id",required=false) String requestId,
            @RequestHeader(value="X-Cpf-Trace-Id",required=false) String traceId){
        SecurityContext security = security(authentication);
        EduExecutionCommand c=new EduExecutionCommand(body.businessKey(),body.idempotencyKey(),body.expectedVersion(),security.actor(),security.roles(),security.dataScope(),body.requestReason(),blankToUuid(requestId),blankToUuid(traceId),body.payload()==null?Map.of():body.payload(),body.failurePoint()==null?EduFailurePoint.NONE:body.failurePoint(),body.autoApprove(),body.autoAcknowledge());
        EduOperationRecord r=service.execute(requirementId,c);return ResponseEntity.status(r.state()==EduExecutionState.WAITING_APPROVAL||r.state()==EduExecutionState.WAITING_EXTERNAL?HttpStatus.ACCEPTED:HttpStatus.OK).body(r);
    }
    @GetMapping("/executions/{operationId}") public EduOperationRecord get(@PathVariable String operationId){return service.require(operationId);}
    @GetMapping("/{requirementId}/executions") public List<EduOperationRecord> find(@PathVariable String requirementId,@RequestParam(defaultValue="100") int limit){return service.find(requirementId,Math.min(Math.max(limit,1),1000));}
    @GetMapping("/executions/{operationId}/audit") public List<EduAuditRecord> audit(@PathVariable String operationId){return service.audits(operationId);}
    @GetMapping("/executions/{operationId}/targets") public List<EduTargetRecord> targets(@PathVariable String operationId){return service.targets(operationId);}
    @GetMapping("/executions/{operationId}/outbox") public List<EduOutboxRecord> outbox(@PathVariable String operationId){return service.outbox(operationId);}
    @PostMapping("/executions/{operationId}/retry") public EduOperationRecord retry(@PathVariable String operationId,Authentication authentication,@RequestParam String reason){return service.retry(operationId,security(authentication).actor(),reason);}
    @PostMapping("/executions/{operationId}/reconcile") public EduOperationRecord reconcile(@PathVariable String operationId,Authentication authentication,@RequestParam String reason){return service.reconcile(operationId,security(authentication).actor(),reason);}
    @PostMapping("/executions/{operationId}/compensate") public EduOperationRecord compensate(@PathVariable String operationId,Authentication authentication,@RequestParam String reason){return service.compensate(operationId,security(authentication).actor(),reason);}
    @PostMapping("/executions/{operationId}/cancel") public EduOperationRecord cancel(@PathVariable String operationId,Authentication authentication,@RequestParam String reason){return service.cancel(operationId,security(authentication).actor(),reason);}

    private static SecurityContext security(Authentication authentication){
        if(authentication==null || !authentication.isAuthenticated() || authentication.getName()==null || authentication.getName().isBlank()){
            throw new org.springframework.security.access.AccessDeniedException("authenticated CPF security context required");
        }
        Set<String> roles=new LinkedHashSet<>();
        String scope="DEFAULT";
        for(GrantedAuthority authority:authentication.getAuthorities()){
            String value=authority.getAuthority();
            if(value==null||value.isBlank()) continue;
            if(value.startsWith("DATA_SCOPE_")) scope=value.substring("DATA_SCOPE_".length());
            else roles.add(value.startsWith("ROLE_")?value.substring(5):value);
        }
        return new SecurityContext(authentication.getName(),Set.copyOf(roles),scope);
    }
    private record SecurityContext(String actor,Set<String> roles,String dataScope){}
    private static String blankToUuid(String v){return v==null||v.isBlank()?UUID.randomUUID().toString():v;}
}
