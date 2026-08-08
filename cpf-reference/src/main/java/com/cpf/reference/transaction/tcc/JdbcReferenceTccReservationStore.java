package com.cpf.reference.transaction.tcc;

import java.sql.ResultSet;import java.sql.SQLException;import java.sql.Timestamp;import java.time.Instant;import java.util.List;import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.transaction.annotation.Transactional;

/** DB3-neutral JDBC store; SQL avoids vendor-specific paging and uses setMaxRows. */
public final class JdbcReferenceTccReservationStore implements ReferenceTccReservationStore {
 private static final String COLS="transaction_id,branch_id,idempotency_key,account_id,amount,state,deadline_at,fencing_token,updated_at";
 private final JdbcTemplate jdbc;
 public JdbcReferenceTccReservationStore(JdbcTemplate jdbc){this.jdbc=java.util.Objects.requireNonNull(jdbc);}
 @Override public Optional<ReferenceTccReservationRecord> find(String tx,String branch){List<ReferenceTccReservationRecord> r=jdbc.query("SELECT "+COLS+" FROM cpf_ref_tcc_reservation WHERE transaction_id=? AND branch_id=?",JdbcReferenceTccReservationStore::map,tx,branch);return r.stream().findFirst();}
 @Override @Transactional public boolean createTry(ReferenceTccReservationRecord r){return insert(r,ReferenceTccReservationState.TRYING);}
 @Override @Transactional public boolean createEmptyRollback(ReferenceTccReservationRecord r){return insert(r,ReferenceTccReservationState.CANCELED);}
 private boolean insert(ReferenceTccReservationRecord r,ReferenceTccReservationState state){try{return jdbc.update("INSERT INTO cpf_ref_tcc_reservation(transaction_id,branch_id,idempotency_key,account_id,amount,state,deadline_at,fencing_token,updated_at) VALUES(?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",r.transactionId(),r.branchId(),r.idempotencyKey(),r.accountId(),r.amount(),state.name(),Timestamp.from(r.deadline()),0L)==1;}catch(org.springframework.dao.DuplicateKeyException duplicate){return false;}}
 @Override @Transactional public boolean transition(String tx,String branch,ReferenceTccReservationState expected,ReferenceTccReservationState next,long fence){return jdbc.update("UPDATE cpf_ref_tcc_reservation SET state=?,fencing_token=fencing_token+1,updated_at=CURRENT_TIMESTAMP WHERE transaction_id=? AND branch_id=? AND state=? AND fencing_token=?",next.name(),tx,branch,expected.name(),fence)==1;}
 @Override @Transactional public boolean markManualReview(String tx,String branch,String reason){return jdbc.update("UPDATE cpf_ref_tcc_reservation SET state='MANUAL_REVIEW',review_reason=?,fencing_token=fencing_token+1,updated_at=CURRENT_TIMESTAMP WHERE transaction_id=? AND branch_id=?",safe(reason),tx,branch)==1;}
 @Override public List<ReferenceTccReservationRecord> findExpired(int limit){if(limit<1||limit>1000)throw new IllegalArgumentException("limit 1..1000");return jdbc.query(c->{var ps=c.prepareStatement("SELECT "+COLS+" FROM cpf_ref_tcc_reservation WHERE state IN ('TRYING','TRIED','UNKNOWN') AND deadline_at<CURRENT_TIMESTAMP ORDER BY updated_at");ps.setMaxRows(limit);return ps;},JdbcReferenceTccReservationStore::map);}
 private static ReferenceTccReservationRecord map(ResultSet rs,int n)throws SQLException{return new ReferenceTccReservationRecord(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getBigDecimal(5),ReferenceTccReservationState.valueOf(rs.getString(6)),rs.getTimestamp(7).toInstant(),rs.getLong(8),rs.getTimestamp(9).toInstant());}
 private static String safe(String s){if(s==null)return null;return s.substring(0,Math.min(500,s.length()));}
}
