package com.cpf.reference.online.integrated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 온라인 요청을 Controller → Domain A → B → C(/D) → Repository/Remote로 연결하는 실행 가능한 참조 흐름이다.
 * <p>공식 first-hop에서 확정된 transactionId는 전체 호출에서 보존하고 retry에서는 attempt만 증가한다.
 * 외부 Side Effect 성공 뒤 로컬 저장 실패는 성공으로 오판하지 않고 UNKNOWN으로 보존하여 Domain D reconcile로 넘긴다.</p>
 */
public final class OnlineAbcdReferenceFlow {
    public enum Outcome { SUCCESS, FAILED, CONFLICT, UNKNOWN, RECONCILED }
    public record Request(String transactionId, String businessKey, String payload, int attempt) {
        public Request {
            if (transactionId == null || transactionId.isBlank()) throw new IllegalArgumentException("transactionId");
            if (businessKey == null || businessKey.isBlank()) throw new IllegalArgumentException("businessKey");
            if (attempt < 1) throw new IllegalArgumentException("attempt");
        }
    }
    public record Event(String transactionId, String segment, int attempt, String state, String detail) {}
    public record Result(String transactionId, String businessKey, int attempt, Outcome outcome, List<Event> events) {}

    public interface Repository {
        /** 업무 transaction 시작. 구현이 transaction을 지원하지 않으면 no-op이다. */
        default void begin(String key) {}
        Optional<String> find(String key);
        void save(String key, String value);
        /** 현재 transaction을 commit한다. */
        default void commit(String key) {}
        /** 현재 transaction을 rollback한다. */
        void rollback(String key);
    }
    public interface RemotePort {
        String invoke(String transactionId, String businessKey, String payload);
        void compensate(String transactionId, String businessKey);
    }

    public static final class InMemoryRepository implements Repository {
        private final Map<String, String> db = new ConcurrentHashMap<>();
        private final ThreadLocal<Optional<String>> before = new ThreadLocal<>();
        public volatile boolean failFind;
        public volatile boolean failSave;
        public void begin(String key) { before.set(Optional.ofNullable(db.get(key))); }
        public Optional<String> find(String key) {
            if (failFind) throw new LocalDbFailure("find");
            return Optional.ofNullable(db.get(key));
        }
        public void save(String key, String value) {
            if (failSave) throw new LocalDbFailure("save");
            db.put(key, value);
        }
        public void commit(String key) { before.remove(); }
        public void rollback(String key) {
            Optional<String> snapshot = before.get();
            if (snapshot == null) return;
            if (snapshot.isPresent()) db.put(key, snapshot.get()); else db.remove(key);
            before.remove();
        }
        public Map<String, String> snapshot() { return Map.copyOf(db); }
    }

    /**
     * 실제 JDBC transaction을 사용하는 reference Repository. Table은 사전에 격리된 검증 schema에
     * {@code business_key VARCHAR(...), value_text VARCHAR(...)} 형태로 준비해야 한다. SQL identifier는
     * 영숫자/underscore만 허용하며 connection은 ThreadLocal transaction scope에서만 사용한다.
     */
    public static final class JdbcRepository implements Repository {
        private final DataSource dataSource;
        private final String table;
        private final ThreadLocal<Connection> tx = new ThreadLocal<>();
        public JdbcRepository(DataSource dataSource, String table) {
            this.dataSource = java.util.Objects.requireNonNull(dataSource, "dataSource");
            if (table == null || !table.matches("[A-Za-z][A-Za-z0-9_]{0,62}")) throw new IllegalArgumentException("safe table name required");
            this.table = table;
        }
        public void begin(String key) {
            if (tx.get() != null) throw new IllegalStateException("nested reference transaction is not supported");
            try { Connection c=dataSource.getConnection(); c.setAutoCommit(false); tx.set(c); }
            catch (SQLException e) { throw new LocalDbFailure("begin", e); }
        }
        public Optional<String> find(String key) {
            Connection c=requireTx();
            try (PreparedStatement ps=c.prepareStatement("SELECT value_text FROM "+table+" WHERE business_key=?")) {
                ps.setString(1,key); try(ResultSet rs=ps.executeQuery()){ return rs.next()?Optional.ofNullable(rs.getString(1)):Optional.empty(); }
            } catch(SQLException e){ throw new LocalDbFailure("find",e); }
        }
        public void save(String key,String value) {
            Connection c=requireTx();
            try (PreparedStatement up=c.prepareStatement("UPDATE "+table+" SET value_text=? WHERE business_key=?")) {
                up.setString(1,value); up.setString(2,key); int n=up.executeUpdate();
                if(n==0) try(PreparedStatement ins=c.prepareStatement("INSERT INTO "+table+" (business_key,value_text) VALUES (?,?)")){ins.setString(1,key);ins.setString(2,value);ins.executeUpdate();}
            } catch(SQLException e){ throw new LocalDbFailure("save",e); }
        }
        public void commit(String key){ Connection c=requireTx(); try{c.commit();}catch(SQLException e){throw new LocalDbFailure("commit",e);}finally{close();} }
        public void rollback(String key){ Connection c=tx.get(); if(c==null)return; try{c.rollback();}catch(SQLException e){throw new LocalDbFailure("rollback",e);}finally{close();} }
        private Connection requireTx(){Connection c=tx.get();if(c==null)throw new LocalDbFailure("transaction-not-started");return c;}
        private void close(){Connection c=tx.get();tx.remove();if(c!=null)try{c.close();}catch(SQLException ignored){}}
    }

