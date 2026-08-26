package com.cpf.batch.control.centercut;

import com.cpf.batch.api.CenterCutExecutionRequest;
import com.cpf.batch.control.security.BatVerifiedActorResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Center-Cut 실행 생성/제어 Owner API. 위험 상태 변경은 요청자/승인자 분리를 강제합니다. */
@RestController
@RequestMapping("/api/v1/batch/center-cut/executions")
public class CenterCutExecutionController {
    private final CenterCutExecutionService service;
    private final BatVerifiedActorResolver actorResolver;
    public CenterCutExecutionController(
            CenterCutExecutionService service,
            BatVerifiedActorResolver actorResolver) {
        this.service = service;
        this.actorResolver = actorResolver;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody CenterCutExecutionRequest request,
            HttpServletRequest http) throws Exception {
        String actor=actorResolver.actor(http,request.requestedBy(),"requestedBy");
        CenterCutExecutionRequest verified=new CenterCutExecutionRequest(
                request.centerCutJobId(),request.idempotencyKey(),request.parameters(),
                request.parameterSchemaVersion(),request.tpsLimit(),request.concurrencyLimit(),
                actor,request.reason(),request.transactionId(),request.parentSegmentId());
        return ResponseEntity.status(201).body(service.launch(verified));
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable String id) { return service.status(id); }

    @PostMapping("/{id}/{action}")
    public ResponseEntity<Map<String, Object>> action(@PathVariable String id, @PathVariable String action,
                                                       @RequestBody ApprovedOperationRequest request,
                                                       HttpServletRequest http) {
        var actors=actorResolver.approved(http,request.requestedBy(),request.approvedBy(),null);
        return ResponseEntity.accepted().body(
                service.transition(id, action, actors.requestedBy(), actors.approvedBy(), request.reason()));
    }

    public record ApprovedOperationRequest(String requestedBy, String approvedBy, String reason) {}
}
