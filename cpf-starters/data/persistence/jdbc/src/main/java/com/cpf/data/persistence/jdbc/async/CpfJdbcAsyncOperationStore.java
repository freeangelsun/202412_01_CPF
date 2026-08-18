package com.cpf.data.persistence.jdbc.async;

import com.cpf.core.api.async.CpfAsyncState;
import com.cpf.starter.async.operation.CpfAsyncOperationStore;
import com.cpf.starter.async.operation.CpfAsyncStoredOperation;
import java.sql.*; import java.time.*; import java.util.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

/** Oracle/PostgreSQL/MariaDB 공통 SQL만 사용하는 durable Async Operation Store입니다. */
public final class CpfJdbcAsyncOperationStore implements CpfAsyncOperationStore {
 private static final String COLS="execution_id,operation_id,transaction_id,idempotency_key,command_type,command_payload,context_payload,result_type,result_payload,state,result_status,error_code,error_message,recovery_id,recovery_action,submitted_at,started_at,updated_at,completed_at,expires_at,heartbeat_at,lease_owner,lease_until,cancellation_reason,version";
 private final JdbcTemplate jdbc;
 public CpfJdbcAsyncOperationStore(JdbcTemplate jdbc){this.jdbc=Objects.requireNonNull(jdbc,"jdbc");}
 @Override public CpfAsyncStoredOperation insertOrGet(CpfAsyncStoredOperation op){
  Optional<CpfAsyncStoredOperation> existing=findByIdempotency(op.operationId(),op.idempotencyKey());if(existing.isPresent())return existing.get();
  try{jdbc.update("INSERT INTO OPS_ASYNC_OPERATION ("+COLS+") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",op.executionId(),op.operationId(),op.transactionId(),op.idempotencyKey(),op.commandType(),op.commandPayload(),op.contextPayload(),op.resultType(),op.resultPayload(),op.state().name(),op.resultStatus(),op.errorCode(),op.errorMessage(),op.recoveryId(),op.recoveryAction(),ts(op.submittedAt()),ts(op.startedAt()),ts(op.updatedAt()),ts(op.completedAt()),ts(op.expiresAt()),ts(op.heartbeatAt()),op.leaseOwner(),ts(op.leaseUntil()),op.cancellationReason(),op.version());return op;}catch(DuplicateKeyException race){return findByIdempotency(op.operationId(),op.idempotencyKey()).orElseThrow(()->race);}
 }
 @Override public Optional<CpfAsyncStoredOperation> find(String executionId){return one("SELECT "+COLS+" FROM OPS_ASYNC_OPERATION WHERE execution_id=?",executionId);}
 private Optional<CpfAsyncStoredOperation> findByIdempotency(String operationId,String key){return one("SELECT "+COLS+" FROM OPS_ASYNC_OPERATION WHERE operation_id=? AND idempotency_key=?",operationId,key);}
 @Override public Optional<CpfAsyncStoredOperation> claimNext(String owner,Instant now,Instant leaseUntil){
  List<CpfAsyncStoredOperation> rows=jdbc.query(con->{PreparedStatement ps=con.prepareStatement("SELECT "+COLS+" FROM OPS_ASYNC_OPERATION WHERE (state='ACCEPTED' OR (state='RUNNING' AND lease_until<?)) AND expires_at>? ORDER BY submitted_at");ps.setTimestamp(1,ts(now));ps.setTimestamp(2,ts(now));ps.setMaxRows(32);return ps;},(rs,n)->map(rs));
  for(CpfAsyncStoredOperation row:rows){int updated=jdbc.update("UPDATE OPS_ASYNC_OPERATION SET state='RUNNING',started_at=COALESCE(started_at,?),updated_at=?,heartbeat_at=?,lease_owner=?,lease_until=?,version=version+1 WHERE execution_id=? AND version=? AND (state='ACCEPTED' OR (state='RUNNING' AND lease_until<?)) AND expires_at>?",ts(now),ts(now),ts(now),owner,ts(leaseUntil),row.executionId(),row.version(),ts(now),ts(now));if(updated==1)return find(row.executionId());}return Optional.empty();
 }
 @Override public boolean heartbeat(String executionId,String owner,long expectedVersion,Instant now,Instant leaseUntil){return jdbc.update("UPDATE OPS_ASYNC_OPERATION SET heartbeat_at=?,updated_at=?,lease_until=? WHERE execution_id=? AND lease_owner=? AND version=? AND state IN ('RUNNING','CANCEL_REQUESTED')",ts(now),ts(now),ts(leaseUntil),executionId,owner,expectedVersion)==1;}
 @Override public CpfAsyncStoredOperation requestCancel(String executionId,String reason,Instant now){
  CpfAsyncStoredOperation current=find(executionId).orElseThrow(()->new NoSuchElementException("Async execution not found: "+executionId));if(current.state().terminal())return current;
  if(current.state()==CpfAsyncState.ACCEPTED){jdbc.update("UPDATE OPS_ASYNC_OPERATION SET state='CANCELLED',cancellation_reason=?,updated_at=?,completed_at=?,version=version+1 WHERE execution_id=? AND version=? AND state='ACCEPTED'",reason,ts(now),ts(now),executionId,current.version());}
  else{jdbc.update("UPDATE OPS_ASYNC_OPERATION SET state='CANCEL_REQUESTED',cancellation_reason=?,updated_at=? WHERE execution_id=? AND version=? AND state='RUNNING'",reason,ts(now),executionId,current.version());}
  return find(executionId).orElseThrow();
 }
 @Override public boolean cancellationRequested(String executionId){String s=jdbc.queryForObject("SELECT state FROM OPS_ASYNC_OPERATION WHERE execution_id=?",String.class,executionId);return "CANCEL_REQUESTED".equals(s);}
 @Override public CpfAsyncStoredOperation complete(String executionId,String owner,long expectedVersion,String resultStatus,String resultType,String resultPayload,String errorCode,String errorMessage,String recoveryId,String recoveryAction,Instant now){
  CpfAsyncState terminal=switch(resultStatus==null?"":resultStatus){case "SUCCESS"->CpfAsyncState.SUCCEEDED;case "UNKNOWN"->CpfAsyncState.UNKNOWN;case "CANCELLED"->CpfAsyncState.CANCELLED;default->CpfAsyncState.FAILED;};
  int updated=jdbc.update("UPDATE OPS_ASYNC_OPERATION SET state=?,result_status=?,result_type=?,result_payload=?,error_code=?,error_message=?,recovery_id=?,recovery_action=?,updated_at=?,completed_at=?,lease_until=NULL,version=version+1 WHERE execution_id=? AND lease_owner=? AND version=? AND state IN ('RUNNING','CANCEL_REQUESTED')",terminal.name(),resultStatus,resultType,resultPayload,errorCode,errorMessage,recoveryId,recoveryAction,ts(now),ts(now),executionId,owner,expectedVersion);
  if(updated!=1)throw new IllegalStateException("Async completion fencing conflict: "+executionId);return find(executionId).orElseThrow();
 }
 @Override public int expireDue(Instant now){
  int accepted=jdbc.update("UPDATE OPS_ASYNC_OPERATION SET state='EXPIRED',error_code='CPF-ASYNC-EXPIRED',error_message='Async execution expired before start',updated_at=?,completed_at=?,version=version+1 WHERE state='ACCEPTED' AND expires_at<=?",ts(now),ts(now),ts(now));
  jdbc.update("UPDATE OPS_ASYNC_OPERATION SET state='CANCEL_REQUESTED',cancellation_reason='EXPIRED',updated_at=? WHERE state='RUNNING' AND expires_at<=?",ts(now),ts(now));
  int unknown=jdbc.update("UPDATE OPS_ASYNC_OPERATION SET state='UNKNOWN',result_status='UNKNOWN',error_code='CPF-ASYNC-LEASE-EXPIRED',error_message='Worker lease expired after execution deadline',recovery_id=execution_id,recovery_action='PROBE_OR_RECONCILE',updated_at=?,completed_at=?,version=version+1 WHERE state='CANCEL_REQUESTED' AND expires_at<=? AND lease_until<?",ts(now),ts(now),ts(now),ts(now));
  return accepted+unknown;
 }
 private Optional<CpfAsyncStoredOperation> one(String sql,Object... args){List<CpfAsyncStoredOperation> rows=jdbc.query(sql,(rs,n)->map(rs),args);return rows.stream().findFirst();}
 private CpfAsyncStoredOperation map(ResultSet rs)throws SQLException{return new CpfAsyncStoredOperation(rs.getString("execution_id"),rs.getString("operation_id"),rs.getString("transaction_id"),rs.getString("idempotency_key"),rs.getString("command_type"),rs.getString("command_payload"),rs.getString("context_payload"),rs.getString("result_type"),rs.getString("result_payload"),CpfAsyncState.valueOf(rs.getString("state")),rs.getString("result_status"),rs.getString("error_code"),rs.getString("error_message"),rs.getString("recovery_id"),rs.getString("recovery_action"),instant(rs,"submitted_at"),instant(rs,"started_at"),instant(rs,"updated_at"),instant(rs,"completed_at"),instant(rs,"expires_at"),instant(rs,"heartbeat_at"),rs.getString("lease_owner"),instant(rs,"lease_until"),rs.getString("cancellation_reason"),rs.getLong("version"));}
 private static Timestamp ts(Instant i){return i==null?null:Timestamp.from(i);} private static Instant instant(ResultSet rs,String c)throws SQLException{Timestamp t=rs.getTimestamp(c);return t==null?null:t.toInstant();}
}
