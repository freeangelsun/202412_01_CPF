package com.cpf.admin.opr.service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Pure decision-rule evaluator shared by the ADM Approval Engine and Java 21 runtime harness. */
public final class AdmApprovalDecisionEvaluator {
    public Evaluation evaluate(boolean anyRejected, List<StepDecision> steps) {
        Objects.requireNonNull(steps, "steps");
        if (anyRejected) return new Evaluation(Status.REJECTED, "participant rejected");
        if (steps.isEmpty()) return new Evaluation(Status.PENDING, "no required approval steps");
        for (StepDecision step : steps) {
            long required = required(step);
            if (step.approvedCount() < required) {
                return new Evaluation(Status.PENDING,
                        "step " + step.stepNo() + " requires " + required
                                + " approvals but has " + step.approvedCount());
            }
        }
        return new Evaluation(Status.APPROVED, "all required approval steps satisfied");
    }

    private static long required(StepDecision step) {
        Objects.requireNonNull(step, "step");
        if (step.stepNo() < 1) throw new IllegalArgumentException("stepNo must be positive");
        if (step.participantCount() < 1) {
            throw new IllegalStateException("approval step has no participants: " + step.stepNo());
        }
        if (step.approvedCount() < 0 || step.approvedCount() > step.participantCount()) {
            throw new IllegalArgumentException("approvedCount is outside participant range");
        }
        String mode = requiredText(step.decisionRule()).toUpperCase(Locale.ROOT);
        long required = switch (mode) {
            case "ALL" -> step.participantCount();
            case "ANY" -> 1;
            case "N_OF_M" -> step.requiredCount() == null ? 0 : step.requiredCount();
            default -> throw new IllegalArgumentException("unsupported decision rule: " + mode);
        };
        if (required < 1 || required > step.participantCount()) {
            throw new IllegalStateException(
                    "invalid required approval count for step " + step.stepNo() + ": " + required);
        }
        return required;
    }

    private static String requiredText(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("decisionRule is required");
        return value.trim();
    }

    public enum Status { PENDING, APPROVED, REJECTED }

    public record StepDecision(
            int stepNo, String decisionRule, Long requiredCount,
            long participantCount, long approvedCount) {}

    public record Evaluation(Status status, String reason) {
        public Evaluation {
            Objects.requireNonNull(status, "status");
            reason = requiredText(reason);
        }
    }
}
