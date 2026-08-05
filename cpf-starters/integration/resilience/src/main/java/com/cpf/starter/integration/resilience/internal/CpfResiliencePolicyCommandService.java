package com.cpf.starter.integration.resilience.internal;

import com.cpf.core.api.resilience.CpfResiliencePolicy;
import com.cpf.core.api.resilience.CpfResiliencePolicyOperations;
import com.cpf.core.api.security.CpfSensitiveData;
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
    @Override public List<CpfResiliencePolicy> search(String filter,int page,int size) {
        validatePage(page,size);
        return store.search(optionalIdentifier(filter,"filter",256),Math.multiplyExact(page,size),size);
    }
    @Override public CpfResiliencePolicy find(String operationId) {
        String id=boundedIdentifier(operationId,"operationId",256);
        return store.findActive(id).orElseThrow(() -> new IllegalArgumentException("policy not found"));
    }
    @Override public String requestChange(CpfResiliencePolicy policy,String requesterId,String reason) {
        Objects.requireNonNull(policy, "policy");
        String requester = canonicalActor(requesterId, "requesterId");
        String changeReason = CpfSensitiveData.sanitizeAuditReason(reason);
        return transaction.required(() -> {
            String id = store.request(policy, requester, changeReason);
            audit.record("RESILIENCE_POLICY_REQUESTED", policy.operationId(), requester,
                    changeReason, Map.of("requestId", id), clock.instant());
            return id;
        });
    }
    @Override public CpfResiliencePolicy approveChange(String requestId,String approverId,String reason) {
        String id = boundedIdentifier(requestId, "requestId", 128);
        String approver = canonicalActor(approverId, "approverId");
        String approvalReason = CpfSensitiveData.sanitizeAuditReason(reason);
        return transaction.required(() -> {
            CpfResiliencePolicy policy = store.approve(id, approver, approvalReason);
            audit.record("RESILIENCE_POLICY_APPROVED", policy.operationId(), approver,
                    approvalReason, Map.of("requestId", id,
                            "revision", Long.toString(policy.revision())), clock.instant());
            return policy;
        });
    }
    @Override public void rejectChange(String requestId,String approverId,String reason) {
        String id = boundedIdentifier(requestId, "requestId", 128);
        String approver = canonicalActor(approverId, "approverId");
        String rejectionReason = CpfSensitiveData.sanitizeAuditReason(reason);
        transaction.required(() -> {
            store.reject(id, approver, rejectionReason);
            audit.record("RESILIENCE_POLICY_REJECTED", "UNKNOWN", approver,
                    rejectionReason, Map.of("requestId", id), clock.instant());
        });
    }
    private static void validatePage(int page,int size){
        if(page<0||size<1||size>500)throw new IllegalArgumentException("invalid paging");
        Math.multiplyExact(page, size);
    }
    private static String canonicalActor(String value,String name){
        String normalized=boundedIdentifier(value,name,128);
        if(!normalized.matches("[A-Za-z0-9][A-Za-z0-9._:@-]*")){
            throw new IllegalArgumentException(name+" contains unsupported characters");
        }
        return normalized;
    }
    private static String optionalIdentifier(String value,String name,int maximum){
        if(value==null||value.isBlank())return "";
        return boundedIdentifier(value,name,maximum);
    }
    private static String boundedIdentifier(String value,String name,int maximum){
        if(value==null||value.isBlank())throw new IllegalArgumentException(name+" is required");
        String normalized=value.trim();
        if(normalized.length()>maximum)throw new IllegalArgumentException(name+" exceeds "+maximum+" characters");
        if(normalized.chars().anyMatch(Character::isISOControl)){
            throw new IllegalArgumentException(name+" contains control characters");
        }
        return normalized;
    }
}
