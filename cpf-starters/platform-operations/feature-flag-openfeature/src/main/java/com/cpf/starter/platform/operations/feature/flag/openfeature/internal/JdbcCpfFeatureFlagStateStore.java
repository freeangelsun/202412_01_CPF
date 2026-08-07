package com.cpf.starter.platform.operations.feature.flag.openfeature.internal;

import com.cpf.core.api.featureflag.*;
import com.cpf.core.spi.featureflag.CpfFeatureFlagStateStore;
import java.time.Instant;
import java.util.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/** Shared JDBC state with one active override per flag and atomic two-person approval. */
public final class JdbcCpfFeatureFlagStateStore implements CpfFeatureFlagStateStore {
 private final JdbcTemplate jdbc;private final TransactionTemplate tx;
 public JdbcCpfFeatureFlagStateStore(JdbcTemplate jdbc,TransactionTemplate tx){this.jdbc=jdbc;this.tx=tx;}
 @Override public Optional<CpfFeatureFlagResult<CpfFeatureFlagValue>> findEffective(String key,Instant now){
  var kills=jdbc.query("select revision from cpf_feature_flag_kill_switch where flag_key=? and enabled_flag='Y'",(r,n)->r.getLong(1),key);
  if(!kills.isEmpty())return Optional.of(new CpfFeatureFlagResult<>(key,new CpfFeatureFlagValue.BooleanValue(false),"KILL_SWITCH","KILL_SWITCH",CpfFeatureFlagResult.Source.KILL_SWITCH,kills.getFirst(),now));
  var rows=jdbc.query("select value_type,value_text,revision from cpf_feature_flag_override where flag_key=? and override_status='ACTIVE' and expires_at>?",(r,n)->new CpfFeatureFlagResult<>(key,decode(r.getString(1),r.getString(2)),"OVERRIDE","APPROVED_OVERRIDE",CpfFeatureFlagResult.Source.SECURE_OVERRIDE,r.getLong(3),now),key,java.sql.Timestamp.from(now));
  return rows.stream().findFirst();
 }
 @Override public List<CpfFeatureFlagResult<CpfFeatureFlagValue>> search(String filter,int offset,int limit,Instant now){String f="%"+(filter==null?"":filter.trim())+"%";int start=offset;int end=Math.addExact(offset,limit);String sql="select flag_key,value_type,value_text,revision from (select flag_key,value_type,value_text,revision,row_number() over(order by flag_key) cpf_rn from cpf_feature_flag_override where override_status='ACTIVE' and expires_at>? and flag_key like ?) cpf_page where cpf_rn>? and cpf_rn<=? order by cpf_rn";return jdbc.query(sql,(r,n)->new CpfFeatureFlagResult<>(r.getString(1),decode(r.getString(2),r.getString(3)),"OVERRIDE","APPROVED_OVERRIDE",CpfFeatureFlagResult.Source.SECURE_OVERRIDE,r.getLong(4),now),java.sql.Timestamp.from(now),f,start,end);}
 @Override public String requestOverride(String key,CpfFeatureFlagValue value,Instant expires,String requester,String reason){
  String id=UUID.randomUUID().toString();String valueType=type(value);String valueText=encode(value);
  try{jdbc.update("insert into cpf_feature_flag_override_request(request_id,flag_key,value_type,value_text,expires_at,requester_id,request_reason,request_status,active_flag_key) values(?,?,?,?,?,?,?,'PENDING',?)",id,key,valueType,valueText,java.sql.Timestamp.from(expires),requester,reason,key);return id;}
  catch(DuplicateKeyException e){
   var existing=jdbc.query("select request_id,value_type,value_text,expires_at,requester_id,request_reason from cpf_feature_flag_override_request where active_flag_key=? and request_status='PENDING'",(r,n)->new Pending(r.getString(1),r.getString(2),r.getString(3),r.getTimestamp(4).toInstant(),r.getString(5),r.getString(6)),key);
   if(existing.size()==1){var p=existing.getFirst();if(p.type.equals(valueType)&&p.value.equals(valueText)&&p.expires.equals(expires)&&p.requester.equals(requester)&&p.reason.equals(reason))return p.id;}
   throw new IllegalStateException("conflicting pending override already exists: "+key,e);
  }
 }
 @Override public CpfFeatureFlagResult<CpfFeatureFlagValue> approveOverride(String id,String approver,String reason,Instant now){return tx.execute(st->{
  var rows=jdbc.query("select flag_key,value_type,value_text,expires_at,requester_id,request_status,approver_id from cpf_feature_flag_override_request where request_id=? for update",(r,n)->new Req(r.getString(1),r.getString(2),r.getString(3),r.getTimestamp(4).toInstant(),r.getString(5),r.getString(6),r.getString(7)),id);
  if(rows.size()!=1)throw new IllegalArgumentException("override request not found");var q=rows.getFirst();
  if("APPROVED".equals(q.status)){if(!Objects.equals(q.approver,approver))throw new IllegalStateException("override already approved by a different operator");return approvedResult(id,q,now);}
  if(!"PENDING".equals(q.status))throw new IllegalStateException("override request is not pending: "+q.status);
  if(q.requester.equals(approver))throw new IllegalArgumentException("self approval is forbidden");if(!q.expires.isAfter(now))throw new IllegalArgumentException("override request expired");
  long rev=nextRevision();jdbc.update("update cpf_feature_flag_override set override_status='SUPERSEDED',active_flag_key=null where flag_key=? and override_status='ACTIVE'",q.key);jdbc.update("insert into cpf_feature_flag_override(override_id,flag_key,value_type,value_text,expires_at,override_status,revision,active_flag_key) values(?,?,?,?,?,'ACTIVE',?,?)",id,q.key,q.type,q.value,java.sql.Timestamp.from(q.expires),rev,q.key);int n=jdbc.update("update cpf_feature_flag_override_request set request_status='APPROVED',approver_id=?,approval_reason=?,active_flag_key=null where request_id=? and request_status='PENDING'",approver,reason,id);if(n!=1)throw new IllegalStateException("approval conflict");bumpRevision(rev);return new CpfFeatureFlagResult<>(q.key,decode(q.type,q.value),"OVERRIDE","APPROVED_OVERRIDE",CpfFeatureFlagResult.Source.SECURE_OVERRIDE,rev,now);});}
 @Override public void revokeOverride(String id,String operator,String reason,Instant now){tx.executeWithoutResult(st->{var rows=jdbc.query("select override_status from cpf_feature_flag_override where override_id=? for update",(r,n)->r.getString(1),id);if(rows.size()!=1)throw new IllegalArgumentException("override not found");String status=rows.getFirst();if("REVOKED".equals(status))return;if(!"ACTIVE".equals(status))throw new IllegalStateException("override is not active: "+status);long rev=nextRevision();int n=jdbc.update("update cpf_feature_flag_override set override_status='REVOKED',active_flag_key=null,revision=? where override_id=? and override_status='ACTIVE'",rev,id);if(n!=1)throw new IllegalStateException("override revoke conflict");bumpRevision(rev);});}
 @Override public void setKillSwitch(String key,boolean enabled,String operator,String reason,Instant now){tx.executeWithoutResult(st->{lockRevision();var rows=jdbc.query("select enabled_flag from cpf_feature_flag_kill_switch where flag_key=? for update",(r,n)->r.getString(1),key);String desired=enabled?"Y":"N";if(rows.size()==1&&desired.equals(rows.getFirst()))return;long rev=nextRevision();if(rows.isEmpty())jdbc.update("insert into cpf_feature_flag_kill_switch(flag_key,enabled_flag,revision,updated_by,updated_at) values(?,?,?,?,?)",key,desired,rev,operator,java.sql.Timestamp.from(now));else{int n=jdbc.update("update cpf_feature_flag_kill_switch set enabled_flag=?,revision=?,updated_by=?,updated_at=? where flag_key=?",desired,rev,operator,java.sql.Timestamp.from(now),key);if(n!=1)throw new IllegalStateException("kill switch update conflict");}bumpRevision(rev);});}
 private CpfFeatureFlagResult<CpfFeatureFlagValue> approvedResult(String id,Req q,Instant now){var rows=jdbc.query("select revision from cpf_feature_flag_override where override_id=?",(r,n)->r.getLong(1),id);if(rows.size()!=1)throw new IllegalStateException("approved override state is missing: "+id);return new CpfFeatureFlagResult<>(q.key,decode(q.type,q.value),"OVERRIDE","APPROVED_OVERRIDE",CpfFeatureFlagResult.Source.SECURE_OVERRIDE,rows.getFirst(),now);}
 private void lockRevision(){Long v=jdbc.queryForObject("select revision from cpf_feature_flag_revision where singleton_id=1 for update",Long.class);if(v==null)throw new IllegalStateException("feature flag revision row is missing");}
 @Override public long revision(){Long v=jdbc.queryForObject("select revision from cpf_feature_flag_revision where singleton_id=1",Long.class);return v==null?0:v;}
 private long nextRevision(){int n=jdbc.update("update cpf_feature_flag_revision set revision=revision+1 where singleton_id=1");if(n!=1)throw new IllegalStateException("feature flag revision row is missing");return revision();}
 private void bumpRevision(long rev){long actual=revision();if(actual<rev)throw new IllegalStateException("feature flag revision update lost: expected at least "+rev+", actual "+actual);}
 private static String type(CpfFeatureFlagValue v){return v instanceof CpfFeatureFlagValue.BooleanValue?"BOOLEAN":v instanceof CpfFeatureFlagValue.StringValue?"STRING":v instanceof CpfFeatureFlagValue.IntegerValue?"INTEGER":"DECIMAL";}
 private static String encode(CpfFeatureFlagValue v){return String.valueOf(v.rawValue());}
 private static CpfFeatureFlagValue decode(String t,String v){return switch(t){case"BOOLEAN"->new CpfFeatureFlagValue.BooleanValue(Boolean.parseBoolean(v));case"INTEGER"->new CpfFeatureFlagValue.IntegerValue(Long.parseLong(v));case"DECIMAL"->new CpfFeatureFlagValue.DecimalValue(Double.parseDouble(v));default->new CpfFeatureFlagValue.StringValue(v);};}
 private record Req(String key,String type,String value,Instant expires,String requester,String status,String approver){}
 private record Pending(String id,String type,String value,Instant expires,String requester,String reason){}
}
