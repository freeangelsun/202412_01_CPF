package com.cpf.batch.control.job;

import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.api.BatchJobDefinitionControlPort;
import com.cpf.batch.control.security.BatVerifiedActorResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BAT Owner의 Versioned Job Definition API입니다.
 *
 * <p>변경 요청의 작업자 값은 Request Body를 신뢰하지 않고 인증된 BAT Principal과 일치하는지
 * 검증한 뒤 Service에 전달합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/batch/job-definitions")
public class BatchJobDefinitionController {
    private final BatchJobDefinitionService service;
    private final BatVerifiedActorResolver actorResolver;

    public BatchJobDefinitionController(
            BatchJobDefinitionService service,
            BatVerifiedActorResolver actorResolver) {
        this.service = service;
        this.actorResolver = actorResolver;
    }

    @GetMapping
    public List<Map<String, Object>> list(
            @RequestParam(required = false) String jobId,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "200") int limit) {
        return service.list(jobId, state, limit);
    }

    @GetMapping("/{jobId}/versions/{version}")
    public BatchJobDefinitionControlPort.DefinitionState state(
            @PathVariable String jobId, @PathVariable long version) {
        return service.state(jobId, version);
    }

    @PostMapping("/validate")
    public BatchJobDefinitionService.ValidationResult validate(
            @RequestBody BatchJobDefinition definition) {
        return service.validate(definition);
    }

    @PostMapping("/drafts")
    public ResponseEntity<BatchJobDefinitionService.SavedDefinition> save(
            HttpServletRequest request,
            @RequestBody BatchJobDefinition definition) {
        String actor = actorResolver.actor(request, definition.requestedBy(), "requestedBy");
        return ResponseEntity.status(201).body(service.saveDraft(definition, actor));
    }

    @PostMapping("/{jobId}/versions/{version}/approved-publish")
    public BatchJobDefinitionControlPort.PublishResult approvedPublish(
            HttpServletRequest servletRequest,
            @PathVariable String jobId,
            @PathVariable long version,
            @RequestBody ApprovedPublishRequest request) {
        BatVerifiedActorResolver.ApprovedActors approved = actorResolver.approved(
                servletRequest, request.requestedBy(), request.approvedBy(),
                Long.toString(request.approvalRequestId()));
        return service.publishApproved(new BatchJobDefinitionControlPort.PublishCommand(
                required(request.operationId(), "operationId"), jobId, version,
                request.expectedRowVersion(), request.approvalRequestId(),
                required(request.payloadHash(), "payloadHash"), approved.requestedBy(),
                approved.approvedBy(), required(request.reason(), "reason")));
    }

    @PostMapping("/{jobId}/versions/{version}/transition")
    public BatchJobDefinitionService.SavedDefinition transition(
            HttpServletRequest servletRequest,
            @PathVariable String jobId,
            @PathVariable long version,
            @RequestBody TransitionRequest request) {
        String target = request.targetState() == null ? "" : request.targetState().trim().toUpperCase();
        BatchJobDefinitionService.AuditContext auditContext;
        String actor;
        if (java.util.Set.of("PUBLISHED", "RETIRED").contains(target)) {
            String suppliedApprover = request.approvedBy() == null || request.approvedBy().isBlank()
                    ? request.operatorId() : request.approvedBy();
            BatVerifiedActorResolver.ApprovedActors approved = actorResolver.approved(
                    servletRequest, request.requestedBy(), suppliedApprover, request.approvalRequestId());
            BatchJobDefinitionControlPort.DefinitionState current = service.state(jobId, version);
            if (!current.checksum().equalsIgnoreCase(required(request.payloadHash(), "payloadHash"))) {
                throw new SecurityException("Approved payload hash does not match Batch Definition checksum");
            }
            actor = approved.approvedBy();
            auditContext = new BatchJobDefinitionService.AuditContext(
                    approved.requestedBy(), approved.approvalRequestId(),
                    header(servletRequest, "X-CPF-Transaction-Id"),
                    firstHeader(servletRequest, "X-CPF-Trace-Id", "traceparent"));
        } else {
            actor = actorResolver.actor(servletRequest, request.operatorId(), "operatorId");
            auditContext = new BatchJobDefinitionService.AuditContext(
                    actor, null, header(servletRequest, "X-CPF-Transaction-Id"),
                    firstHeader(servletRequest, "X-CPF-Trace-Id", "traceparent"));
        }
        return service.transition(jobId, version, request.expectedRowVersion(), target,
                actor, request.reason(), auditContext);
    }

    /**
     * operatorId는 하위 호환 입력 필드이며 인증 Principal과 다르면 거부됩니다.
     */
    private static String header(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String firstHeader(HttpServletRequest request, String... names) {
        for (String name : names) {
            String value = header(request, name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    public record ApprovedPublishRequest(
            String operationId,
            long expectedRowVersion,
            long approvalRequestId,
            String payloadHash,
            String requestedBy,
            String approvedBy,
            String reason) {
    }

    public record TransitionRequest(
            long expectedRowVersion,
            String targetState,
            String operatorId,
            String requestedBy,
            String approvedBy,
            String approvalRequestId,
            String payloadHash,
            String reason) {
    }
}