    public static class ScenarioRemote implements RemotePort {
        public final AtomicInteger sideEffects = new AtomicInteger();
        public volatile boolean timeout;
        public volatile boolean fail;
        public volatile boolean compensateFail;
        public String invoke(String tx, String key, String payload) {
            if (timeout) throw new RemoteTimeout();
            if (fail) throw new IllegalStateException("remote-failure");
            sideEffects.incrementAndGet();
            return "REMOTE:" + payload;
        }
        public void compensate(String tx, String key) {
            if (compensateFail) throw new IllegalStateException("compensation-failure");
            sideEffects.updateAndGet(v -> Math.max(0, v - 1));
        }
    }
    public static final class RemoteTimeout extends RuntimeException {}
    public static final class LocalDbFailure extends RuntimeException {
        public LocalDbFailure(String phase) { super(phase); }
        public LocalDbFailure(String phase, Throwable cause) { super(phase, cause); }
    }
    public static final class DomainFailure extends RuntimeException {
        public DomainFailure(String phase) { super(phase); }
    }

    /** Channel/Controller boundary. */
    public static final class Controller {
        private final DomainA a;
        public Controller(DomainA a) { this.a = a; }
        public Result execute(Request request) { return a.execute(request); }
    }

    /** Domain A owns idempotency and same-business-key serialization. */
    public static final class DomainA {
        private final DomainB b;
        private final Map<String, Result> terminal = new ConcurrentHashMap<>();
        private final Set<String> inFlight = ConcurrentHashMap.newKeySet();
        public volatile boolean fail;
        public DomainA(DomainB b) { this.b = b; }
        public Result execute(Request r) {
            Result old = terminal.get(r.businessKey());
            if (old != null) {
                if (!old.transactionId().equals(r.transactionId())) {
                    return conflict(r, "duplicate-business-key-different-transaction");
                }
                return old;
            }
            if (!inFlight.add(r.businessKey())) return conflict(r, "concurrent-business-key");
            try {
                if (fail) throw new DomainFailure("A");
                Result x = b.execute(r);
                if (x.outcome() == Outcome.SUCCESS || x.outcome() == Outcome.RECONCILED) {
                    terminal.putIfAbsent(r.businessKey(), x);
                }
                return x;
            } catch (RuntimeException ex) {
                return failed(r, "A", ex);
            } finally {
                inFlight.remove(r.businessKey());
            }
        }
        private static Result conflict(Request r, String detail) {
            return new Result(r.transactionId(), r.businessKey(), r.attempt(), Outcome.CONFLICT,
                    List.of(new Event(r.transactionId(), "A", r.attempt(), "CONFLICT", detail)));
        }
        private static Result failed(Request r, String segment, RuntimeException ex) {
            return new Result(r.transactionId(), r.businessKey(), r.attempt(), Outcome.FAILED,
                    List.of(new Event(r.transactionId(), segment, r.attempt(), "FAILED", ex.getClass().getSimpleName())));
        }
    }

