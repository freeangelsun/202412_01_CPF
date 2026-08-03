package com.cpf.starter.platform.operations.feature.flag.openfeature.internal;

import com.cpf.core.api.featureflag.*;
import com.cpf.core.spi.featureflag.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Precedence: kill switch/secure override -> cache -> provider -> fallback, with persistent audit. */
public final class CpfFeatureFlagRuntime implements CpfFeatureFlagOperations {
    private final CpfFeatureFlagProvider provider;
    private final CpfFeatureFlagStateStore state;
    private final CpfFeatureFlagAuditSink audit;
    private final Clock clock;
    private final Duration cacheTtl;
    private final CpfFeatureFlagTransactionRunner transaction;
    private final ConcurrentHashMap<CacheKey, CacheEntry> cache = new ConcurrentHashMap<>();
    private volatile long observedRevision = -1;

    public CpfFeatureFlagRuntime(CpfFeatureFlagProvider provider, CpfFeatureFlagStateStore state,
                                 CpfFeatureFlagAuditSink audit, Clock clock, Duration cacheTtl) {
        this(provider, state, audit, clock, cacheTtl, CpfFeatureFlagTransactionRunner.direct());
    }

    CpfFeatureFlagRuntime(CpfFeatureFlagProvider provider, CpfFeatureFlagStateStore state,
                          CpfFeatureFlagAuditSink audit, Clock clock, Duration cacheTtl,
                          CpfFeatureFlagTransactionRunner transaction) {
        this.provider=Objects.requireNonNull(provider); this.state=Objects.requireNonNull(state);
        this.audit=Objects.requireNonNull(audit); this.clock=Objects.requireNonNull(clock);
        this.transaction=Objects.requireNonNull(transaction, "transaction");
        if(cacheTtl==null||cacheTtl.isNegative()||cacheTtl.isZero())throw new IllegalArgumentException("cacheTtl must be positive");
        this.cacheTtl=cacheTtl;
    }

