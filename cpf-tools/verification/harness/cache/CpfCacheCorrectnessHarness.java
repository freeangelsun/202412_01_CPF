import com.cpf.data.cache.api.*;
import com.cpf.data.cache.rediscommon.CpfCacheInvalidationCoordinator;
import com.cpf.data.cache.rediscommon.CpfCacheInvalidationProperties;
import com.cpf.data.cache.rediscommon.CpfCacheInvalidationSubjectKey;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public final class CpfCacheCorrectnessHarness {
    static final class FakeCache implements CpfCachePort {
        final ConcurrentMap<String, AtomicLong> evictions = new ConcurrentHashMap<>();
        private void hit(String subject) { evictions.computeIfAbsent(subject, x -> new AtomicLong()).incrementAndGet(); }
        long count(String subject) { var v=evictions.get(subject); return v==null?0:v.get(); }
        @Override public CpfCacheValue get(CpfCacheKey key){ return CpfCacheValue.miss(); }
        @Override public void put(CpfCacheKey key,CpfCacheValue value,Duration ttl){}
        @Override public boolean evict(CpfCacheKey key){ hit(key.tenantId()+"/"+key.namespace()+"/"+key.key()); return true; }
        @Override public long evictNamespace(String tenantId,String namespace){ hit(tenantId+"/"+namespace+"/"); return 1; }
        @Override public CpfCacheMetricsSnapshot metrics(){ return new CpfCacheMetricsSnapshot("FAKE",0,0,0,0,0,0,0,Instant.now()); }
        @Override public CpfCacheHealth health(){ return new CpfCacheHealth(true,"FAKE","LOCAL",false,true,0,List.of(),Instant.now()); }
    }

    static final class Durable implements CpfCacheInvalidationPort {
        final AtomicLong ids = new AtomicLong();
        final List<CpfCacheInvalidationEvent> events = Collections.synchronizedList(new ArrayList<>());
        final ConcurrentMap<String,CpfCacheInvalidationEvent> byKey = new ConcurrentHashMap<>();
        final ConcurrentMap<String,AtomicLong> checkpoints = new ConcurrentHashMap<>();
        final ConcurrentMap<String,AtomicLong> versions = new ConcurrentHashMap<>();
        static String subject(String c,String t,String n,String k){ return c+"|"+t+"|"+n+"|"+(k==null?"":k); }
        @Override public CpfCacheInvalidationEvent append(CpfCacheInvalidationEvent e) {
            CpfCacheInvalidationEvent old=byKey.get(e.eventKey());
            if(old!=null){
                if(!same(old,e)) throw new IllegalStateException("eventKey conflict");
                return old;
            }
            CpfCacheInvalidationEvent p=new CpfCacheInvalidationEvent(ids.incrementAndGet(),e.eventKey(),e.tenantId(),e.namespace(),e.cacheKey(),e.version(),e.reason(),e.requestedBy(),e.createdAt());
            CpfCacheInvalidationEvent race=byKey.putIfAbsent(e.eventKey(),p);
            if(race!=null) return append(e);
            events.add(p); return p;
        }
        private static boolean same(CpfCacheInvalidationEvent a,CpfCacheInvalidationEvent b){ return a.tenantId().equals(b.tenantId())&&a.namespace().equals(b.namespace())&&a.cacheKey().equals(b.cacheKey())&&a.version()==b.version()&&a.reason().equals(b.reason())&&a.requestedBy().equals(b.requestedBy()); }
        @Override public List<CpfCacheInvalidationEvent> loadAfter(long cp,int limit){ synchronized(events){ return events.stream().filter(e->e.eventId()>cp).sorted(Comparator.comparingLong(CpfCacheInvalidationEvent::eventId)).limit(limit).toList(); } }
        @Override public long checkpoint(String c){ return checkpoints.getOrDefault(c,new AtomicLong()).get(); }
        @Override public void checkpoint(String c,long id){ checkpoints.computeIfAbsent(c,x->new AtomicLong()).accumulateAndGet(id,Math::max); }
        @Override public long backlog(String c){ long cp=checkpoint(c); synchronized(events){ return events.stream().filter(e->e.eventId()>cp).count(); } }
        @Override public long version(String c,String t,String n,String k){ var v=versions.get(subject(c,t,n,k)); return v==null?-1:v.get(); }
        @Override public void advanceVersion(String c,String t,String n,String k,long v){ versions.computeIfAbsent(subject(c,t,n,k),x->new AtomicLong(-1)).accumulateAndGet(v,Math::max); }
    }

    static CpfCacheInvalidationCoordinator coordinator(FakeCache cache, Durable d, String consumer){
        CpfCacheInvalidationProperties p=new CpfCacheInvalidationProperties(); p.setConsumerId(consumer); p.setReconcileBatchSize(100); p.setReconcileMaxBatches(10);
        return new CpfCacheInvalidationCoordinator(cache,d,null,p);
    }
    static void require(boolean b,String m){ if(!b) throw new AssertionError(m); }
    public static void main(String[] args) throws Exception {
        require(!CpfCacheInvalidationSubjectKey.encode("").isBlank(),"namespace subject must never persist as empty/NULL");
        require(!CpfCacheInvalidationSubjectKey.encode("").equals(CpfCacheInvalidationSubjectKey.encode("N:")),"namespace sentinel must not collide with real key");
        require(CpfCacheInvalidationSubjectKey.encode("real").equals("K:real"),"key subject discriminator");
        Durable d=new Durable(); FakeCache aCache=new FakeCache(); FakeCache bCache=new FakeCache();
        var a=coordinator(aCache,d,"instance-a"); var b=coordinator(bCache,d,"instance-b");
        String subject="tenant/ns/key";
        a.request("ev-10",new CpfCacheKey("ns","key","tenant"),10,"change","tester");
        a.request("ev-10",new CpfCacheKey("ns","key","tenant"),10,"change","tester");
        require(aCache.count(subject)==1,"duplicate event must not re-evict");
        a.request("ev-09-late",new CpfCacheKey("ns","key","tenant"),9,"late","tester");
        require(aCache.count(subject)==1,"out-of-order lower version must be fenced");
        b.reconcileNow();
        require(bCache.count(subject)==1,"second instance must apply latest version once");
        require(d.version("instance-b","tenant","ns","key")==10,"second instance durable version");

        // Concurrent reconcile of the same backlog must remain exactly-once by durable version fence.
        d.append(new CpfCacheInvalidationEvent(0,"ev-11","tenant","ns","key",11,"change","tester",Instant.now()));
        ExecutorService pool=Executors.newFixedThreadPool(8);
        List<Future<Integer>> fs=new ArrayList<>(); for(int i=0;i<8;i++) fs.add(pool.submit(b::reconcileNow));
        for(Future<Integer> f:fs) f.get(); pool.shutdown();
        require(bCache.count(subject)==2,"concurrent reconcile must apply version 11 once");
        require(d.version("instance-b","tenant","ns","key")==11,"version must advance monotonically");

        // Simulate process restart: a new coordinator with same consumer keeps the durable fence.
        FakeCache restartedCache=new FakeCache(); var restarted=coordinator(restartedCache,d,"instance-a");
        d.append(new CpfCacheInvalidationEvent(0,"ev-08-after-restart","tenant","ns","key",8,"late","tester",Instant.now()));
        restarted.reconcileNow();
        require(restartedCache.count(subject)==1,"restart must catch up missed newer version exactly once");
        require(d.version("instance-a","tenant","ns","key")==11,"restart fence must advance to missed version 11 and reject late version 8");
        System.out.println("CPF_CACHE_CORRECTNESS=PASS duplicate=true outOfOrder=true concurrentReconcile=8 multiInstance=2 durableRestartFence=true");
    }
}
