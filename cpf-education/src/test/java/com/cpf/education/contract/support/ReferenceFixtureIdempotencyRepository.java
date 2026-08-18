package com.cpf.education.contract.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

/** Reference DB lifecycle 검증에서 동일 idempotency key의 재실행/충돌을 실제 DB로 확인하는 test-support입니다. */
public final class ReferenceFixtureIdempotencyRepository {
    public enum RegisterResult { INSERTED, REPLAY }

    public RegisterResult register(Connection connection, String requirementId, String idempotencyKey, String payloadHash) throws SQLException {
        String executionId = "LIVE-" + Long.toUnsignedString(System.nanoTime(), 36);
        String sql = "INSERT INTO CPF_EDU_OPERATION (EXECUTION_ID,REQUIREMENT_ID,BUSINESS_KEY,IDEMPOTENCY_KEY,PAYLOAD_HASH,ACTOR_ID,ACTOR_ROLES,DATA_SCOPE,STATE,EXPECTED_VERSION,RECORD_VERSION,FENCING_TOKEN,RETRY_COUNT,MAX_RETRIES,FAILURE_POINT,RESULT_CODE,RESULT_MESSAGE,REQUEST_ID,TRACE_ID,PAYLOAD_JSON,RESULT_JSON,CREATED_AT,UPDATED_AT,COMPLETED_AT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int i=1;
            ps.setString(i++,executionId); ps.setString(i++,requirementId); ps.setString(i++,"LIVE-IDEMPOTENCY"); ps.setString(i++,idempotencyKey);
            ps.setString(i++,payloadHash); ps.setString(i++,"CPF_TEST"); ps.setString(i++,"TEST"); ps.setString(i++,"REFERENCE_FIXTURE");
            ps.setString(i++,"ACCEPTED"); ps.setLong(i++,0L); ps.setLong(i++,0L); ps.setLong(i++,0L); ps.setInt(i++,0); ps.setInt(i++,0);
            ps.setString(i++,""); ps.setString(i++,""); ps.setString(i++,""); ps.setString(i++,executionId); ps.setString(i++,executionId);
            ps.setString(i++,"{}"); ps.setString(i++,"{}");
            var now=java.sql.Timestamp.from(Instant.now()); ps.setTimestamp(i++,now); ps.setTimestamp(i++,now); ps.setNull(i,java.sql.Types.TIMESTAMP);
            ps.executeUpdate(); return RegisterResult.INSERTED;
        } catch (SQLException duplicate) {
            String current = payloadHash(connection, requirementId, idempotencyKey);
            if (current == null) throw duplicate;
            if (current.equals(payloadHash)) return RegisterResult.REPLAY;
            throw new IllegalStateException("IDEMPOTENCY_PAYLOAD_CONFLICT", duplicate);
        }
    }

    public int count(Connection connection, String requirementId, String idempotencyKey) throws SQLException {
        try (PreparedStatement ps=connection.prepareStatement("SELECT COUNT(*) FROM CPF_EDU_OPERATION WHERE REQUIREMENT_ID=? AND IDEMPOTENCY_KEY=?")) {
            ps.setString(1,requirementId); ps.setString(2,idempotencyKey); try(ResultSet rs=ps.executeQuery()){rs.next();return rs.getInt(1);}
        }
    }
    public void delete(Connection connection, String requirementId, String idempotencyKey) throws SQLException {
        try(PreparedStatement ps=connection.prepareStatement("DELETE FROM CPF_EDU_OPERATION WHERE REQUIREMENT_ID=? AND IDEMPOTENCY_KEY=?")){ps.setString(1,requirementId);ps.setString(2,idempotencyKey);ps.executeUpdate();}
    }
    private String payloadHash(Connection connection,String requirementId,String idempotencyKey) throws SQLException {
        try(PreparedStatement ps=connection.prepareStatement("SELECT PAYLOAD_HASH FROM CPF_EDU_OPERATION WHERE REQUIREMENT_ID=? AND IDEMPOTENCY_KEY=?")){ps.setString(1,requirementId);ps.setString(2,idempotencyKey);try(ResultSet rs=ps.executeQuery()){return rs.next()?rs.getString(1):null;}}
    }
}
