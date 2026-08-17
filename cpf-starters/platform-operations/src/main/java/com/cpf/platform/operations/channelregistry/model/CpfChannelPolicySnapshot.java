package com.cpf.platform.operations.channelregistry.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 요청 처리 중 잠금 없이 읽는 불변 Channel Policy LKG Snapshot입니다. */
public record CpfChannelPolicySnapshot(
        long version,
        Instant loadedAt,
        Instant expiresAt,
        Status status,
        Map<String, CpfChannelDefinition> channels,
        List<CpfChannelExecutionPolicy> policies) {

    public enum Status { CURRENT, STALE, EXPIRED, REFRESH_FAILED }

    public CpfChannelPolicySnapshot {
        if (version < 0) throw new IllegalArgumentException("스냅샷 버전은 0 이상이어야 합니다.");
        loadedAt = loadedAt == null ? Instant.now() : loadedAt;
        expiresAt = expiresAt == null ? loadedAt : expiresAt;
        status = status == null ? Status.CURRENT : status;
        channels = Map.copyOf(channels == null ? Map.of() : new LinkedHashMap<>(channels));
        policies = List.copyOf(policies == null ? List.of() : policies);
    }

    public static CpfChannelPolicySnapshot loaded(long version, Instant loadedAt, Duration maxStale,
            Map<String,CpfChannelDefinition> channels, List<CpfChannelExecutionPolicy> policies) {
        Instant at=loadedAt==null?Instant.now():loadedAt; Duration ttl=maxStale==null?Duration.ZERO:maxStale;
        if(ttl.isNegative()||ttl.isZero())throw new IllegalArgumentException("channel policy maxStale must be positive");
        return new CpfChannelPolicySnapshot(version,at,at.plus(ttl),Status.CURRENT,channels,policies);
    }

    public CpfChannelPolicySnapshot withStatus(Status next) {
        return new CpfChannelPolicySnapshot(version,loadedAt,expiresAt,next,channels,policies);
    }

    public boolean expiredAt(Instant now) { return now == null || !now.isBefore(expiresAt); }

    public Optional<CpfChannelExecutionPolicy> resolve(String standardExecutionId,String originalChannelCode,
            String callerChannelCode,String requestType,Instant evaluatedAt) {
        return policies.stream().filter(policy->policy.isEffectiveAt(evaluatedAt))
                .filter(policy->matches(policy.standardExecutionId(),standardExecutionId))
                .filter(policy->matches(policy.originalChannelCode(),originalChannelCode))
                .filter(policy->matches(policy.callerChannelCode(),callerChannelCode))
                .filter(policy->matches(policy.requestType(),requestType))
                .max(Comparator.comparingInt(policy->specificity(policy,standardExecutionId,originalChannelCode,callerChannelCode,requestType)));
    }

    /** DB 없는 단위 테스트 등 명시적 비운영 구성에서 사용하는 fail-close snapshot입니다. */
    public static CpfChannelPolicySnapshot denyAll(Duration maxStale) {
        Duration ttl=maxStale==null||maxStale.isNegative()||maxStale.isZero()?Duration.ofMinutes(5):maxStale;
        Instant now=Instant.now();
        return new CpfChannelPolicySnapshot(0,now,now.plus(ttl),Status.CURRENT,Map.of(),List.of());
    }

    private boolean matches(String configured,String actual){return configured!=null&&actual!=null&&("*".equals(configured)||"ANY".equals(configured)||configured.equalsIgnoreCase(actual));}
    private int specificity(CpfChannelExecutionPolicy policy,String executionId,String originalChannel,String callerChannel,String requestType){
        int score=0; score+=equals(policy.standardExecutionId(),executionId)?8:0; score+=equals(policy.originalChannelCode(),originalChannel)?4:0;
        score+=equals(policy.callerChannelCode(),callerChannel)?2:0; score+=equals(policy.requestType(),requestType)?1:0; return score;
    }
    private boolean equals(String a,String b){return a!=null&&b!=null&&a.equalsIgnoreCase(b);}
}
