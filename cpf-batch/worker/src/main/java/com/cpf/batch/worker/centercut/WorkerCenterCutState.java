package com.cpf.batch.worker.centercut;

import com.cpf.batch.centercut.runtime.CenterCutWorkObserver;
import com.cpf.batch.centercut.runtime.JdbcCenterCutClaimRepository;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 실제 Worker의 DB Center-Cut Claim/Lease 상태를 공통 Runtime health에 제공합니다. */
@Component
public final class WorkerCenterCutState implements CenterCutWorkObserver {
    public static final String LEASE_LOST = "BAT_CENTER_CUT_LEASE_LOST";
    private final boolean enabled;
    private final ConcurrentMap<String, Active> active = new ConcurrentHashMap<>();
    private final AtomicReference<String> error = new AtomicReference<>();

    public WorkerCenterCutState(
            @Value("${cpf.batch.worker.center-cut.enabled:${CPF_BAT_WORKER_CENTER_CUT_ENABLED:true}}")
            boolean enabled) {
        this.enabled = enabled;
    }

    public boolean enabled() { return enabled; }
    public int activeCount() { return active.size(); }
    public List<String> executions() {
        return active.values().stream().map(value -> value.executionId()).distinct().sorted().toList();
    }
    public List<String> leases() { return active.keySet().stream().sorted().toList(); }
    public long fencingToken() {
        return active.values().stream().mapToLong(value -> value.fencingToken()).max().orElse(0L);
    }
    public String errorCode() { return error.get(); }
    public Map<String,String> dependencyHealth() {
        return Map.of("centerCutDbClaim", !enabled ? "DISABLED" : error.get()==null ? "UP" : "DOWN");
    }

    @Override
    public void claimed(JdbcCenterCutClaimRepository.Claim claim, JdbcCenterCutClaimRepository.Work work) {
        active.put(claim.claimToken(), new Active(work.executionId(), claim.fencingToken()));
    }

    @Override public void released(JdbcCenterCutClaimRepository.Claim claim) { active.remove(claim.claimToken()); }
    @Override public void repositoryHealthy() { error.set(null); }
    @Override public void repositoryFailure(RuntimeException failure) {
        error.set("BAT_CENTER_CUT_REPOSITORY_" + failure.getClass().getSimpleName().toUpperCase(java.util.Locale.ROOT));
    }
    @Override public void leaseLost(JdbcCenterCutClaimRepository.Claim claim) { error.set(LEASE_LOST); }

    private record Active(String executionId,long fencingToken) { }
}
