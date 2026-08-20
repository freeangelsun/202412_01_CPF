package com.cpf.common.management;

import com.cpf.common.parameter.api.CpfParameterValueCodec;
import com.cpf.common.spi.CpfCommonPersistenceNames;
import com.cpf.common.spi.CpfCommonCacheChangePublisher;
import com.cpf.core.api.error.CpfValidationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/** Fixed table/column allowlist 기반 Common 관리 JDBC 구현입니다. */
@Service
public final class JdbcCpfCommonManagementService implements CpfCommonManagementApi {
    private static final Set<String> AUDIT_COLUMNS = Set.of("created_by","created_at","updated_by","updated_at");
    private final JdbcTemplate jdbc;
    private final CpfCommonCacheChangePublisher invalidation;
    private final CpfCommonManagementAuditSink audit;
    private final ObjectProvider<CpfParameterValueCodec> parameterCodec;
    private final Clock clock;

    /** Common 전용 JDBC, 캐시 무효화, 감사 Sink와 Parameter Codec을 조합합니다. */
    public JdbcCpfCommonManagementService(@Qualifier("cpfCommonJdbcTemplate") JdbcTemplate cpfCommonJdbcTemplate, CpfCommonCacheChangePublisher invalidation, CpfCommonManagementAuditSink audit, ObjectProvider<CpfParameterValueCodec> parameterCodec) {
        this(cpfCommonJdbcTemplate, invalidation, audit, parameterCodec, Clock.systemUTC());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public JdbcCpfCommonManagementService(@Qualifier("cpfCommonJdbcTemplate") JdbcTemplate cpfCommonJdbcTemplate, CpfCommonCacheChangePublisher invalidation, CpfCommonManagementAuditSink audit, ObjectProvider<CpfParameterValueCodec> parameterCodec, Clock clock) {
        this.jdbc=cpfCommonJdbcTemplate; this.invalidation=invalidation; this.audit=audit; this.parameterCodec=parameterCodec; this.clock=Objects.requireNonNull(clock,"clock");
    }

    /** Allowlist로 제한한 Common 자원을 조건검색하고 최대 200건 단위로 안전하게 Paging합니다. */
    @Override
    public CpfCommonPage<Map<String,Object>> search(CpfCommonResource r, String query, int page, int size, boolean includeDisabled, Instant effectiveAt) {
        Objects.requireNonNull(r,"resource");
        int p=Math.max(0,page), s=Math.max(1,Math.min(size,200));
        List<Object> args=new ArrayList<>();
        String where=whereForSearch(r, query, includeDisabled, effectiveAt, args);
        Long total=jdbc.queryForObject("SELECT COUNT(*) FROM "+r.table()+where, Long.class, args.toArray());
        int from=p*s, to=from+s;
        List<Object> pageArgs=new ArrayList<>(args); pageArgs.add(from); pageArgs.add(to);
        String sql="SELECT * FROM (SELECT t.*, ROW_NUMBER() OVER(ORDER BY "+r.orderColumn()+") cpf_rn FROM "+r.table()+" t"+where+") cpf_page WHERE cpf_rn>? AND cpf_rn<=? ORDER BY cpf_rn";
        List<Map<String,Object>> rows=jdbc.queryForList(sql,pageArgs.toArray());
        long n=total==null?0:total; int pages=(int)((n+s-1)/s);
        return new CpfCommonPage<>(rows.stream().map(row->safeProjection(r,row)).toList(),p,s,n,pages);
    }

    @Override
    public Map<String,Object> get(CpfCommonResource r, Map<String,Object> identifiers) {
        List<Object> args=new ArrayList<>(); String where=keyWhere(r, identifiers,args);
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM "+r.table()+" WHERE "+where,args.toArray());
        if(rows.isEmpty()) throw new NoSuchElementException(r.name()+" not found");
        if(rows.size()!=1) throw new IllegalStateException("Common resource key is not unique");
        return safeProjection(r, rows.get(0));
    }

    /** 생성 데이터와 durable cache event를 같은 Common transaction으로 묶어 부분 반영을 방지합니다. */
    @Override
    @Transactional(transactionManager = CpfCommonPersistenceNames.TX_MANAGER_BEAN)
    public Map<String,Object> create(CpfCommonResource r, CpfCommonMutation mutation, String actor) {
        Objects.requireNonNull(r,"resource"); requireReason(mutation.reason()); String user=actor(actor);
        Map<String,Object> values=new LinkedHashMap<>(safeValues(r, mutation.values()));
        encodeParameterValueIfNeeded(r, values, null);
        for (String keyColumn : r.keyColumns()) {
            Object explicit = valueIgnoreCase(mutation.identifiers(), keyColumn);
            if (explicit != null && r.writableColumns().contains(keyColumn)) values.putIfAbsent(keyColumn, explicit);
        }
        if(values.isEmpty()) throw validation("values are required");
        addAuditForInsert(values,user);
        List<String> cols=new ArrayList<>(values.keySet());
        String sql="INSERT INTO "+r.table()+"("+String.join(",",cols)+") VALUES("+cols.stream().map(x->"?").collect(Collectors.joining(","))+")";
        try { if(jdbc.update(sql,cols.stream().map(values::get).map(JdbcCpfCommonManagementService::jdbcValue).toArray())!=1) throw new IllegalStateException("Common create affected unexpected rows"); }
        // DB unique/constraint 위반은 Vendor 예외를 노출하지 않고 Common Validation 오류로 일관되게 변환합니다.
        // DB unique/constraint 위반은 Vendor 예외를 노출하지 않고 Common Validation 오류로 일관되게 변환합니다.
        catch(DataIntegrityViolationException e){ throw validation("duplicate or invalid Common resource"); }
        Map<String,Object> keys=resolveKeysAfterCreate(r, mutation.identifiers(), values);
        String auditKey=eventKey(r,keys,values);
        invalidation.publishRequired(r.cacheName(),"UPSERT",auditKey,user);
        auditAfterCommit("CREATE",r,auditKey,user,mutation.reason());
        return keys.isEmpty()?Collections.unmodifiableMap(new LinkedHashMap<>(values)):get(r,keys);
    }

    /** Optimistic version 조건과 cache event를 같은 transaction으로 적용해 동시 변경의 정합성을 지킵니다. */
    @Override
    @Transactional(transactionManager = CpfCommonPersistenceNames.TX_MANAGER_BEAN)
    public Map<String,Object> update(CpfCommonResource r, CpfCommonMutation mutation, String actor) {
        Objects.requireNonNull(r,"resource"); requireReason(mutation.reason()); String user=actor(actor);
        Map<String,Object> beforeRaw=getRaw(r,mutation.identifiers());
        Map<String,Object> before=safeProjection(r,beforeRaw);
        Map<String,Object> values=new LinkedHashMap<>(safeValues(r, mutation.values()));
        encodeParameterValueIfNeeded(r, values, beforeRaw);
        r.keyColumns().forEach(values::remove);
        if(values.isEmpty()) throw validation("update values are required");
        values.put("updated_by",user); values.put("updated_at",Timestamp.from(clock.instant()));
        List<Object> args=new ArrayList<>();
        String versionGuard=versionGuardAndBump(r,mutation.expectedVersion(),before,values,args);
        List<String> sets=new ArrayList<>(values.keySet());
        String setSql=sets.stream().map(c->c+"=?").collect(Collectors.joining(","));
        args.clear(); for(String c:sets) args.add(jdbcValue(values.get(c)));
        List<Object> keyArgs=new ArrayList<>(); String keyWhere=keyWhere(r,mutation.identifiers(),keyArgs);
        args.addAll(keyArgs);
        if(r.versionColumn()!=null && mutation.expectedVersion()!=null) args.add(mutation.expectedVersion());
        int changed;
        try { changed=jdbc.update("UPDATE "+r.table()+" SET "+setSql+" WHERE "+keyWhere+versionGuard,args.toArray()); }
        // Update constraint 충돌도 Vendor 예외를 노출하지 않고 동일한 Common Validation 계약으로 변환합니다.
        catch(DataIntegrityViolationException e){ throw validation("duplicate or invalid Common resource"); }
        if(changed!=1) throw validation("resource changed concurrently or not found");
        String auditKey=eventKey(r,mutation.identifiers(),values);
        invalidation.publishRequired(r.cacheName(),"UPSERT",auditKey,user);
        auditAfterCommit("UPDATE",r,auditKey,user,mutation.reason());
        return get(r,mutation.identifiers());
    }

    /** 논리/물리 삭제와 cache invalidation event를 원자적으로 기록해 조회 정합성을 보장합니다. */
    @Override
    @Transactional(transactionManager = CpfCommonPersistenceNames.TX_MANAGER_BEAN)
    public Map<String,Object> delete(CpfCommonResource r, CpfCommonMutation mutation, String actor) {
        Objects.requireNonNull(r,"resource"); requireReason(mutation.reason()); String user=actor(actor);
        Map<String,Object> before=get(r,mutation.identifiers());
        List<Object> args=new ArrayList<>(); String keyWhere=keyWhere(r,mutation.identifiers(),args);
        int changed;
        String deleteVersionGuard = versionGuardForDelete(r, mutation.expectedVersion(), before);
        if(r.activeColumn()!=null) {
            String bump = r.versionColumn()==null ? "" : ","+r.versionColumn()+"="+r.versionColumn()+"+1";
            String sql="UPDATE "+r.table()+" SET "+r.activeColumn()+"='N'"+bump+",updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE "+keyWhere+deleteVersionGuard;
            List<Object> all=new ArrayList<>(); all.add(user); all.addAll(args); if(!deleteVersionGuard.isEmpty()) all.add(mutation.expectedVersion()); changed=jdbc.update(sql,all.toArray());
        } else {
            List<Object> all=new ArrayList<>(args); if(!deleteVersionGuard.isEmpty()) all.add(mutation.expectedVersion());
            changed=jdbc.update("DELETE FROM "+r.table()+" WHERE "+keyWhere+deleteVersionGuard,all.toArray());
        }
        if(changed!=1) throw validation("resource changed concurrently or not found");
        String auditKey=eventKey(r,mutation.identifiers(),before);
        invalidation.publishRequired(r.cacheName(),"DELETE",auditKey,user);
        auditAfterCommit("DELETE",r,auditKey,user,mutation.reason());
        Map<String,Object> result=new LinkedHashMap<>();result.put("resource",r.name());result.put("identifiers",Collections.unmodifiableMap(new LinkedHashMap<>(mutation.identifiers())));result.put("deleted",true);return result;
    }

    private String whereForSearch(CpfCommonResource r,String query,boolean includeDisabled,Instant effectiveAt,List<Object> args){
        List<String> w=new ArrayList<>();
        if(query!=null&&!query.isBlank()){
            String q="%"+query.trim().toLowerCase(Locale.ROOT)+"%";
            w.add("("+r.searchableColumns().stream().map(c->"LOWER("+c+") LIKE ?").collect(Collectors.joining(" OR "))+")");
            for(int i=0;i<r.searchableColumns().size();i++)args.add(q);
        }
        if(!includeDisabled && r.activeColumn()!=null) w.add(r.activeColumn()+"='Y'");
        Instant at=effectiveAt==null?clock.instant():effectiveAt;
        if(r.effectiveFrom()!=null){w.add("("+r.effectiveFrom()+" IS NULL OR "+r.effectiveFrom()+"<=?)");args.add(Timestamp.from(at));}
        if(r.effectiveTo()!=null){w.add("("+r.effectiveTo()+" IS NULL OR "+r.effectiveTo()+">?)");args.add(Timestamp.from(at));}
        return w.isEmpty()?"":" WHERE "+String.join(" AND ",w);
    }

    private Map<String,Object> getRaw(CpfCommonResource r, Map<String,Object> identifiers) {
        List<Object> args=new ArrayList<>(); String where=keyWhere(r,identifiers,args);
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM "+r.table()+" WHERE "+where,args.toArray());
        if(rows.isEmpty()) throw new NoSuchElementException(r.name()+" not found");
        if(rows.size()!=1) throw new IllegalStateException("Common resource key is not unique");
        return rows.get(0);
    }
    private Map<String,Object> safeProjection(CpfCommonResource r, Map<String,Object> row) {
        Map<String,Object> safe=new LinkedHashMap<>(row);
        if(r==CpfCommonResource.PARAMETER && "Y".equalsIgnoreCase(String.valueOf(valueIgnoreCase(row,"encrypted_yn")))) {
            for(String key:new ArrayList<>(safe.keySet())) if(normalizeColumn(key).equals("config_value")) safe.put(key,"[MASKED]");
        }
        return Collections.unmodifiableMap(safe);
    }
    private void encodeParameterValueIfNeeded(CpfCommonResource r, Map<String,Object> values, Map<String,Object> current) {
        if(r!=CpfCommonResource.PARAMETER || valueIgnoreCase(values,"config_value")==null) return;
        Object flag=valueIgnoreCase(values,"encrypted_yn");
        if(flag==null && current!=null) flag=valueIgnoreCase(current,"encrypted_yn");
        if(!"Y".equalsIgnoreCase(String.valueOf(flag))) return;
        Object key=valueIgnoreCase(values,"config_key"); if(key==null && current!=null) key=valueIgnoreCase(current,"config_key");
        if(key==null) throw validation("config_key is required for encrypted parameter");
        CpfParameterValueCodec codec=parameterCodec.getIfUnique();
        if(codec==null) throw validation("encrypted parameter requires a single Security codec Provider");
        String encoded=codec.encode(String.valueOf(key),String.valueOf(valueIgnoreCase(values,"config_value")));
        for(String column:new ArrayList<>(values.keySet())) if(normalizeColumn(column).equals("config_value")) values.put(column,encoded);
    }

    private Map<String,Object> safeValues(CpfCommonResource r,Map<String,Object> raw){
        Map<String,Object> out=new LinkedHashMap<>(); if(raw==null)return out;
        for(var e:raw.entrySet()){
            String c=normalizeColumn(e.getKey()); if(!r.writableColumns().contains(c)) throw validation("column is not writable: "+c); out.put(c,e.getValue());
        }
        return out;
    }
    private String keyWhere(CpfCommonResource r,Map<String,Object> ids,List<Object> args){
        if(ids==null)throw validation("identifiers are required"); List<String>w=new ArrayList<>();
        for(String c:r.keyColumns()){Object v=valueIgnoreCase(ids,c);if(v==null)throw validation("missing identifier: "+c);w.add(c+"=?");args.add(jdbcValue(v));}
        return String.join(" AND ",w);
    }
    private String versionGuardAndBump(CpfCommonResource r,Long expected,Map<String,Object> before,Map<String,Object> values,List<Object> ignored){
        if(r.versionColumn()==null)return "";
        Object current=valueIgnoreCase(before,r.versionColumn());
        long actual=current instanceof Number n?n.longValue():Long.parseLong(String.valueOf(current));
        if(expected==null) throw validation("expectedVersion is required for versioned resource update");
        if(expected!=actual)throw validation("version conflict");
        values.put(r.versionColumn(),actual+1);
        return " AND "+r.versionColumn()+"=?";
    }

    private String versionGuardForDelete(CpfCommonResource r, Long expected, Map<String,Object> before) {
        if (r.versionColumn()==null) return "";
        Object current=valueIgnoreCase(before,r.versionColumn());
        long actual=current instanceof Number n?n.longValue():Long.parseLong(String.valueOf(current));
        if (expected==null) throw validation("expectedVersion is required for versioned resource delete");
        if (expected!=actual) throw validation("version conflict");
        return " AND "+r.versionColumn()+"=?";
    }
    private Map<String,Object> resolveKeysAfterCreate(CpfCommonResource r,Map<String,Object> explicit,Map<String,Object> values){
        Map<String,Object> keys=new LinkedHashMap<>();
        for(String k:r.keyColumns()){Object v=valueIgnoreCase(explicit,k);if(v==null)v=valueIgnoreCase(values,k);if(v!=null)keys.put(k,v);}
        return keys.size()==r.keyColumns().size()?keys:Map.of();
    }
    private String eventKey(CpfCommonResource r,Map<String,Object> keys,Map<String,Object> values){
        List<String> parts=new ArrayList<>(); for(String k:r.keyColumns()){Object v=valueIgnoreCase(keys,k);if(v==null)v=valueIgnoreCase(values,k);parts.add(k+"="+(v==null?"?":String.valueOf(v)));} return String.join(",",parts);
    }
    private void auditAfterCommit(String action, CpfCommonResource resource, String key, String actor, String reason) {
        Runnable work=()->audit.record(action,resource.name(),key,actor,reason,Map.of());
        if(TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
                @Override public void afterCommit(){work.run();}
            });
        } else work.run();
    }

    private void addAuditForInsert(Map<String,Object> v,String actor){Instant now=clock.instant();v.put("created_by",actor);v.put("created_at",Timestamp.from(now));v.put("updated_by",actor);v.put("updated_at",Timestamp.from(now));}
    private static Object valueIgnoreCase(Map<String,Object> map,String key){if(map==null)return null;for(var e:map.entrySet())if(normalizeColumn(e.getKey()).equals(key))return e.getValue();return null;}
    private static String normalizeColumn(String c){if(c==null||c.isBlank())throw validation("blank column");String n=c.trim().toLowerCase(Locale.ROOT);if(!n.matches("[a-z0-9_]+"))throw validation("invalid column");return n;}
    private static Object jdbcValue(Object value){if(value instanceof Instant i)return Timestamp.from(i);return value;}
    private static String actor(String a){if(a==null||a.isBlank())throw validation("actor is required");return a.trim();}
    private static void requireReason(String r){if(r==null||r.isBlank())throw validation("reason is required for Common management mutation");}
    private static CpfValidationException validation(String message){return new CpfValidationException(message);}
}
