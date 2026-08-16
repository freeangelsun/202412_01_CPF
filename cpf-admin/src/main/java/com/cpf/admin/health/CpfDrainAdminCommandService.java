package com.cpf.admin.health;

import com.cpf.platform.operations.api.health.*;
import java.time.Duration;
import java.time.Instant;

/** Drain/Resume 위험조치를 권한·승인·사유·감사와 함께 수행하는 ADM Consumer입니다. */
public final class CpfDrainAdminCommandService {
    private final CpfDrainControl drain;
    private final CpfDrainCommandAuthorizer authorizer;
    private final CpfDrainAuditSink audit;
    private final String instanceId;
    public CpfDrainAdminCommandService(CpfDrainControl drain, CpfDrainCommandAuthorizer authorizer, CpfDrainAuditSink audit, String instanceId) {
        this.drain = drain; this.authorizer = authorizer; this.audit = audit; this.instanceId = instanceId;
    }
    public CpfDrainState drain(String actor, String reason, String approvalId, Duration timeout) {
        require(actor, reason, approvalId); authorizer.authorize(actor, "DRAIN", approvalId);
        try { CpfDrainState result = drain.beginDrain(timeout); record("DRAIN", actor, reason, approvalId, result.name()); return result; }
        catch (RuntimeException failure) { record("DRAIN", actor, reason, approvalId, "FAILED:" + failure.getClass().getSimpleName()); throw failure; }
    }
    public void resume(String actor, String reason, String approvalId) {
        require(actor, reason, approvalId); authorizer.authorize(actor, "RESUME", approvalId);
        try { drain.resume(); record("RESUME", actor, reason, approvalId, drain.state().name()); }
        catch (RuntimeException failure) { record("RESUME", actor, reason, approvalId, "FAILED:" + failure.getClass().getSimpleName()); throw failure; }
    }
    private static void require(String actor, String reason, String approvalId) {
        if (actor == null || actor.isBlank()) throw new IllegalArgumentException("actor required");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("reason required");
        if (approvalId == null || approvalId.isBlank()) throw new IllegalArgumentException("approvalId required");
    }
    private void record(String action, String actor, String reason, String approvalId, String result) {
        audit.record(new CpfDrainAuditEvent(action, actor, reason, approvalId, instanceId, result, Instant.now()));
    }
}
