package com.cpf.gateway.runtime;

import com.cpf.gateway.api.CpfGatewayHealthStatus;

import java.time.Duration;
import java.time.OffsetDateTime;

/** Probe 결과를 Hysteresis·Freshness·Recovery 정책으로 상태 전환합니다. */
public final class CpfGatewayHealthEvaluator {
    public Evaluation evaluate(Policy policy, State previous, ProbeResult result, OffsetDateTime now) {
        if (policy == null || previous == null || result == null) throw new IllegalArgumentException("policy/state/result required");
        OffsetDateTime evaluatedAt = now == null ? OffsetDateTime.now() : now;
        if (previous.manualStatus() == CpfGatewayHealthStatus.DISABLED
                || previous.manualStatus() == CpfGatewayHealthStatus.DRAINING
                || previous.manualStatus() == CpfGatewayHealthStatus.MAINTENANCE) {
            return new Evaluation(previous.manualStatus(), previous.consecutiveSuccesses(), previous.consecutiveFailures(), "MANUAL_OVERRIDE", evaluatedAt);
        }
        int successes = result.success() ? previous.consecutiveSuccesses() + 1 : 0;
        int failures = result.success() ? 0 : previous.consecutiveFailures() + 1;
        CpfGatewayHealthStatus next = previous.status();
        String reason = result.code();
        if (!result.success() && failures >= policy.failureThreshold()) next = CpfGatewayHealthStatus.DOWN;
        else if (result.success() && previous.status() == CpfGatewayHealthStatus.DOWN) next = CpfGatewayHealthStatus.RECOVERING;
        else if (result.success() && previous.status() == CpfGatewayHealthStatus.RECOVERING
                && successes >= policy.recoverySuccessThreshold()) next = CpfGatewayHealthStatus.UP;
        else if (result.success() && successes >= policy.successThreshold()) next = CpfGatewayHealthStatus.UP;
        else if (!result.success() && failures > 0) next = CpfGatewayHealthStatus.DEGRADED;
        if (result.observedAt() == null || Duration.between(result.observedAt(), evaluatedAt).compareTo(policy.staleAfter()) > 0) {
            next = CpfGatewayHealthStatus.STALE;
            reason = "PROBE_STALE";
        }
        return new Evaluation(next, successes, failures, reason, evaluatedAt);
    }

    public record Policy(int successThreshold, int failureThreshold, int recoverySuccessThreshold, Duration staleAfter) {
        public Policy {
            if (successThreshold < 1 || failureThreshold < 1 || recoverySuccessThreshold < 1) throw new IllegalArgumentException("threshold must be positive");
            if (staleAfter == null || staleAfter.isNegative() || staleAfter.isZero()) throw new IllegalArgumentException("staleAfter must be positive");
        }
    }
    public record State(CpfGatewayHealthStatus status, CpfGatewayHealthStatus manualStatus, int consecutiveSuccesses, int consecutiveFailures) {
        public State { status = status == null ? CpfGatewayHealthStatus.UNKNOWN : status; }
    }
    public record ProbeResult(boolean success, String code, long durationMs, OffsetDateTime observedAt) {}
    public record Evaluation(CpfGatewayHealthStatus status, int consecutiveSuccesses, int consecutiveFailures, String reason, OffsetDateTime evaluatedAt) {}
}
