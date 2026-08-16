package com.cpf.education.data.transaction.tcc;

import com.cpf.data.persistence.api.annotation.CpfTx;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * EDU TCC 예약 상태를 Oracle/PostgreSQL/MariaDB 공통 JDBC 계약으로 저장하는 구현입니다.
 * Vendor 전용 paging 대신 {@code setMaxRows}를 사용하고 fencing token으로 상태전이 충돌을 차단합니다.
 */
public final class JdbcEducationTccReservationStore implements EducationTccReservationStore {
    private static final String COLS =
            "transaction_id,branch_id,idempotency_key,account_id,amount,state,deadline_at,fencing_token,updated_at";
    private final JdbcTemplate jdbc;

    /** 표준 JdbcTemplate만 받아 DB Vendor와 무관한 TCC 상태 저장소를 구성한다. */
    public JdbcEducationTccReservationStore(JdbcTemplate jdbc) {
        this.jdbc = java.util.Objects.requireNonNull(jdbc, "jdbc");
    }

    @Override
    public Optional<EducationTccReservationRecord> find(String transactionId, String branchId) {
        List<EducationTccReservationRecord> rows = jdbc.query(
                "SELECT " + COLS + " FROM cpf_ref_tcc_reservation WHERE transaction_id=? AND branch_id=?",
                JdbcEducationTccReservationStore::map,
                transactionId,
                branchId);
        return rows.stream().findFirst();
    }

    @Override
    @CpfTx(id = "EDU-RVATIONSTORE-01", name = "EducationTccCreateTry", ownerDomain = "EDU",
            transactionManager = "educationReferenceFixtureTransactionManager")
    public boolean createTry(EducationTccReservationRecord record) {
        return insert(record, EducationTccReservationState.TRYING);
    }

    @Override
    @CpfTx(id = "EDU-RVATIONSTORE-02", name = "EducationTccCreateEmptyRollback", ownerDomain = "EDU",
            transactionManager = "educationReferenceFixtureTransactionManager")
    public boolean createEmptyRollback(EducationTccReservationRecord record) {
        return insert(record, EducationTccReservationState.CANCELED);
    }

    @Override
    @CpfTx(id = "EDU-RVATIONSTORE-03", name = "EducationTccTransition", ownerDomain = "EDU",
            transactionManager = "educationReferenceFixtureTransactionManager")
    /** 예상 상태와 fencing token이 모두 일치할 때만 다음 상태로 전이해 중복 확정을 방지한다. */
    public boolean transition(String transactionId, String branchId, EducationTccReservationState expected,
                              EducationTccReservationState next, long expectedFence) {
        int updated = jdbc.update(
                "UPDATE cpf_ref_tcc_reservation SET state=?, fencing_token=fencing_token+1, updated_at=CURRENT_TIMESTAMP "
                        + "WHERE transaction_id=? AND branch_id=? AND state=? AND fencing_token=?",
                next.name(), transactionId, branchId, expected.name(), expectedFence);
        return updated == 1;
    }

    @Override
    @CpfTx(id = "EDU-RVATIONSTORE-04", name = "EducationTccManualReview", ownerDomain = "EDU",
            transactionManager = "educationReferenceFixtureTransactionManager")
    /** 자동 확정이 위험한 예약을 수동검토 상태로 격리하고 사유를 안전한 길이로 저장한다. */
    public boolean markManualReview(String transactionId, String branchId, String reason) {
        String safeReason = safe(reason);
        if (safeReason == null || safeReason.isBlank()) {
            throw new IllegalArgumentException("reason is required");
        }
        int updated = jdbc.update(
                "UPDATE cpf_ref_tcc_reservation SET state='MANUAL_REVIEW', review_reason=?, "
                        + "fencing_token=fencing_token+1, updated_at=CURRENT_TIMESTAMP "
                        + "WHERE transaction_id=? AND branch_id=? AND state<>'CONFIRMED'",
                safeReason, transactionId, branchId);
        return updated == 1;
    }

    @Override
    public List<EducationTccReservationRecord> findExpired(int limit) {
        if (limit < 1 || limit > 1000) {
            throw new IllegalArgumentException("limit 1..1000");
        }
        return jdbc.query(connection -> {
            var statement = connection.prepareStatement(
                    "SELECT " + COLS + " FROM cpf_ref_tcc_reservation "
                            + "WHERE state IN ('TRYING','TRIED','UNKNOWN') AND deadline_at<CURRENT_TIMESTAMP "
                            + "ORDER BY updated_at");
            statement.setMaxRows(limit);
            return statement;
        }, JdbcEducationTccReservationStore::map);
    }

    private boolean insert(EducationTccReservationRecord record, EducationTccReservationState state) {
        try {
            return jdbc.update(
                    "INSERT INTO cpf_ref_tcc_reservation(transaction_id,branch_id,idempotency_key,account_id,amount,state,deadline_at,fencing_token,updated_at) "
                            + "VALUES(?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                    record.transactionId(), record.branchId(), record.idempotencyKey(), record.accountId(),
                    record.amount(), state.name(), Timestamp.from(record.deadline()), 0L) == 1;
        // 동일 멱등키의 중복 TRY는 예외 확산 대신 기존 예약 존재로 처리해 재시도 중복효과를 막는다.
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }

    private static EducationTccReservationRecord map(ResultSet rs, int rowNum) throws SQLException {
        return new EducationTccReservationRecord(
                rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getBigDecimal(5),
                EducationTccReservationState.valueOf(rs.getString(6)), rs.getTimestamp(7).toInstant(),
                rs.getLong(8), rs.getTimestamp(9).toInstant());
    }

    private static String safe(String value) {
        if (value == null) return null;
        return value.substring(0, Math.min(500, value.length()));
    }
}
