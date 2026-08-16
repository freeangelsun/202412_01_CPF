package com.cpf.integration.resilience.internal;

import com.cpf.integration.resilience.spi.CpfResilienceAuditSink;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

/** Persistent sanitized audit; values are bounded and secrets are rejected. */
public final class JdbcCpfResilienceAuditSink implements CpfResilienceAuditSink {
    private final JdbcTemplate jdbc;
    public JdbcCpfResilienceAuditSink(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @Override public void record(String eventType,String operationId,String actorId,String reason,Map<String,String> attrs,Instant at){
        var sanitized=new TreeMap<String,String>();
        if(attrs!=null)attrs.forEach((k,v)->{String key=k==null?"":k.toLowerCase(java.util.Locale.ROOT);if(!(key.contains("password")||key.contains("secret")||key.contains("token")||key.contains("authorization")))sanitized.put(k,truncate(v,512));});
        jdbc.update("insert into cpf_resilience_audit(audit_id,event_type,operation_id,actor_id,reason_code,sanitized_attributes,occurred_at) values(?,?,?,?,?,?,?)",UUID.randomUUID().toString(),eventType,operationId,actorId,truncate(reason,256),sanitized.toString(),java.sql.Timestamp.from(at));
    }
    private static String truncate(String v,int max){return v==null?null:v.substring(0,Math.min(v.length(),max));}
}
