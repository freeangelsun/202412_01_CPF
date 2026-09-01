package com.cpf.admin.opr.health;

import org.springframework.beans.factory.annotation.Autowired;
import com.cpf.platform.operations.api.health.CpfRuntimeHealth;
import com.cpf.platform.operations.api.health.CpfRuntimeHealthRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** ADM multi-instance health 집계. Runtime observedAt을 canonical freshness 기준으로 사용합니다. */
@Component
public final class AdmHealthInstanceRegistry {
    public record Entry(CpfRuntimeHealth health,Instant reportedAt,boolean stale) {}
    private final ConcurrentMap<String,CpfRuntimeHealth> local=new ConcurrentHashMap<>();
    private final CpfRuntimeHealthRegistry persistent;
    private final Clock clock;
    private final Duration staleAfter;
    // 생성자가 둘이면 Spring 은 어느 쪽을 쓸지 정하지 못하고 기본 생성자를 찾다가 기동에
    // 실패한다. 운영 주입 대상 생성자를 명시한다. 나머지는 테스트 seam 이다.
    @Autowired
    public AdmHealthInstanceRegistry(ObjectProvider<CpfRuntimeHealthRegistry> providers,@Value("${cpf.adm.health.stale-after:PT90S}") Duration staleAfter){this(providers.getIfAvailable(),staleAfter,Clock.systemUTC());}
    AdmHealthInstanceRegistry(Duration staleAfter,Clock cpfStarterClock){this(null,staleAfter,cpfStarterClock);}
    AdmHealthInstanceRegistry(CpfRuntimeHealthRegistry persistent,Duration staleAfter,Clock cpfStarterClock){if(staleAfter==null||staleAfter.isZero()||staleAfter.isNegative())throw new IllegalArgumentException("staleAfter must be positive");this.persistent=persistent;this.staleAfter=staleAfter;this.clock=cpfStarterClock;}
    public Entry report(CpfRuntimeHealth h){Objects.requireNonNull(h);required(h.systemId(),"systemId");required(h.instanceId(),"instanceId");if(persistent!=null){persistent.upsert(h);return entry(h);}local.compute(h.instanceKey(),(k,p)->p!=null&&p.observedAt().isAfter(h.observedAt())?p:h);return entry(local.get(h.instanceKey()));}
    public Optional<Entry> find(String s,String i){return persistent!=null?persistent.find(required(s,"systemId"),required(i,"instanceId")).map(this::entry):Optional.ofNullable(local.get(key(required(s,"systemId"),required(i,"instanceId")))).map(this::entry);}
    public List<Entry> search(String s,String readiness,boolean includeStale,int page,int size){int safePage=Math.max(0,page),safeSize=Math.min(200,Math.max(1,size));return source().stream().map(this::entry).filter(v->matches(v,s,readiness)).filter(v->includeStale||!v.stale()).sorted(Comparator.comparing((Entry v)->v.health().systemId()).thenComparing(v->v.health().instanceId())).skip((long)safePage*safeSize).limit(safeSize).toList();}
    public long count(String s,String readiness,boolean includeStale){return source().stream().map(this::entry).filter(v->matches(v,s,readiness)).filter(v->includeStale||!v.stale()).count();}
    private List<CpfRuntimeHealth> source(){return persistent!=null?persistent.list():List.copyOf(local.values());}
    private boolean matches(Entry v,String s,String readiness){String system=normalizeText(s),ready=normalizeReadiness(readiness);return(system==null||v.health().systemId().toLowerCase(Locale.ROOT).contains(system))&&(ready==null||v.health().readiness().name().equals(ready));}
    private Entry entry(CpfRuntimeHealth health){Instant at=health.observedAt();return new Entry(health,at,Duration.between(at,clock.instant()).compareTo(staleAfter)>0);}
    private static String key(String s,String i){return s+":"+i;}
    private static String normalizeText(String s){return s==null||s.isBlank()?null:s.trim().toLowerCase(Locale.ROOT);}
    private static String normalizeReadiness(String s){return s==null||s.isBlank()?null:s.trim().toUpperCase(Locale.ROOT);}
    private static String required(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" required");return v.trim();}
}
