package com.cpf.data.persistence.jdbc.locking;

import com.cpf.data.lock.api.CpfLockManager.State;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** OPS_RUNTIME_LOCK 테이블의 JDBC 저장소입니다. */
public final class JdbcCpfLockStore implements CpfJdbcLockManager.Store {
    static final String COLUMNS="LOCK_KEY,OWNER_ID,REQUEST_ID,FENCING_TOKEN,OWNER_EPOCH,VERSION_NO,ACQUIRED_AT,LEASE_UNTIL,LOCK_STATE,LAST_REASON,UPDATED_AT";
    private final JdbcTemplate jdbc;
    public JdbcCpfLockStore(JdbcTemplate jdbc){ this.jdbc=Objects.requireNonNull(jdbc,"jdbc"); }

    @Override public Optional<CpfJdbcLockManager.LockRow> findForUpdate(String key){
        List<CpfJdbcLockManager.LockRow> rows=jdbc.query("SELECT "+COLUMNS+" FROM OPS_RUNTIME_LOCK WHERE LOCK_KEY=? FOR UPDATE",JdbcCpfLockStore::map,key);
        return rows.stream().findFirst();
    }
    @Override public Optional<CpfJdbcLockManager.LockRow> find(String key){
        List<CpfJdbcLockManager.LockRow> rows=jdbc.query("SELECT "+COLUMNS+" FROM OPS_RUNTIME_LOCK WHERE LOCK_KEY=?",JdbcCpfLockStore::map,key);
        return rows.stream().findFirst();
    }
    @Override public List<CpfJdbcLockManager.LockRow> list(int limit){
        return jdbc.query(con -> {
            PreparedStatement ps=con.prepareStatement("SELECT "+COLUMNS+" FROM OPS_RUNTIME_LOCK ORDER BY UPDATED_AT,LOCK_KEY");
            ps.setMaxRows(limit); return ps;
        },JdbcCpfLockStore::map);
    }
    @Override public void insert(CpfJdbcLockManager.LockRow r){
        jdbc.update("INSERT INTO OPS_RUNTIME_LOCK ("+COLUMNS+") VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                r.key(),r.ownerId(),r.requestId(),r.fencingToken(),r.ownerEpoch(),r.version(),ts(r.acquiredAt()),ts(r.leaseUntil()),r.state().name(),r.lastReason(),ts(r.updatedAt()));
    }
    @Override public int update(CpfJdbcLockManager.LockRow r,long expectedVersion){
        return jdbc.update("UPDATE OPS_RUNTIME_LOCK SET OWNER_ID=?,REQUEST_ID=?,FENCING_TOKEN=?,OWNER_EPOCH=?,VERSION_NO=?,ACQUIRED_AT=?,LEASE_UNTIL=?,LOCK_STATE=?,LAST_REASON=?,UPDATED_AT=? WHERE LOCK_KEY=? AND VERSION_NO=?",
                r.ownerId(),r.requestId(),r.fencingToken(),r.ownerEpoch(),r.version(),ts(r.acquiredAt()),ts(r.leaseUntil()),r.state().name(),r.lastReason(),ts(r.updatedAt()),r.key(),expectedVersion);
    }
    private static CpfJdbcLockManager.LockRow map(java.sql.ResultSet rs,int rowNum) throws java.sql.SQLException {
        return new CpfJdbcLockManager.LockRow(rs.getString("LOCK_KEY"),rs.getString("OWNER_ID"),rs.getString("REQUEST_ID"),rs.getLong("FENCING_TOKEN"),rs.getLong("OWNER_EPOCH"),rs.getLong("VERSION_NO"),instant(rs.getTimestamp("ACQUIRED_AT")),instant(rs.getTimestamp("LEASE_UNTIL")),State.valueOf(rs.getString("LOCK_STATE")),rs.getString("LAST_REASON"),instant(rs.getTimestamp("UPDATED_AT")));
    }
    private static Timestamp ts(Instant i){ return Timestamp.from(i); }
    private static Instant instant(Timestamp t){ return t.toInstant(); }
}
