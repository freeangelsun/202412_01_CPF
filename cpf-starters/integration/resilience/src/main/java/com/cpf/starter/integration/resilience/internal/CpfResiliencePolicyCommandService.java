package com.cpf.starter.integration.resilience.internal;

import com.cpf.core.api.resilience.CpfResiliencePolicy;
import com.cpf.core.api.resilience.CpfResiliencePolicyOperations;
import com.cpf.core.spi.resilience.CpfResilienceAuditSink;
import com.cpf.core.spi.resilience.CpfResiliencePolicyStore;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Two-person approval command/query implementation. */
public final class CpfResiliencePolicyCommandService implements CpfResiliencePolicyOperations {
    private final CpfResiliencePolicyStore store;
    private final CpfResilienceAuditSink audit;
    private final Clock clock;
    private final CpfResilienceTransactionRunner transaction;

    public CpfResiliencePolicyCommandService(CpfResiliencePolicyStore store,
                                              CpfResilienceAuditSink audit,
                                              Clock clock) {
        this(store, audit, clock, CpfResilienceTransactionRunner.direct());
    }

    CpfResiliencePolicyCommandService(CpfResiliencePolicyStore store,
                                      CpfResilienceAuditSink audit,
                                      Clock clock,
                                      CpfResilienceTransactionRunner transaction) {
        this.store = Objects.requireNonNull(store, "store");
        this.audit = Objects.requireNonNull(audit, "audit");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.transaction = Objects.requireNonNull(transaction, "transaction");
    }
    @Override public List<CpfResiliencePolicy> search(String filter,int page,int size) { validatePage(page,size); return store.search(filter,page*size,size); }
    @Override public CpfResiliencePolicy find(String operationId) { return store.findActive(operationId).orElseThrow(() -> new IllegalArgumentException("policy not found")); }
    @Override public String requestChange(CpfResiliencePolicy policy,String requesterId,String reason) {
        Objects.requireNonNull(policy, "policy");
        String requester = required(requesterId, "requesterId");
        String changeReason = required(reason, "reason");
        return transaction.required(() -> {
            String id = store.request(policy, requester, changeReason);
            audit.record("RESILIENCE_POLICY_REQUESTED", policy.operationId(), requester,
                    changeReason, Map.of("requestId", id), clock.instant());
            return id;
        });
    }
    @Override public CpfResiliencePolicy approveChange(String requestId,String approverId,String reason) {
        String id = required(requestId, "requestId");
        String approver = required(approverId, "approverId");
        String approvalReason = required(reason, "reason");
        return transaction.required(() -> {
            CpfResiliencePolicy policy = store.approve(id, approver, approvalReason);
            audit.record("RESILIENCE_POLICY_APPROVED", policy.operationId(), approver,
                    approvalReason, Map.of("requestId", id,
                            "revision", Long.toString(policy.revision())), clock.instant());
            return policy;
        });
    }
    @Override public void rejectChange(String requestId,String approverId,String reason) {
        String id = required(requestId, "requestId");
        String approver = required(approverId, "approverId");
        String rejectionReason = required(reason, "reason");
        transaction.required(() -> {
            store.reject(id, approver, rejectionReason);
            audit.record("RESILIENCE_POLICY_REJECTED", "UNKNOWN", approver,
                    rejectionReason, Map.of("requestId", id), clock.instant());
        });
    }
    private static void validatePage(int page,int size){if(page<0||size<1||size>500)throw new IllegalArgumentException("invalid paging");}
    private static String required(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" is required");return v.trim();}
}
