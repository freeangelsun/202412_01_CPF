package com.cpf.platform.operations.health;

import com.cpf.platform.operations.api.health.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class CpfHealthRuntimeHarness {
    public static void main(String[] args) throws Exception {
        CpfDrainManager drain = new CpfDrainManager();
        if (!drain.tryEnter()) fail("running entry rejected");
        Thread finisher = new Thread(() -> { try { Thread.sleep(20); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } drain.leave(); });
        finisher.start();
        if (drain.beginDrain(Duration.ofSeconds(1)) != CpfDrainState.STOPPED) fail("drain did not stop");
        if (drain.tryEnter()) fail("draining/stopped accepted new work");
        drain.resume(); if (drain.state() != CpfDrainState.RUNNING) fail("resume failed");

        AtomicInteger calls = new AtomicInteger();
        CpfDependencyHealthCheck slow = new CpfDependencyHealthCheck() {
            public CpfDependencyHealth check() { calls.incrementAndGet(); try { Thread.sleep(150); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } return new CpfDependencyHealth("db","jdbc://secret",CpfHealthStatus.UP,"",Instant.now(),0,Map.of()); }
            public String name(){ return "db"; }
        };
        CpfHealthConfig cfg = new CpfHealthConfig(Duration.ofMillis(20), Duration.ofSeconds(1), 1, "sys", "i1", "1", "sha", false);
        try (CpfRuntimeHealthService health = new CpfRuntimeHealthService(cfg, drain, List.of(slow))) {
            CpfRuntimeHealth first = health.snapshot();
            if (first.dependencies().get(0).status() != CpfHealthStatus.UNKNOWN) fail("timeout not UNKNOWN");
            if (!"masked".equals(first.dependencies().get(0).endpointRef())) fail("endpoint leaked");
            health.snapshot(); if (calls.get() != 1) fail("cache ttl ignored");
        }

        CpfRuntimeHealthRegistryMemory registry = new CpfRuntimeHealthRegistryMemory();
        CpfRuntimeHealth older = snap("sys","i1",Instant.parse("2026-01-01T00:00:00Z"));
        CpfRuntimeHealth newer = snap("sys","i1",Instant.parse("2026-01-01T00:01:00Z"));
        registry.upsert(newer); registry.upsert(older);
        if (!registry.find("sys","i1").orElseThrow().observedAt().equals(newer.observedAt())) fail("stale report overwrote newer");
        registry.upsert(snap("sys","i2",Instant.parse("2026-01-01T00:02:00Z")));
        if (registry.list().size() != 2) fail("multi-instance registry query missing");
        if (registry.find("sys", "i2").isEmpty()) fail("instance detail lookup missing");
        if (drain.state() != CpfDrainState.RUNNING) fail("drain state did not remain RUNNING after resume");
        System.out.println("HEALTH_DRAIN_RUNTIME_PASS dependencies=timeout+cache+mask multiInstance=2 drainLifecycle=PASS");
    }
    private static CpfRuntimeHealth snap(String s,String i,Instant observed){ return new CpfRuntimeHealth(s,i,CpfHealthStatus.UP,CpfHealthStatus.UP,CpfHealthStatus.UP,false,false,"1","sha",observed.minusSeconds(60),observed,60000,List.of(),List.of(),List.of(),Map.of()); }
    private static void fail(String m){ throw new IllegalStateException(m); }
}