    @Override public CpfFeatureFlagResult<CpfFeatureFlagValue> evaluate(String flagKey,CpfFeatureFlagValue fallback,CpfFeatureFlagContext context){
        flagKey=required(flagKey,"flagKey"); Objects.requireNonNull(fallback); Objects.requireNonNull(context);
        Instant now=clock.instant(); long revision=state.revision();
        if(revision!=observedRevision){cache.clear();observedRevision=revision;}
        Optional<CpfFeatureFlagResult<CpfFeatureFlagValue>> controlled=state.findEffective(flagKey,now);
        if(controlled.isPresent())return audited(controlled.get(),context,"CONTROLLED_STATE");
        CacheKey key=new CacheKey(flagKey,context.targetingKey(),canonical(context.attributes()),provider.revision(),revision);
        CacheEntry cached=cache.get(key);
        if(cached!=null&&cached.expiresAt.isAfter(now))return audited(withSource(cached.result,CpfFeatureFlagResult.Source.CACHE,now),context,"CACHE_HIT");
        CpfFeatureFlagResult<CpfFeatureFlagValue> result;
        try { result=provider.evaluate(flagKey,fallback,context); }
        catch(RuntimeException e){result=new CpfFeatureFlagResult<>(flagKey,fallback,null,"PROVIDER_ERROR",CpfFeatureFlagResult.Source.FALLBACK,revision,now);}
        cache.put(key,new CacheEntry(result,now.plus(cacheTtl)));
        return audited(result,context,"EVALUATED");
    }
    @Override public List<CpfFeatureFlagResult<CpfFeatureFlagValue>> search(String filter,int page,int size){validatePage(page,size);return state.search(filter,page*size,size,clock.instant());}
    @Override public CpfFeatureFlagResult<CpfFeatureFlagValue> find(String flagKey){return state.findEffective(required(flagKey,"flagKey"),clock.instant()).orElseThrow(()->new IllegalArgumentException("flag not found"));}
    @Override public String requestOverride(String flagKey,CpfFeatureFlagValue value,Instant expiresAt,String requester,String reason){
        if(expiresAt==null||!expiresAt.isAfter(clock.instant()))throw new IllegalArgumentException("expiresAt must be in the future");
        String key = required(flagKey, "flagKey");
        String requesterId = required(requester, "requesterId");
        String requestReason = required(reason, "reason");
        return transaction.required(() -> {
            String id = state.requestOverride(key, Objects.requireNonNull(value), expiresAt,
                    requesterId, requestReason);
            audit.record("FEATURE_FLAG_OVERRIDE_REQUESTED", key, requesterId, requestReason,
                    Map.of("requestId", id, "expiresAt", expiresAt.toString()), clock.instant());
            return id;
        });
    }
    @Override public CpfFeatureFlagResult<CpfFeatureFlagValue> approveOverride(String requestId,String approver,String reason){
        String id = required(requestId, "requestId");
        String approverId = required(approver, "approverId");
        String approvalReason = required(reason, "reason");
        CpfFeatureFlagResult<CpfFeatureFlagValue> result = transaction.required(() -> {
            CpfFeatureFlagResult<CpfFeatureFlagValue> approved = state.approveOverride(
                    id, approverId, approvalReason, clock.instant());
            audit.record("FEATURE_FLAG_OVERRIDE_APPROVED", approved.flagKey(), approverId,
                    approvalReason, Map.of("requestId", id,
                            "revision", Long.toString(approved.revision())), clock.instant());
            return approved;
        });
        invalidate();
        return result;
    }
    @Override public void revokeOverride(String requestId,String operator,String reason){
        String id=required(requestId,"requestId");String operatorId=required(operator,"operatorId");String revokeReason=required(reason,"reason");
        transaction.required(()->{state.revokeOverride(id,operatorId,revokeReason,clock.instant());audit.record("FEATURE_FLAG_OVERRIDE_REVOKED","UNKNOWN",operatorId,revokeReason,Map.of("requestId",id),clock.instant());});invalidate();
    }
    @Override public void setKillSwitch(String flagKey,boolean enabled,String operator,String reason){
        String key=required(flagKey,"flagKey");String operatorId=required(operator,"operatorId");String changeReason=required(reason,"reason");
        transaction.required(()->{state.setKillSwitch(key,enabled,operatorId,changeReason,clock.instant());audit.record("FEATURE_FLAG_KILL_SWITCH_CHANGED",key,operatorId,changeReason,Map.of("enabled",Boolean.toString(enabled)),clock.instant());});invalidate();
    }
    private void invalidate(){observedRevision=-1;cache.clear();}
    private CpfFeatureFlagResult<CpfFeatureFlagValue> audited(CpfFeatureFlagResult<CpfFeatureFlagValue> r,CpfFeatureFlagContext c,String reason){
        audit.record("FEATURE_FLAG_EVALUATED",r.flagKey(),null,reason,Map.of("source",r.source().name(),"revision",Long.toString(r.revision()),"targetingKeyHash",Integer.toHexString(c.targetingKey().hashCode())),clock.instant()); return r;
    }
    private static CpfFeatureFlagResult<CpfFeatureFlagValue> withSource(CpfFeatureFlagResult<CpfFeatureFlagValue> r,CpfFeatureFlagResult.Source s,Instant now){return new CpfFeatureFlagResult<>(r.flagKey(),r.value(),r.variant(),r.reasonCode(),s,r.revision(),now);}
    private static String canonical(Map<String,String> a){var t=new TreeMap<>(a);return t.toString();}
    private static String required(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" is required");return v.trim();}
    private static void validatePage(int p,int s){if(p<0||s<1||s>500)throw new IllegalArgumentException("invalid paging");}
    private record CacheKey(String flag,String target,String attributes,long providerRevision,long stateRevision){}
    private record CacheEntry(CpfFeatureFlagResult<CpfFeatureFlagValue> result,Instant expiresAt){}
}
