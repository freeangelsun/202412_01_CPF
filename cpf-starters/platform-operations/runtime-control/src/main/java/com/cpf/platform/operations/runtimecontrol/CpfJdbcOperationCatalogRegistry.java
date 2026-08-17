package com.cpf.platform.operations.runtimecontrol;

import com.cpf.foundation.execution.api.CpfOperationCatalogRegistry;
import java.time.Clock;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/** Catalog metadata와 ADM-owned policy를 분리해 최초 Seed만 적용하는 JDBC registry입니다. */
public final class CpfJdbcOperationCatalogRegistry implements CpfOperationCatalogRegistry {
    private final JdbcTemplate jdbc; private final TransactionTemplate tx; private final Clock clock;
    private final Set<String> defaultAllowedCallers; private final String seedSource; private final String seedRevision;
    public CpfJdbcOperationCatalogRegistry(JdbcTemplate jdbc, TransactionTemplate tx, Clock clock, List<String> defaultAllowedCallers,
            String seedSource, String seedRevision){
        this.jdbc=Objects.requireNonNull(jdbc);this.tx=Objects.requireNonNull(tx);this.clock=Objects.requireNonNull(clock);
        LinkedHashSet<String> c=new LinkedHashSet<>(); if(defaultAllowedCallers!=null) for(String v:defaultAllowedCallers) if(v!=null&&!v.isBlank()) c.add(v.trim().toUpperCase(Locale.ROOT));
        this.defaultAllowedCallers=Set.copyOf(c);this.seedSource=text(seedSource,"YML");this.seedRevision=text(seedRevision,"UNSPECIFIED");
    }
    @Override public SyncResult synchronize(SyncRequest request){return tx.execute(status->sync(request));}
    private SyncResult sync(SyncRequest r){
        Timestamp now=Timestamp.from(clock.instant()); ensureSystem(r,now); int inserted=0,updated=0,seeded=0;
        for(Operation o:r.operations()){
            Integer exists=jdbc.queryForObject("SELECT COUNT(*) FROM cpf_operation_catalog WHERE operation_id=?",Integer.class,o.operationId());
            boolean fresh=exists==null||exists==0;
            if(fresh){jdbc.update("INSERT INTO cpf_operation_catalog(operation_id,operation_name,description,system_code,domain_code,application_code,http_method,api_path,controller_class,handler_method,source_fingerprint,discovery_status,first_seen_at,last_seen_at,last_instance_id,metadata_version) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,1)",o.operationId(),o.name(),o.description(),o.systemCode(),o.domainCode(),o.application(),o.httpMethod(),o.apiPath(),o.controllerClass(),o.handlerMethod(),o.sourceFingerprint(),"ACTIVE",now,now,r.instanceId());inserted++;}
            else{jdbc.update("UPDATE cpf_operation_catalog SET operation_name=?,description=?,system_code=?,domain_code=?,application_code=?,http_method=?,api_path=?,controller_class=?,handler_method=?,source_fingerprint=?,discovery_status='ACTIVE',last_seen_at=?,last_instance_id=?,metadata_version=metadata_version+1 WHERE operation_id=?",o.name(),o.description(),o.systemCode(),o.domainCode(),o.application(),o.httpMethod(),o.apiPath(),o.controllerClass(),o.handlerMethod(),o.sourceFingerprint(),now,r.instanceId(),o.operationId());updated++;}
            if(fresh) seeded+=seedPolicy(o.operationId(),now);
        }
        return new SyncResult(r.operations().size(),inserted,updated,seeded);
    }
    private void ensureSystem(SyncRequest r,Timestamp now){Integer n=jdbc.queryForObject("SELECT COUNT(*) FROM cpf_system_registry WHERE system_code=?",Integer.class,r.systemCode());
        if(n==null||n==0) jdbc.update("INSERT INTO cpf_system_registry(system_code,system_name,domain_code,enabled_yn,description,first_seen_at,last_seen_at,last_instance_id) VALUES (?,?,?,?,?,?,?,?)",r.systemCode(),r.systemCode(),r.domainCode(),"Y","Runtime discovered system",now,now,r.instanceId());
        else jdbc.update("UPDATE cpf_system_registry SET domain_code=?,last_seen_at=?,last_instance_id=? WHERE system_code=?",r.domainCode(),now,r.instanceId(),r.systemCode());}
    private int seedPolicy(String op,Timestamp now){boolean all=defaultAllowedCallers.contains("ALL");
        jdbc.update("INSERT INTO cpf_operation_policy(operation_id,enabled_yn,all_callers_yn,policy_version,seed_source,seed_revision,seeded_at,updated_at) VALUES (?,?,?,?,?,?,?,?)",op,"Y",all?"Y":"N",1L,seedSource,seedRevision,now,now);
        int rows=1; if(!all) for(String caller:defaultAllowedCallers){jdbc.update("INSERT INTO cpf_operation_caller_policy(operation_id,caller_system_code,allowed_yn,policy_version,seed_source,seed_revision,seeded_at,updated_at) VALUES (?,?,?,?,?,?,?,?)",op,caller,"Y",1L,seedSource,seedRevision,now,now);rows++;} return rows;}
    private static String text(String v,String d){return v==null||v.isBlank()?d:v.trim();}
}
