package com.cpf.admin.opr.health;
import com.cpf.core.api.health.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public final class AdmHealthInstanceRegistry {
 public record Entry(CpfRuntimeHealth health,Instant reportedAt,boolean stale) {}
 private record Stored(CpfRuntimeHealth health,Instant reportedAt) {}
 private final ConcurrentMap<String,Stored> local=new ConcurrentHashMap<>(); private final CpfRuntimeHealthRegistry persistent; private final Clock clock; private final Duration staleAfter;
 public AdmHealthInstanceRegistry(ObjectProvider<CpfRuntimeHealthRegistry> providers,@Value("${cpf.adm.health.stale-after:PT90S}") Duration staleAfter){this(providers.getIfAvailable(),staleAfter,Clock.systemUTC());}
 AdmHealthInstanceRegistry(Duration staleAfter,Clock clock){this(null,staleAfter,clock);}
 AdmHealthInstanceRegistry(CpfRuntimeHealthRegistry persistent,Duration staleAfter,Clock clock){if(staleAfter==null||staleAfter.isZero()||staleAfter.isNegative())throw new IllegalArgumentException("staleAfter must be positive");this.persistent=persistent;this.staleAfter=staleAfter;this.clock=clock;}
 public Entry report(CpfRuntimeHealth h){Objects.requireNonNull(h);required(h.systemId(),"systemId");required(h.instanceId(),"instanceId");Instant now=clock.instant();if(persistent!=null){persistent.report(h,now);return entry(new Stored(h,now),now);}local.compute(key(h.systemId(),h.instanceId()),(k,p)->p!=null&&p.reportedAt().isAfter(now)?p:new Stored(h,now));return entry(local.get(key(h.systemId(),h.instanceId())),now);}
 public Optional<Entry> find(String s,String i){Instant now=clock.instant();if(persistent!=null)return persistent.find(required(s,"systemId"),required(i,"instanceId")).map(v->entry(new Stored(v.health(),v.reportedAt()),now));return Optional.ofNullable(local.get(key(required(s,"systemId"),required(i,"instanceId")))).map(v->entry(v,now));}
 public List<Entry> search(String s,String readiness,boolean includeStale,int page,int size){int safePage=Math.max(0,page),safeSize=Math.min(200,Math.max(1,size));Instant now=clock.instant();List<Entry> all;if(persistent!=null){all=persistent.search(normalizeText(s),normalizeReadiness(readiness),safePage*safeSize,safeSize).stream().map(v->entry(new Stored(v.health(),v.reportedAt()),now)).toList();}else{all=local.values().stream().map(v->entry(v,now)).filter(v->matches(v,s,readiness)).sorted(Comparator.comparing((Entry v)->v.health().systemId()).thenComparing(v->v.health().instanceId())).skip((long)safePage*safeSize).limit(safeSize).toList();}return includeStale?all:all.stream().filter(v->!v.stale()).toList();}
 public long count(String s,String readiness,boolean includeStale){if(persistent!=null&&includeStale)return persistent.count(normalizeText(s),normalizeReadiness(readiness));if(persistent!=null)return search(s,readiness,false,0,200).size();Instant now=clock.instant();return local.values().stream().map(v->entry(v,now)).filter(v->matches(v,s,readiness)).filter(v->includeStale||!v.stale()).count();}
 private boolean matches(Entry v,String s,String readiness){String system=normalizeText(s),ready=normalizeReadiness(readiness);return(system==null||v.health().systemId().toLowerCase(Locale.ROOT).contains(system))&&(ready==null||v.health().readiness().name().equals(ready));}
 private Entry entry(Stored s,Instant now){return new Entry(s.health(),s.reportedAt(),Duration.between(s.reportedAt(),now).compareTo(staleAfter)>0);}
 private static String key(String s,String i){return s+'\0'+i;} private static String normalizeText(String s){return s==null||s.isBlank()?null:s.trim().toLowerCase(Locale.ROOT);} private static String normalizeReadiness(String s){return s==null||s.isBlank()?null:s.trim().toUpperCase(Locale.ROOT);} private static String required(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" required");return v.trim();}
}
