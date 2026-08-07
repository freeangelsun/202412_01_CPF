package com.cpf.batch.control.deploy;

import com.cpf.batch.api.DeploymentCellManifest;
import com.cpf.batch.api.DeploymentRequest;
import com.cpf.batch.api.DeploymentResult;
import com.cpf.batch.control.security.BatVerifiedActorResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/batch/deployment-plans")
public class DeploymentPlanController {
    private final DeploymentPlanRepository plans;
    private final DeploymentEngine engine;
    private final DeploymentExecutionRepository executions;
    private final BatVerifiedActorResolver actorResolver;

    @Autowired
    public DeploymentPlanController(
            DeploymentPlanRepository plans,
            DeploymentEngine engine,
            DeploymentExecutionRepository executions,
            BatVerifiedActorResolver actorResolver) {
        this.plans = plans;
        this.engine = engine;
        this.executions = executions;
        this.actorResolver = actorResolver;
    }

    DeploymentPlanController(
            DeploymentPlanRepository plans,
            DeploymentEngine engine,
            BatVerifiedActorResolver actorResolver) {
        this(plans, engine, null, actorResolver);
    }

    @PostMapping
    ResponseEntity<DeploymentPlanRepository.Plan> plan(
            @RequestBody PlanRequest request,
            HttpServletRequest http) throws Exception {
        String id = request.planId() == null || request.planId().isBlank()
                ? UUID.randomUUID().toString()
                : request.planId();
        String actor = actorResolver.actor(http, request.requestedBy(), "requestedBy");
        return ResponseEntity.status(201)
                .body(plans.create(id, request.manifest(), actor, request.reason()));
    }

    /** Read-only owner observation used by ADM UNKNOWN reconciliation; never replays deployment. */
    @GetMapping("/{id}/executions/by-command/{commandRequestId}")
    ResponseEntity<Map<String, Object>> observeExecution(
            @PathVariable String id,
            @PathVariable String commandRequestId,
            HttpServletRequest http) throws Exception {
        actorResolver.identity(http);
        if (executions == null) return ResponseEntity.notFound().build();
        DeploymentCellManifest manifest = plans.load(id);
        return executions.findByIdempotency(manifest.cellId(), commandRequestId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/execute-approved")
    ResponseEntity<DeploymentResult> execute(
            @PathVariable String id,
            @RequestBody ApprovedExecution approval,
            HttpServletRequest http) throws Exception {
        DeploymentRequest request = request(id, approval, http);
        DeploymentResult result = engine.deploy(request);
        plans.mark(id, result.state().name());
        return ResponseEntity.accepted().body(result);
    }

    @PostMapping("/{id}/rollback-approved")
    ResponseEntity<DeploymentResult> rollback(
            @PathVariable String id,
            @RequestBody ApprovedExecution approval,
            HttpServletRequest http) throws Exception {
        DeploymentRequest request = request(id, approval, http);
        DeploymentResult result = engine.rollbackApproved(request);
        plans.mark(id, result.state().name());
        return ResponseEntity.accepted().body(result);
    }

    private DeploymentRequest request(
            String id,
            ApprovedExecution approval,
            HttpServletRequest http) throws Exception {
        var actors = actorResolver.approved(
                http,
                approval.requestedBy(),
                approval.approvedBy(),
                Long.toString(approval.approvalRequestId()));
        return new DeploymentRequest(
                approval.commandRequestId(),
                approval.commandRequestId(),
                plans.load(id),
                approval.expectedVersion(),
                actors.requestedBy(),
                approval.reason(),
                actors.approvalRequestId(),
                actors.approvedBy(),
                Instant.now().plusSeconds(900));
    }

    public record PlanRequest(
            String planId,
            DeploymentCellManifest manifest,
            String requestedBy,
            String reason) {
    }

    public record ApprovedExecution(
            long approvalRequestId,
            String commandRequestId,
            long expectedVersion,
            String requestedBy,
            String approvedBy,
            String reason) {
    }
}
