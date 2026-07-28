package com.cpf.core.common.reconciliation;
import java.util.*;import java.util.concurrent.atomic.AtomicReference;
/** Reconciliation Worker가 실제 소비하는 Runtime 정책입니다. */
public final class CpfReconciliationRuntimePolicy{
 private final AtomicReference<Snapshot>ref=new AtomicReference<>(Snapshot.defaults());public Snapshot current(){return ref.get();}
 public Snapshot replace(long version,boolean enabled,long queryIntervalMillis,int thresholdSeconds,int batchSize,int leaseSeconds,boolean manualResolutionRequired,Set<String>types){Snapshot n=new Snapshot(version,enabled,queryIntervalMillis,thresholdSeconds,batchSize,leaseSeconds,manualResolutionRequired,normalize(types));ref.set(n);return n;}
 private static Set<String>normalize(Set<String>s){if(s==null)return Set.of();LinkedHashSet<String>r=new LinkedHashSet<>();for(String v:s)if(v!=null&&!v.isBlank())r.add(v.trim().toUpperCase(Locale.ROOT));return Set.copyOf(r);}
 public record Snapshot(long version,boolean enabled,long queryIntervalMillis,int thresholdSeconds,int batchSize,int leaseSeconds,boolean manualResolutionRequired,Set<String>unknownTypes){public Snapshot{if(queryIntervalMillis<1000||queryIntervalMillis>3600000||thresholdSeconds<0||batchSize<1||batchSize>1000||leaseSeconds<5||leaseSeconds>3600)throw new IllegalArgumentException("reconciliation policy 범위 오류");unknownTypes=unknownTypes==null?Set.of():Set.copyOf(unknownTypes);}private static Snapshot defaults(){return new Snapshot(0,false,30000,60,100,60,true,Set.of());}}
}
