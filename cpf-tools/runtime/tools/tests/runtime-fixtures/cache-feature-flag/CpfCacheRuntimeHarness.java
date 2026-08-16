package com.cpf.data.cache;
import com.cpf.data.cache.api.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
public final class CpfCacheRuntimeHarness {
 public static void main(String[] a) throws Exception {
  CpfLocalCacheProvider p=new CpfLocalCacheProvider(); CpfCacheKey k1=new CpfCacheKey("Profile","user-1","TENANT_A"),k2=new CpfCacheKey("Profile","user-1","TENANT_B");
  req(!p.get(k1).found(),"initial miss"); p.put(k1,val("A",1),Duration.ofMillis(120)); p.put(k2,val("B",1),Duration.ofSeconds(5)); req(txt(p.get(k1)).equals("A"),"hit A"); req(txt(p.get(k2)).equals("B"),"hit B");
  Thread.sleep(180); req(!p.get(k1).found(),"ttl"); req(txt(p.get(k2)).equals("B"),"tenant isolation"); p.put(new CpfCacheKey("Profile","user-2","TENANT_B"),val("B2",2),Duration.ofSeconds(5)); req(p.evictNamespace("TENANT_B","Profile")==2,"namespace evict");
  CpfLockToken t1=p.tryAcquire("cache:profile:user-1",Duration.ZERO,Duration.ofMillis(150)).orElseThrow(); req(p.tryAcquire("cache:profile:user-1",Duration.ZERO,Duration.ofMillis(150)).isEmpty(),"contention"); CpfLockToken forged=new CpfLockToken(t1.lockName(),"forged",t1.fencingToken(),t1.acquiredAt(),t1.expiresAt()); req(!p.release(forged),"owner fence"); req(p.release(t1),"release"); CpfLockToken t2=p.tryAcquire("cache:profile:user-1",Duration.ZERO,Duration.ofMillis(150)).orElseThrow(); req(t2.fencingToken()>t1.fencingToken(),"monotonic fence"); p.release(t2);
  illegal(()->p.put(k1,val("x",1),Duration.ZERO)); illegal(()->p.tryAcquire("bad lock space",Duration.ZERO,Duration.ofSeconds(1))); illegal(()->new CpfCacheKey("bad!","k","t")); illegal(()->p.put(k1,CpfCacheValue.miss(),Duration.ofSeconds(1)));
  CpfCacheMetricsSnapshot m=p.metrics(); req(m.hits()>=3&&m.misses()>=2&&m.puts()>=3&&m.evictions()>=2&&m.lockContentions()>=1,"metrics"); req(p.health().ready()&&!p.health().durableInvalidationConfigured(),"health"); System.out.println("CACHE_RUNTIME_PASS");
 }
 static CpfCacheValue val(String s,long v){return new CpfCacheValue(true,false,s.getBytes(StandardCharsets.UTF_8),"text/plain",v,Instant.now().plusSeconds(60));} static String txt(CpfCacheValue v){return new String(v.payload(),StandardCharsets.UTF_8);} static void req(boolean b,String m){if(!b)throw new AssertionError(m);} static void illegal(R r)throws Exception{try{r.run();throw new AssertionError("expected illegal");}catch(IllegalArgumentException ok){}} interface R{void run()throws Exception;}
}
