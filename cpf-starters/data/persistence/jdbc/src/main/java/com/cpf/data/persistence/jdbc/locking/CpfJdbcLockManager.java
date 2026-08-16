package com.cpf.data.persistence.jdbc.locking;

import com.cpf.data.lock.api.CpfLockManager;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** JDBC 영속 잠금 저장소를 사용하는 CPF lease/fencing 구현입니다. */
public final class CpfJdbcLockManager implements CpfLockManager {
    static final Duration MAX_LEASE = Duration.ofHours(24);
    private final Store store;
    private final TransactionTemplate tx;
    private final Clock clock;

    public CpfJdbcLockManager(Store store, PlatformTransactionManager transactionManager) {
        this(store, new TransactionTemplate(Objects.requireNonNull(transactionManager, "transactionManager")), Clock.systemUTC());
    }

    CpfJdbcLockManager(Store store, TransactionTemplate tx, Clock clock) {
        this.store = Objects.requireNonNull(store, "store");
        this.tx = Objects.requireNonNull(tx, "tx");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public AcquireResult acquire(String key, String ownerId, String requestId, Duration leaseDuration) {
        if (blank(key) || blank(ownerId) || blank(requestId) || !validLease(leaseDuration)) {
            return new AcquireResult(AcquireStatus.INVALID, null, null, "INVALID_ARGUMENT");
        }
        String k=key.trim(), owner=ownerId.trim(), request=requestId.trim();
        for (int attempt=0; attempt<2; attempt++) {
            try {
                AcquireResult out = tx.execute(status -> acquireLocked(k, owner, request, leaseDuration));
                return out == null ? new AcquireResult(AcquireStatus.UNKNOWN,null,null,"TRANSACTION_NO_RESULT") : out;
            } catch (DuplicateKeyException race) {
                if (attempt == 0) continue;
                return new AcquireResult(AcquireStatus.UNKNOWN,null,null,"CONCURRENT_INSERT_RACE");
            } catch (RuntimeException storageFailure) {
                return new AcquireResult(AcquireStatus.UNKNOWN,null,null,"STORAGE_FAILURE");
            }
        }
        return new AcquireResult(AcquireStatus.UNKNOWN,null,null,"ACQUIRE_RETRY_EXHAUSTED");
    }

    private AcquireResult acquireLocked(String key, String owner, String request, Duration lease) {
        Instant now=clock.instant();
        LockRow current=store.findForUpdate(key).orElse(null);
        if (current == null) {
            LockRow created=new LockRow(key,owner,request,1,1,1,now,now.plus(lease),State.ACTIVE,"ACQUIRED",now);
            store.insert(created);
            return new AcquireResult(AcquireStatus.ACQUIRED, created.token(), created.snapshot(), "ACQUIRED");
        }
        if (current.state()==State.ACTIVE && current.leaseUntil().isAfter(now)) {
            if (current.ownerId().equals(owner) && current.requestId().equals(request)) {
                return new AcquireResult(AcquireStatus.IDEMPOTENT_REPLAY,current.token(),current.snapshot(),"IDEMPOTENT_REPLAY");
            }
            return new AcquireResult(AcquireStatus.BUSY,null,current.snapshot(),"LEASE_ACTIVE");
        }
        long fence=inc(current.fencingToken());
        long epoch=inc(current.ownerEpoch());
        long version=inc(current.version());
        if (fence<0 || epoch<0 || version<0) {
            return new AcquireResult(AcquireStatus.RESOURCE_EXHAUSTED,null,current.snapshot(),"COUNTER_EXHAUSTED");
        }
        LockRow next=new LockRow(key,owner,request,fence,epoch,version,now,now.plus(lease),State.ACTIVE,"TAKEOVER",now);
        if (store.update(next,current.version()) != 1) {
            return new AcquireResult(AcquireStatus.UNKNOWN,null,current.snapshot(),"OPTIMISTIC_CONFLICT");
        }
        return new AcquireResult(AcquireStatus.ACQUIRED,next.token(),next.snapshot(),"ACQUIRED_AFTER_TAKEOVER");
    }

    @Override
    public RenewResult renew(LockToken token, Duration leaseDuration) {
        if (token==null || !validLease(leaseDuration)) return new RenewResult(RenewStatus.INVALID,null,"INVALID_ARGUMENT");
        try {
            RenewResult out=tx.execute(status -> {
                Instant now=clock.instant();
                LockRow current=store.findForUpdate(token.key()).orElse(null);
                if (current==null) return new RenewResult(RenewStatus.NOT_FOUND,null,"NOT_FOUND");
                if (current.state()!=State.ACTIVE) return new RenewResult(RenewStatus.STALE_TOKEN,null,"NOT_ACTIVE");
                if (!current.leaseUntil().isAfter(now)) {
                    expire(current,now,"LEASE_EXPIRED");
                    return new RenewResult(RenewStatus.EXPIRED,null,"LEASE_EXPIRED");
                }
                if (!current.ownerId().equals(token.ownerId())) return new RenewResult(RenewStatus.NOT_OWNER,null,"NOT_OWNER");
                if (!sameToken(current,token)) return new RenewResult(RenewStatus.STALE_TOKEN,null,"STALE_TOKEN");
                long version=inc(current.version());
                if (version<0) return new RenewResult(RenewStatus.UNKNOWN,null,"VERSION_EXHAUSTED");
                LockRow next=current.withLease(version,now.plus(leaseDuration),now,"RENEWED");
                if (store.update(next,current.version())!=1) return new RenewResult(RenewStatus.UNKNOWN,null,"OPTIMISTIC_CONFLICT");
                return new RenewResult(RenewStatus.RENEWED,next.token(),"RENEWED");
            });
            return out==null?new RenewResult(RenewStatus.UNKNOWN,null,"TRANSACTION_NO_RESULT"):out;
        } catch (RuntimeException e) { return new RenewResult(RenewStatus.UNKNOWN,null,"STORAGE_FAILURE"); }
    }

    @Override
    public ReleaseResult release(LockToken token, String reason) {
        if (token==null || blank(reason)) return new ReleaseResult(ReleaseStatus.INVALID,null,"INVALID_ARGUMENT");
        try {
            ReleaseResult out=tx.execute(status -> {
                Instant now=clock.instant();
                LockRow current=store.findForUpdate(token.key()).orElse(null);
                if (current==null) return new ReleaseResult(ReleaseStatus.NOT_FOUND,null,"NOT_FOUND");
                if (current.state()==State.RELEASED && sameOwnerFence(current,token)) {
                    return new ReleaseResult(ReleaseStatus.IDEMPOTENT_REPLAY,current.snapshot(),"IDEMPOTENT_REPLAY");
                }
                if (current.state()!=State.ACTIVE) return new ReleaseResult(ReleaseStatus.STALE_TOKEN,current.snapshot(),"NOT_ACTIVE");
                if (!current.leaseUntil().isAfter(now)) {
                    LockRow expired=expire(current,now,"LEASE_EXPIRED");
                    return new ReleaseResult(ReleaseStatus.EXPIRED,expired.snapshot(),"LEASE_EXPIRED");
                }
                if (current.fencingToken()!=token.fencingToken() || current.version()!=token.version())
                    return new ReleaseResult(ReleaseStatus.STALE_TOKEN,current.snapshot(),"STALE_TOKEN");
                if (!current.ownerId().equals(token.ownerId())) return new ReleaseResult(ReleaseStatus.NOT_OWNER,current.snapshot(),"NOT_OWNER");
                if (!sameToken(current,token)) return new ReleaseResult(ReleaseStatus.STALE_TOKEN,current.snapshot(),"STALE_TOKEN");
                long version=inc(current.version());
                if (version<0) return new ReleaseResult(ReleaseStatus.UNKNOWN,current.snapshot(),"VERSION_EXHAUSTED");
                LockRow released=current.withState(State.RELEASED,version,now,reason.trim());
                if (store.update(released,current.version())!=1) return new ReleaseResult(ReleaseStatus.UNKNOWN,current.snapshot(),"OPTIMISTIC_CONFLICT");
                return new ReleaseResult(ReleaseStatus.RELEASED,released.snapshot(),"RELEASED");
            });
            return out==null?new ReleaseResult(ReleaseStatus.UNKNOWN,null,"TRANSACTION_NO_RESULT"):out;
        } catch (RuntimeException e) { return new ReleaseResult(ReleaseStatus.UNKNOWN,null,"STORAGE_FAILURE"); }
    }

    @Override
    public boolean validateFence(String key, long fencingToken) {
        if (blank(key) || fencingToken<1) return false;
        try {
            Instant now=clock.instant();
            return store.find(key.trim()).filter(r -> r.state()==State.ACTIVE)
                    .filter(r -> r.leaseUntil().isAfter(now))
                    .filter(r -> r.fencingToken()==fencingToken).isPresent();
        } catch (RuntimeException e) { return false; }
    }

    @Override
    public Optional<LockSnapshot> find(String key) {
        if (blank(key)) return Optional.empty();
        try { return store.find(key.trim()).map(LockRow::snapshot); }
        catch (RuntimeException e) { return Optional.empty(); }
    }

    @Override
    public List<LockSnapshot> list(int limit) {
        if (limit<1 || limit>1000) return List.of();
        try { return store.list(limit).stream().map(LockRow::snapshot).toList(); }
        catch (RuntimeException e) { return List.of(); }
    }

    @Override
    public ForceReleaseResult forceRelease(String key, String operatorId, String reason, ForceReleaseApproval approval) {
        if (blank(key)||blank(operatorId)||blank(reason)||approval==null) {
            return new ForceReleaseResult(ForceReleaseStatus.INVALID,null,null,"INVALID_ARGUMENT");
        }
        try {
            ForceReleaseResult out=tx.execute(status -> {
                Instant now=clock.instant();
                LockRow current=store.findForUpdate(key.trim()).orElse(null);
                if (current==null) return new ForceReleaseResult(ForceReleaseStatus.NOT_FOUND,null,null,"NOT_FOUND");
                if (current.state()==State.FORCE_RELEASED) return new ForceReleaseResult(ForceReleaseStatus.IDEMPOTENT_REPLAY,current.snapshot(),null,"IDEMPOTENT_REPLAY");
                if (approval.approverId().equals(operatorId.trim())) return new ForceReleaseResult(ForceReleaseStatus.SEPARATION_OF_DUTIES,current.snapshot(),null,"SAME_REQUESTER_APPROVER");
                if (now.isBefore(approval.approvedAt()) || !now.isBefore(approval.expiresAt())) return new ForceReleaseResult(ForceReleaseStatus.APPROVAL_EXPIRED,current.snapshot(),null,"APPROVAL_EXPIRED");
                ForceReleaseCommand command=new ForceReleaseCommand(current.key(),operatorId.trim(),reason.trim(),current.fencingToken(),current.version());
                if (approval.commandHash()==null || !approval.commandHash().equals(command.immutableHash())) return new ForceReleaseResult(ForceReleaseStatus.APPROVAL_SCOPE_MISMATCH,current.snapshot(),null,"APPROVAL_SCOPE_MISMATCH");
                long version=inc(current.version());
                if (version<0) return new ForceReleaseResult(ForceReleaseStatus.UNKNOWN,current.snapshot(),null,"VERSION_EXHAUSTED");
                LockRow released=current.withState(State.FORCE_RELEASED,version,now,"FORCE_RELEASED");
                if (store.update(released,current.version())!=1) return new ForceReleaseResult(ForceReleaseStatus.UNKNOWN,current.snapshot(),null,"OPTIMISTIC_CONFLICT");
                return new ForceReleaseResult(ForceReleaseStatus.RELEASED,released.snapshot(),null,"FORCE_RELEASED");
            });
            return out==null?new ForceReleaseResult(ForceReleaseStatus.UNKNOWN,null,null,"TRANSACTION_NO_RESULT"):out;
        } catch (RuntimeException e) { return new ForceReleaseResult(ForceReleaseStatus.UNKNOWN,null,null,"STORAGE_FAILURE"); }
    }

    @Override
    public RecoveryResult reconcileExpired(int limit) {
        if (limit<1 || limit>1000) return new RecoveryResult(RecoveryStatus.INVALID,0,0,0,"INVALID_LIMIT");
        int scanned=0,recovered=0,conflicts=0;
        try {
            for (LockRow candidate: store.list(limit)) {
                scanned++;
                if (candidate.state()!=State.ACTIVE || candidate.leaseUntil().isAfter(clock.instant())) continue;
                Boolean changed=tx.execute(status -> {
                    LockRow current=store.findForUpdate(candidate.key()).orElse(null);
                    if (current==null || current.state()!=State.ACTIVE || current.leaseUntil().isAfter(clock.instant())) return false;
                    return store.update(current.withState(State.EXPIRED,inc(current.version()),clock.instant(),"RECONCILED_EXPIRED"),current.version())==1;
                });
                if (Boolean.TRUE.equals(changed)) recovered++; else conflicts++;
            }
            RecoveryStatus rs=conflicts==0?RecoveryStatus.SUCCESS:RecoveryStatus.PARTIAL;
            return new RecoveryResult(rs,scanned,recovered,conflicts,rs.name());
        } catch (RuntimeException e) { return new RecoveryResult(RecoveryStatus.UNKNOWN,scanned,recovered,conflicts,"STORAGE_FAILURE"); }
    }

    private LockRow expire(LockRow current, Instant now, String reason) {
        long version=inc(current.version());
        if (version<0) return current;
        LockRow expired=current.withState(State.EXPIRED,version,now,reason);
        return store.update(expired,current.version())==1?expired:current;
    }
    private static boolean sameToken(LockRow r, LockToken t) {
        return sameOwnerFence(r,t) && r.ownerEpoch()==t.ownerEpoch() && r.version()==t.version() && r.leaseUntil().equals(t.leaseUntil());
    }
    private static boolean sameOwnerFence(LockRow r, LockToken t) {
        return r.key().equals(t.key()) && r.ownerId().equals(t.ownerId()) && r.requestId().equals(t.requestId()) && r.fencingToken()==t.fencingToken();
    }
    private static boolean validLease(Duration d) { return d!=null && !d.isZero() && !d.isNegative() && d.compareTo(MAX_LEASE)<=0; }
    private static boolean blank(String s) { return s==null || s.isBlank(); }
    private static long inc(long v) { return v==Long.MAX_VALUE?-1:v+1; }

    interface Store {
        Optional<LockRow> findForUpdate(String key);
        Optional<LockRow> find(String key);
        List<LockRow> list(int limit);
        void insert(LockRow row);
        int update(LockRow row,long expectedVersion);
    }

    record LockRow(String key,String ownerId,String requestId,long fencingToken,long ownerEpoch,long version,
                   Instant acquiredAt,Instant leaseUntil,State state,String lastReason,Instant updatedAt) {
        LockToken token(){ return new LockToken(key,ownerId,requestId,fencingToken,ownerEpoch,version,leaseUntil); }
        LockSnapshot snapshot(){ return new LockSnapshot(key,ownerId,requestId,fencingToken,ownerEpoch,version,acquiredAt,leaseUntil,state); }
        LockRow withLease(long newVersion,Instant newLease,Instant now,String reason){ return new LockRow(key,ownerId,requestId,fencingToken,ownerEpoch,newVersion,acquiredAt,newLease,state,reason,now); }
        LockRow withState(State newState,long newVersion,Instant now,String reason){ return new LockRow(key,ownerId,requestId,fencingToken,ownerEpoch,newVersion,acquiredAt,leaseUntil,newState,reason,now); }
    }
}