    /** Domain B owns the local transaction boundary around Domain C remote invocation. */
    public static final class DomainB {
        private final DomainC c;
        private final Repository repo;
        public volatile boolean failBeforeRemote;
        public volatile boolean failAfterRemote;
        public volatile boolean failAfterSave;
        public DomainB(DomainC c, Repository repo) { this.c = c; this.repo = repo; }
        public Result execute(Request r) {
            List<Event> e = new ArrayList<>();
            try {
                repo.begin(r.businessKey());
                e.add(new Event(r.transactionId(), "B", r.attempt(), "DB_BEFORE", repo.find(r.businessKey()).orElse("<none>")));
                if (failBeforeRemote) throw new DomainFailure("B-before-remote");
                String remote = c.execute(r, e);
                if (failAfterRemote) throw new LocalDbFailure("after-remote");
                repo.save(r.businessKey(), remote);
                if (failAfterSave) throw new LocalDbFailure("after-save-before-commit");
                repo.commit(r.businessKey());
                e.add(new Event(r.transactionId(), "B", r.attempt(), "COMMIT", remote));
                return new Result(r.transactionId(), r.businessKey(), r.attempt(), Outcome.SUCCESS, List.copyOf(e));
            } catch (LocalDbFailure ex) {
                repo.rollback(r.businessKey());
                boolean remoteSucceeded = e.stream().anyMatch(x -> x.segment().equals("C") && x.state().equals("REMOTE_SUCCESS"));
                String state = remoteSucceeded ? "UNKNOWN" : "ROLLBACK";
                e.add(new Event(r.transactionId(), "B", r.attempt(), state, ex.getMessage()));
                return new Result(r.transactionId(), r.businessKey(), r.attempt(),
                        remoteSucceeded ? Outcome.UNKNOWN : Outcome.FAILED, List.copyOf(e));
            } catch (RuntimeException ex) {
                repo.rollback(r.businessKey());
                e.add(new Event(r.transactionId(), "B", r.attempt(), "ROLLBACK", ex.getClass().getSimpleName()));
                return new Result(r.transactionId(), r.businessKey(), r.attempt(), Outcome.FAILED, List.copyOf(e));
            }
        }
    }

    /** Domain C invokes the remote port and records a separate call segment. */
    public static final class DomainC {
        private final RemotePort remote;
        public volatile boolean fail;
        public DomainC(RemotePort remote) { this.remote = remote; }
        String execute(Request r, List<Event> e) {
            if (fail) throw new DomainFailure("C");
            e.add(new Event(r.transactionId(), "C", r.attempt(), "REMOTE_BEGIN", r.businessKey()));
            String x = remote.invoke(r.transactionId(), r.businessKey(), r.payload());
            e.add(new Event(r.transactionId(), "C", r.attempt(), "REMOTE_SUCCESS", x));
            return x;
        }
    }

    /** Domain D is the explicit reconcile/compensation path for UNKNOWN. */
    public static final class DomainD {
        private final Repository repo;
        private final RemotePort remote;
        public volatile boolean fail;
        public DomainD(Repository repo, RemotePort remote) { this.repo = repo; this.remote = remote; }
        public Result reconcile(Result unknown) {
            if (unknown.outcome() != Outcome.UNKNOWN) return unknown;
            List<Event> e = new ArrayList<>(unknown.events());
            try {
                if (fail) throw new DomainFailure("D");
                remote.compensate(unknown.transactionId(), unknown.businessKey());
                repo.rollback(unknown.businessKey());
                e.add(new Event(unknown.transactionId(), "D", unknown.attempt(), "RECONCILED", "compensated"));
                return new Result(unknown.transactionId(), unknown.businessKey(), unknown.attempt(), Outcome.RECONCILED, List.copyOf(e));
            } catch (RuntimeException ex) {
                e.add(new Event(unknown.transactionId(), "D", unknown.attempt(), "UNKNOWN", ex.getClass().getSimpleName()));
                return new Result(unknown.transactionId(), unknown.businessKey(), unknown.attempt(), Outcome.UNKNOWN, List.copyOf(e));
            }
        }
    }

    private OnlineAbcdReferenceFlow() {}
}
