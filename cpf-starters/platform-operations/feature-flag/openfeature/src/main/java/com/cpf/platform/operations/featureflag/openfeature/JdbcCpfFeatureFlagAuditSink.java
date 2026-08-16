package com.cpf.platform.operations.featureflag.openfeature;
import com.cpf.platform.operations.spi.featureflag.CpfFeatureFlagAuditSink;import java.time.Instant;import java.util.*;import org.springframework.jdbc.core.JdbcTemplate;
/** Persistent audit that never stores raw flag values or sensitive context. */
public final class JdbcCpfFeatureFlagAuditSink implements CpfFeatureFlagAuditSink{
 private final JdbcTemplate jdbc;public JdbcCpfFeatureFlagAuditSink(JdbcTemplate j){jdbc=j;}
 public void record(String event,String key,String actor,String reason,Map<String,String>a,Instant at){var s=new TreeMap<String,String>();if(a!=null)a.forEach((k,v)->{String x=k==null?"":k.toLowerCase(Locale.ROOT);if(!(x.contains("token")||x.contains("password")||x.contains("secret")||x.contains("value")))s.put(k,v==null?null:v.substring(0,Math.min(512,v.length())));});jdbc.update("insert into cpf_feature_flag_audit(audit_id,event_type,flag_key,actor_id,reason_code,sanitized_attributes,occurred_at) values(?,?,?,?,?,?,?)",UUID.randomUUID().toString(),event,key,actor,reason,s.toString(),java.sql.Timestamp.from(at));}
}
