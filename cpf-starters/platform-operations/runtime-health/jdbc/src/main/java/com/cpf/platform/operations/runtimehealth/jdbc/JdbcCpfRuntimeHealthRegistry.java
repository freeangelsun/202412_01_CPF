package com.cpf.platform.operations.runtimehealth.jdbc;

import com.cpf.platform.operations.api.health.CpfRuntimeHealth;
import com.cpf.platform.operations.api.health.CpfRuntimeHealthRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;

/** DB-backed canonical Runtime Health registry. observedAt 기준으로 stale overwrite를 방지합니다. */
public final class JdbcCpfRuntimeHealthRegistry implements CpfRuntimeHealthRegistry {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    public JdbcCpfRuntimeHealthRegistry(JdbcTemplate jdbc,ObjectMapper json){this.jdbc=Objects.requireNonNull(jdbc);this.json=Objects.requireNonNull(json);}

    @Override public void upsert(CpfRuntimeHealth h){
        Objects.requireNonNull(h,"health");
        Optional<CpfRuntimeHealth> current=find(h.systemId(),h.instanceId());
        if(current.isPresent() && current.get().observedAt().isAfter(h.observedAt())) return;
        String payload=encode(h);
        int n=jdbc.update("update CPF_RUNTIME_INSTANCE_HEALTH set VERSION=?,BUILD_SHA=?,STARTED_AT=?,UPTIME_MS=?,LAST_SEEN_AT=?,LIVENESS=?,READINESS=?,STARTUP=?,DRAINING=?,MAINTENANCE=?,PAYLOAD_JSON=? where SYSTEM_ID=? and INSTANCE_ID=?",
                h.version(),h.buildSha(),h.startedAt(),h.uptimeMillis(),h.observedAt(),h.liveness().name(),h.readiness().name(),h.startup().name(),h.draining()?1:0,h.maintenance()?1:0,payload,h.systemId(),h.instanceId());
        if(n==0) jdbc.update("insert into CPF_RUNTIME_INSTANCE_HEALTH(SYSTEM_ID,INSTANCE_ID,VERSION,BUILD_SHA,STARTED_AT,UPTIME_MS,LAST_SEEN_AT,LIVENESS,READINESS,STARTUP,DRAINING,MAINTENANCE,PAYLOAD_JSON) values(?,?,?,?,?,?,?,?,?,?,?,?,?)",
                h.systemId(),h.instanceId(),h.version(),h.buildSha(),h.startedAt(),h.uptimeMillis(),h.observedAt(),h.liveness().name(),h.readiness().name(),h.startup().name(),h.draining()?1:0,h.maintenance()?1:0,payload);
    }

    @Override public Optional<CpfRuntimeHealth> find(String systemId,String instanceId){
        return jdbc.query("select PAYLOAD_JSON from CPF_RUNTIME_INSTANCE_HEALTH where SYSTEM_ID=? and INSTANCE_ID=?",
                (rs,n)->decode(rs.getString(1)),systemId,instanceId).stream().findFirst();
    }

    @Override public List<CpfRuntimeHealth> list(){
        return List.copyOf(jdbc.query("select PAYLOAD_JSON from CPF_RUNTIME_INSTANCE_HEALTH order by SYSTEM_ID,INSTANCE_ID",
                (rs,n)->decode(rs.getString(1))));
    }

    private String encode(CpfRuntimeHealth h){try{return json.writeValueAsString(h);}catch(Exception e){throw new IllegalStateException("health JSON serialization failed",e);}}
    private CpfRuntimeHealth decode(String p){try{return json.readValue(p,CpfRuntimeHealth.class);}catch(Exception e){throw new IllegalStateException("health JSON decode failed",e);}}
}
