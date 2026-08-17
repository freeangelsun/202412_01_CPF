package com.cpf.platform.operations.runtimecontrol;

import com.cpf.foundation.execution.api.CpfOperationAccessPolicy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.jdbc.core.JdbcTemplate;

/** cpfDB Operation Policy를 LKG로 캐시하고 Controller 전 fail-close로 평가합니다. */
public final class CpfJdbcOperationAccessPolicy implements CpfOperationAccessPolicy {
    public enum Status { CURRENT, STALE, EXPIRED, REFRESH_FAILED }
    public record RuntimeStatus(long policyVersion, Instant loadedAt, Instant expiresAt, Status status, String reason) {}
    private record SystemEntry(String domain, boolean enabled) {}
    private record OperationEntry(boolean enabled, boolean allCallers, long version) {}
    private record Snapshot(long version,Instant loadedAt,Instant expiresAt,Status status,Map<String,SystemEntry> systems,
            Map<String,Boolean> domainAccess,Map<String,OperationEntry> operations,Map<String,Boolean> callerAccess) {}

    private final JdbcTemplate jdbc; private final Duration refreshInterval; private final Duration maxStale; private final Clock clock;
    private final AtomicReference<Snapshot> snapshot=new AtomicReference<>(); private volatile Instant nextRefresh=Instant.EPOCH;

    public CpfJdbcOperationAccessPolicy(JdbcTemplate jdbc,Duration refreshInterval,Duration maxStale,Clock clock){
        this.jdbc=Objects.requireNonNull(jdbc); this.refreshInterval=positive(refreshInterval,"refreshInterval"); this.maxStale=positive(maxStale,"maxStale"); this.clock=Objects.requireNonNull(clock); refreshRequired();
    }
    @Override public Decision evaluate(Request r){refreshIfDue(); Snapshot s=snapshot.get(); Instant now=clock.instant();
        if(s==null||!now.isBefore(s.expiresAt()))return Decision.deny("OPERATION_POLICY_EXPIRED",s==null?-1:s.version());
        String target=code(r.targetSystemCode()); SystemEntry targetSystem=s.systems().get(target);
        if(targetSystem==null)return Decision.deny("TARGET_SYSTEM_NOT_REGISTERED",s.version()); if(!targetSystem.enabled())return Decision.deny("TARGET_SYSTEM_DISABLED",s.version());
        OperationEntry op=s.operations().get(r.operationId()); if(op==null)return Decision.deny("OPERATION_NOT_REGISTERED",s.version()); if(!op.enabled())return Decision.deny("OPERATION_DISABLED",op.version());
        if(!r.trustedInternal())return Decision.allow(Math.max(s.version(),op.version()));
        String caller=code(r.callerSystemCode()); if(caller==null)return Decision.deny("CALLER_NOT_REGISTERED",s.version()); SystemEntry callerSystem=s.systems().get(caller);
        if(callerSystem==null)return Decision.deny("CALLER_NOT_REGISTERED",s.version()); if(!callerSystem.enabled())return Decision.deny("CALLER_DISABLED",s.version());
        if(!caller.equals(target)){Boolean domain=s.domainAccess().get(caller+"->"+target); if(!Boolean.TRUE.equals(domain))return Decision.deny("SYSTEM_DOMAIN_DENY",s.version());}
        if(op.allCallers())return Decision.allow(Math.max(s.version(),op.version()));
        Boolean allowed=s.callerAccess().get(r.operationId()+"|"+caller); return Boolean.TRUE.equals(allowed)?Decision.allow(Math.max(s.version(),op.version())):Decision.deny("OPERATION_CALLER_DENY",Math.max(s.version(),op.version()));
    }
    public synchronized RuntimeStatus refresh(){try{return status(refreshRequired());}catch(RuntimeException ex){Snapshot cur=snapshot.get();Instant now=clock.instant();
        if(cur!=null&&now.isBefore(cur.expiresAt())){Snapshot stale=new Snapshot(cur.version(),cur.loadedAt(),cur.expiresAt(),Status.REFRESH_FAILED,cur.systems(),cur.domainAccess(),cur.operations(),cur.callerAccess());snapshot.set(stale);nextRefresh=now.plus(refreshInterval);return status(stale);}
        if(cur!=null)snapshot.set(new Snapshot(cur.version(),cur.loadedAt(),cur.expiresAt(),Status.EXPIRED,cur.systems(),cur.domainAccess(),cur.operations(),cur.callerAccess()));throw ex;}}
    public RuntimeStatus runtimeStatus(){Snapshot s=snapshot.get();return s==null?new RuntimeStatus(-1,null,null,Status.EXPIRED,"NO_LKG"):status(s);}
    private void refreshIfDue(){if(!clock.instant().isBefore(nextRefresh))refresh();}
    private Snapshot refreshRequired(){Instant now=clock.instant(); Map<String,SystemEntry> systems=new HashMap<>();
        jdbc.query("SELECT system_code,domain_code,enabled_yn FROM cpf_system_registry",rs->systems.put(code(rs.getString(1)),new SystemEntry(code(rs.getString(2)),yes(rs.getString(3)))));
        if(systems.isEmpty())throw new IllegalStateException("cpf_system_registry has no active catalog data"); Map<String,Boolean> domain=new HashMap<>();
        jdbc.query("SELECT caller_system_code,target_system_code,allowed_yn FROM cpf_system_domain_access",rs->domain.put(code(rs.getString(1))+"->"+code(rs.getString(2)),yes(rs.getString(3))));
        Map<String,OperationEntry> ops=new HashMap<>(); jdbc.query("SELECT operation_id,enabled_yn,all_callers_yn,policy_version FROM cpf_operation_policy",rs->ops.put(rs.getString(1),new OperationEntry(yes(rs.getString(2)),yes(rs.getString(3)),rs.getLong(4))));
        Map<String,Boolean> callers=new HashMap<>(); jdbc.query("SELECT operation_id,caller_system_code,allowed_yn FROM cpf_operation_caller_policy",rs->callers.put(rs.getString(1)+"|"+code(rs.getString(2)),yes(rs.getString(3))));
        Long version=jdbc.queryForObject("SELECT COALESCE(MAX(policy_version),0) FROM cpf_operation_policy",Long.class); Snapshot loaded=new Snapshot(version==null?0:version,now,now.plus(maxStale),Status.CURRENT,Map.copyOf(systems),Map.copyOf(domain),Map.copyOf(ops),Map.copyOf(callers));
        snapshot.set(loaded);nextRefresh=now.plus(refreshInterval);return loaded;}
    private RuntimeStatus status(Snapshot s){return new RuntimeStatus(s.version(),s.loadedAt(),s.expiresAt(),s.status(),s.status().name());}
    private static boolean yes(String v){return "Y".equalsIgnoreCase(v);} private static String code(String v){return v==null||v.isBlank()?null:v.trim().toUpperCase(Locale.ROOT);}
    private static Duration positive(Duration v,String n){if(v==null||v.isZero()||v.isNegative())throw new IllegalArgumentException(n+" must be positive");return v;}
}
