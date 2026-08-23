package com.cpf.security.audit.jdbc;

import com.cpf.security.api.audit.CpfTamperAuditHead;
import com.cpf.security.api.audit.CpfTamperAuditRecord;
import com.cpf.security.api.audit.CpfTamperAuditStore;
import com.cpf.security.api.crypto.CpfDigitalSignature;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** Append-only tamper-audit JDBC store. Head CAS + record insert run in one local transaction. */
public class JdbcCpfTamperAuditStore implements CpfTamperAuditStore {
    private static final String SELECT_RECORD = "SELECT sequence_no,transaction_id,actor_id,action_code,payload_hash,previous_hash,current_hash,key_id,key_version,algorithm,certificate_id,signature_value,occurred_at FROM cpf_tamper_audit";
    private final JdbcTemplate jdbc;

    public JdbcCpfTamperAuditStore(JdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
    }

    @Override
    public Optional<CpfTamperAuditRecord> latest() {
        List<CpfTamperAuditRecord> rows = jdbc.query(SELECT_RECORD + " ORDER BY sequence_no DESC", ps -> ps.setMaxRows(1), JdbcCpfTamperAuditStore::map);
        return rows.stream().findFirst();
    }

    @Override
    public CpfTamperAuditHead head() {
        return jdbc.queryForObject(
            "SELECT sequence_no,current_hash FROM cpf_tamper_audit_head WHERE head_id=1",
            (rs, n) -> new CpfTamperAuditHead(rs.getLong("sequence_no"), rs.getString("current_hash")));
    }

    @Override
    @Transactional
    public boolean append(String expectedPreviousHash, CpfTamperAuditRecord record) {
        Objects.requireNonNull(record, "record");
        int head = jdbc.update(
            "UPDATE cpf_tamper_audit_head SET sequence_no=?, current_hash=?, version_no=version_no+1, updated_at=CURRENT_TIMESTAMP WHERE head_id=1 AND current_hash=? AND sequence_no=?",
            record.sequence(), record.currentHash(), expectedPreviousHash, record.sequence() - 1);
        if (head != 1) return false;
        int inserted = jdbc.update(
            "INSERT INTO cpf_tamper_audit(sequence_no,transaction_id,actor_id,action_code,payload_hash,previous_hash,current_hash,key_id,key_version,algorithm,certificate_id,signature_value,occurred_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
            record.sequence(), record.transactionId(), record.actor(), record.action(), record.payloadHash(), record.previousHash(), record.currentHash(),
            record.signature().keyId(), record.signature().keyVersion(), record.signature().algorithm(), record.signature().certificateId(), record.signature().signature(), Timestamp.from(record.occurredAt()));
        if (inserted != 1) throw new IllegalStateException("tamper audit insert failed");
        return true;
    }

    @Override
    public List<CpfTamperAuditRecord> scan(long from, int limit) {
        if (from < 1) throw new IllegalArgumentException("fromSequence must be >= 1");
        if (limit < 1 || limit > 10000) throw new IllegalArgumentException("limit 1..10000");
        return jdbc.query(con -> {
            PreparedStatement ps = con.prepareStatement(SELECT_RECORD + " WHERE sequence_no>=? ORDER BY sequence_no");
            ps.setLong(1, from);
            ps.setMaxRows(limit);
            return ps;
        }, JdbcCpfTamperAuditStore::map);
    }

    private static CpfTamperAuditRecord map(ResultSet rs, int rowNum) throws SQLException {
        CpfDigitalSignature sig = new CpfDigitalSignature(
            rs.getString("key_id"), rs.getString("key_version"), rs.getString("algorithm"), rs.getString("certificate_id"),
            rs.getBytes("signature_value"), rs.getTimestamp("occurred_at").toInstant());
        return new CpfTamperAuditRecord(
            rs.getLong("sequence_no"), rs.getString("transaction_id"), rs.getString("actor_id"), rs.getString("action_code"),
            rs.getString("payload_hash"), rs.getString("previous_hash"), rs.getString("current_hash"), sig,
            rs.getTimestamp("occurred_at").toInstant());
    }
}
