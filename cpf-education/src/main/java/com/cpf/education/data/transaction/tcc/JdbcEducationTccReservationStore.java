package com.cpf.education.data.transaction.tcc;
import java.sql.ResultSet;import java.sql.SQLException;import java.sql.Timestamp;import java.time.Instant;import java.util.List;import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;import com.cpf.data.persistence.api.annotation.CpfTx;

/** DB3-neutral JDBC store; SQL avoids vendor-specific paging and uses setMaxRows. */
/** JdbcEducationTccReservationStore 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class JdbcEducationTccReservationStore implements EducationTccReservationStore {
 private static final String COLS="transaction_id,branch_id,idempotency_key,account_id,amount,state,deadline_at,fencing_token,updated_at";
 private final JdbcTemplate jdbc;
 public JdbcEducationTccReservationStore(JdbcTemplate jdbc){this.jdbc=java.util.Objects.requireNonNull(jdbc);}
 @Override public Optional<EducationTccReservationRecord> find(String tx,String branch){List<EducationTccReservationRecord> r=jdbc.query("SELECT "+COLS+" FROM cpf_ref_tcc_reservation WHERE transaction_id=? AND branch_id=?",JdbcEducationTccReservationStore::map,tx,branch);return r.stream().findFirst();}
 @CpfTx(id = "EDU-RVATIONSTORE-01", name = "JdbcEducationTccReservationStoreTx1", ownerDomain = "EDU", transactionManager = "educationReferenceFixtureTransactionManager")
 @CpfTx(id = "EDU-RVATIONSTORE-02", name = "JdbcEducationTccReservationStoreTx2", ownerDomain = "EDU", transactionManager = "educationReferenceFixtureTransactionManager")
 private boolean insert(EducationTccReservationRecord r,EducationTccReservationState state){try{return jdbc.update("INSERT INTO cpf_ref_tcc_reservation(transaction_id,branch_id,idempotency_key,account_id,amount,state,deadline_at,fencing_token,updated_at) VALUES(?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",r.transactionId(),r.branchId(),r.idempotencyKey(),r.accountId(),r.amount(),state.name(),Timestamp.from(r.deadline()),0L)==1;}catch(org.springframework.dao.DuplicateKeyException duplicate){return false;}}
 @CpfTx(id = "EDU-RVATIONSTORE-03", name = "JdbcEducationTccReservationStoreTx3", ownerDomain = "EDU", transactionManager = "educationReferenceFixtureTransactionManager")
 @CpfTx(id = "EDU-RVATIONSTORE-04", name = "JdbcEducationTccReservationStoreTx4", ownerDomain = "EDU", transactionManager = "educationReferenceFixtureTransactionManager")
 @Override public List<EducationTccReservationRecord> findExpired(int limit){if(limit<1||limit>1000)throw new IllegalArgumentException("limit 1..1000");return jdbc.query(c->{var ps=c.prepareStatement("SELECT "+COLS+" FROM cpf_ref_tcc_reservation WHERE state IN ('TRYING','TRIED','UNKNOWN') AND deadline_at<CURRENT_TIMESTAMP ORDER BY updated_at");ps.setMaxRows(limit);return ps;},JdbcEducationTccReservationStore::map);}
 private static EducationTccReservationRecord map(ResultSet rs,int n)throws SQLException{return new EducationTccReservationRecord(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getBigDecimal(5),EducationTccReservationState.valueOf(rs.getString(6)),rs.getTimestamp(7).toInstant(),rs.getLong(8),rs.getTimestamp(9).toInstant());}
 private static String safe(String s){if(s==null)return null;return s.substring(0,Math.min(500,s.length()));}
}
