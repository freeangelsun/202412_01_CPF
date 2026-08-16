package com.cpf.security.session.valkey;

import com.cpf.security.api.CpfSessionOperations;
import com.cpf.security.api.CpfSessionSnapshot;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MeteredCpfSessionOperationsTest {
    @Test
    void metricsSeparateMissFromProviderFailureAndTrackForcedLogout() {
        var delegate = new FakeSessions();
        var sessions = new MeteredCpfSessionOperations(delegate);
        sessions.create("tenant-a", "user-a", Duration.ofMinutes(10), Map.of());
        assertTrue(sessions.find("missing").isEmpty());
        assertEquals(2, sessions.revokePrincipal("tenant-a", "user-a", "ADMIN_LOGOUT"));
        delegate.fail = true;
        assertThrows(IllegalStateException.class, () -> sessions.find("boom"));
        var metrics = sessions.snapshot();
        assertEquals(1L, metrics.creates());
        assertEquals(1L, metrics.misses());
        assertEquals(1L, metrics.providerFailures());
        assertEquals(2L, metrics.forcedLogouts());
        assertEquals(2L, metrics.revocations());
    }

    private static final class FakeSessions implements CpfSessionOperations {
        boolean fail;
        @Override public CpfSessionSnapshot create(String t,String p,Duration ttl,Map<String,String> attrs) {
            var now=Instant.parse("2026-08-10T00:00:00Z");
            return new CpfSessionSnapshot("s1",t,p,now,now,now.plus(ttl),1,false,attrs);
        }
        @Override public Optional<CpfSessionSnapshot> find(String id) { if(fail) throw new IllegalStateException("provider down"); return Optional.empty(); }
        @Override public CpfSessionSnapshot renew(String id,Duration ttl){ return create("t","u",ttl,Map.of()); }
        @Override public CpfSessionSnapshot rotate(String id,Duration ttl){ return create("t","u",ttl,Map.of()); }
        @Override public boolean revoke(String id,String reason){ return true; }
        @Override public int revokePrincipal(String t,String u,String reason){ return 2; }
        @Override public List<CpfSessionSnapshot> findByPrincipal(String t,String u){ return List.of(); }
    }
}
