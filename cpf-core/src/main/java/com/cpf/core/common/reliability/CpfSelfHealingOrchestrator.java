package com.cpf.core.common.reliability;

import com.cpf.core.api.reliability.CpfSelfHealingActionPort;
import com.cpf.core.api.reliability.CpfSelfHealingEventSink;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Health/운영 이벤트가 요청한 자동복구 조치를 bounded guard 뒤에서 실행하는 공통 orchestrator입니다.
 * 허용 action allowlist, 승인 필요 action, cooldown/window/failure cutoff를 통과해야만 Owner Port가 호출됩니다.
 */
public final class CpfSelfHealingOrchestrator {
    private final CpfSelfHealingGuard guard;
    private final CpfSelfHealingActionPort actionPort;
    private final CpfSelfHealingEventSink eventSink;
    private final Set<String> allowedActions;
    private final Set<String> approvalRequiredActions;

    public CpfSelfHealingOrchestrator(
            CpfSelfHealingGuard guard,
            CpfSelfHealingActionPort actionPort,
            CpfSelfHealingEventSink eventSink,
            Set<String> allowedActions,
            Set<String> approvalRequiredActions) {
        this.guard = Objects.requireNonNull(guard, "guard");
        this.actionPort = Objects.requireNonNull(actionPort, "actionPort");
        this.eventSink = eventSink == null ? CpfSelfHealingEventSink.noop() : eventSink;
        this.allowedActions = allowedActions == null ? Set.of() : Set.copyOf(allowedActions);
        this.approvalRequiredActions = approvalRequiredActions == null ? Set.of() : Set.copyOf(approvalRequiredActions);
    }

    public Outcome attempt(Request request, Instant now) {
        Objects.requireNonNull(request, "request");
        Instant instant = now == null ? Instant.now() : now;
        String target = requireText(request.targetKey(), "targetKey");
        String action = requireText(request.actionType(), "actionType");
        String reason = requireText(request.reason(), "reason");

        if (!allowedActions.contains(action)) {
            return denied(target, action, reason, "action-not-allowlisted", instant);
        }
        if (approvalRequiredActions.contains(action)
                && (request.approvalReference() == null || request.approvalReference().isBlank())) {
            return denied(target, action, reason, "approval-required", instant);
        }

        CpfSelfHealingGuard.Decision decision = guard.allow(target + ":" + action, instant);
        if (!decision.allowed()) {
            return denied(target, action, reason, decision.reason(), instant);
        }

        try {
            CpfSelfHealingActionPort.ActionResult result = actionPort.execute(
                    new CpfSelfHealingActionPort.ActionCommand(
                            target,
                            action,
                            reason,
                            request.approvalReference(),
                            request.attributes()));
            if (result.success()) {
                guard.success(target + ":" + action);
                eventSink.publish(new CpfSelfHealingEventSink.Event(target, action, "SUCCEEDED", reason, result.message(), instant));
                return new Outcome("SUCCEEDED", result.message());
            }
            guard.failure(target + ":" + action);
            eventSink.publish(new CpfSelfHealingEventSink.Event(target, action, "FAILED", reason, result.message(), instant));
            return new Outcome("FAILED", result.message());
        } catch (RuntimeException ex) {
            guard.failure(target + ":" + action);
            String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            eventSink.publish(new CpfSelfHealingEventSink.Event(target, action, "FAILED", reason, message, instant));
            return new Outcome("FAILED", message);
        }
    }

    private Outcome denied(String target, String action, String reason, String cause, Instant now) {
        eventSink.publish(new CpfSelfHealingEventSink.Event(target, action, "DENIED", reason, cause, now));
        return new Outcome("DENIED", cause);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value;
    }

    public record Request(
            String targetKey,
            String actionType,
            String reason,
            String approvalReference,
            Map<String, String> attributes) {
        public Request {
            attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        }
    }

    public record Outcome(String state, String message) { }
}
