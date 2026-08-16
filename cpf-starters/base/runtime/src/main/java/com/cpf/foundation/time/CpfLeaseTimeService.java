package com.cpf.foundation.time;
import java.time.*; import java.util.*; import java.util.concurrent.ConcurrentHashMap; import java.util.concurrent.atomic.AtomicLong;
/** Lease/fencing consumer using wall time for audit and monotonic time for expiry. */
public final class CpfLeaseTimeService {
    public record Lease(String resource,String owner,long fencingToken,Instant issuedAt,CpfDeadline deadline){}
    private final CpfTimeOperations time; private final AtomicLong fence=new AtomicLong(); private final Map<String,Lease> leases=new ConcurrentHashMap<>();
    public CpfLeaseTimeService(CpfTimeOperations time){this.time=Objects.requireNonNull(time);}
    public Lease acquire(String resource,String owner,Duration ttl){ Objects.requireNonNull(resource);Objects.requireNonNull(owner); return leases.compute(resource,(k,old)->{if(old!=null&&!old.deadline().expired(time))throw new IllegalStateException("lease already held"); return new Lease(resource,owner,fence.incrementAndGet(),time.now(),time.deadline(ttl));}); }
    public boolean valid(Lease lease){ Lease current=leases.get(lease.resource()); return current!=null&&current.fencingToken()==lease.fencingToken()&&!current.deadline().expired(time); }
    public void release(Lease lease){ leases.computeIfPresent(lease.resource(),(k,current)->current.fencingToken()==lease.fencingToken()?null:current); }
}
