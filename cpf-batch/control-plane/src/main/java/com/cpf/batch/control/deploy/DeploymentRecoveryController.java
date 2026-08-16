package com.cpf.batch.control.deploy;

import com.cpf.batch.control.security.BatVerifiedActorResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 위험한 배포 결과불명 대사를 요청자/승인자 분리 하에 수행합니다. */
@RestController
@RequestMapping("/api/v1/batch/deployment-executions")
public final class DeploymentRecoveryController {
    private final DeploymentEngine engine;
    private final BatVerifiedActorResolver actorResolver;

    public DeploymentRecoveryController(DeploymentEngine engine, BatVerifiedActorResolver actorResolver) {
        this.engine = engine;
        this.actorResolver = actorResolver;
    }

    @PostMapping("/{deploymentId}/reconcile-approved")
    ResponseEntity<DeploymentReconciliation> reconcile(
            @PathVariable String deploymentId,
            @RequestBody ApprovedRecovery approval,
            HttpServletRequest request) {
        var actors = actorResolver.approved(
                request,
                approval.requestedBy(),
                approval.approvedBy(),
                approval.approvalRequestId());
        return ResponseEntity.ok(engine.reconcileLockResult(
                deploymentId,
                actors.requestedBy(),
                actors.approvedBy(),
                actors.approvalRequestId(),
                approval.reason()));
    }

    public record ApprovedRecovery(
            String approvalRequestId,
            String requestedBy,
            String approvedBy,
            String reason) {}
}
