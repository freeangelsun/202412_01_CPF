package com.cpf.education.scenarios.online.integrated;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class OnlineAbcdEducationFlowTest {
    @Test
    void success_retry_duplicate_and_identity_are_end_to_end_stable() {
        var f = fixture();
        var ok = f.controller.execute(req("TX-001", "K1", 1));
        assertEquals(OnlineAbcdEducationFlow.Outcome.SUCCESS, ok.outcome());
        assertTrue(ok.events().stream().allMatch(e -> e.transactionId().equals("TX-001")));
        assertEquals(1, f.remote.sideEffects.get());

        var duplicate = f.controller.execute(req("TX-001", "K1", 2));
        assertSame(ok, duplicate);
        assertEquals("TX-001", duplicate.transactionId());
        assertEquals(1, f.remote.sideEffects.get());

        var conflict = f.controller.execute(req("TX-DIFFERENT", "K1", 1));
        assertEquals(OnlineAbcdEducationFlow.Outcome.CONFLICT, conflict.outcome());
        assertEquals(1, f.remote.sideEffects.get());
    }

    @Test
    void a_b_c_and_remote_failures_rollback_without_side_effect_duplication() {
        var aFail = fixture(); aFail.a.fail = true;
        assertEquals(OnlineAbcdEducationFlow.Outcome.FAILED, aFail.controller.execute(req("TX-A", "KA", 1)).outcome());
        assertTrue(aFail.repo.snapshot().isEmpty()); assertEquals(0, aFail.remote.sideEffects.get());

        var bFail = fixture(); bFail.b.failBeforeRemote = true;
        assertEquals(OnlineAbcdEducationFlow.Outcome.FAILED, bFail.controller.execute(req("TX-B", "KB", 1)).outcome());
        assertTrue(bFail.repo.snapshot().isEmpty()); assertEquals(0, bFail.remote.sideEffects.get());

        var cFail = fixture(); cFail.c.fail = true;
        assertEquals(OnlineAbcdEducationFlow.Outcome.FAILED, cFail.controller.execute(req("TX-C", "KC", 1)).outcome());
        assertTrue(cFail.repo.snapshot().isEmpty()); assertEquals(0, cFail.remote.sideEffects.get());

        var remoteFail = fixture(); remoteFail.remote.fail = true;
        assertEquals(OnlineAbcdEducationFlow.Outcome.FAILED, remoteFail.controller.execute(req("TX-R", "KR", 1)).outcome());
        assertTrue(remoteFail.repo.snapshot().isEmpty()); assertEquals(0, remoteFail.remote.sideEffects.get());
    }

    @Test
    void db_failure_before_remote_is_failed_but_remote_success_then_db_failure_is_unknown_and_reconciled() {
        var before = fixture(); before.repo.failFind = true;
        var failed = before.controller.execute(req("TX-DB1", "KDB1", 1));
        assertEquals(OnlineAbcdEducationFlow.Outcome.FAILED, failed.outcome());
        assertEquals(0, before.remote.sideEffects.get());

        var after = fixture(); after.repo.failSave = true;
        var unknown = after.controller.execute(req("TX-DB2", "KDB2", 1));
        assertEquals(OnlineAbcdEducationFlow.Outcome.UNKNOWN, unknown.outcome());
        assertTrue(after.repo.snapshot().isEmpty());
        assertEquals(1, after.remote.sideEffects.get());
        var reconciled = after.d.reconcile(unknown);
        assertEquals(OnlineAbcdEducationFlow.Outcome.RECONCILED, reconciled.outcome());
        assertEquals("TX-DB2", reconciled.transactionId());
        assertEquals(0, after.remote.sideEffects.get());
    }

    @Test
    void remote_timeout_can_retry_with_same_transaction_and_incremented_attempt() {
        var f = fixture(); f.remote.timeout = true;
        var first = f.controller.execute(req("TX-RETRY", "K-RETRY", 1));
        assertEquals(OnlineAbcdEducationFlow.Outcome.FAILED, first.outcome());
        f.remote.timeout = false;
        var retry = f.controller.execute(req("TX-RETRY", "K-RETRY", 2));
        assertEquals(OnlineAbcdEducationFlow.Outcome.SUCCESS, retry.outcome());
        assertEquals("TX-RETRY", retry.transactionId());
        assertEquals(2, retry.attempt());
        assertEquals(1, f.remote.sideEffects.get());
    }

    @Test
    void d_failure_keeps_unknown_operator_visible() {
        var f = fixture(); f.repo.failSave = true;
        var unknown = f.controller.execute(req("TX-D", "KD", 1));
        f.d.fail = true;
        var result = f.d.reconcile(unknown);
        assertEquals(OnlineAbcdEducationFlow.Outcome.UNKNOWN, result.outcome());
        assertTrue(result.events().stream().anyMatch(e -> e.segment().equals("D") && e.state().equals("UNKNOWN")));
    }

    @Test
    void concurrent_same_business_key_allows_only_one_remote_side_effect() throws Exception {
        var repo = new OnlineAbcdEducationFlow.InMemoryRepository();
        var entered = new CountDownLatch(1); var release = new CountDownLatch(1); var effects = new AtomicInteger();
        OnlineAbcdEducationFlow.RemotePort remote = new OnlineAbcdEducationFlow.RemotePort() {
            public String invoke(String tx, String key, String payload) {
                effects.incrementAndGet(); entered.countDown();
                try { assertTrue(release.await(3, TimeUnit.SECONDS)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
                return "REMOTE:" + payload;
            }
            public void compensate(String tx, String key) { effects.decrementAndGet(); }
        };
        var c = new OnlineAbcdEducationFlow.DomainC(remote); var b = new OnlineAbcdEducationFlow.DomainB(c, repo);
        var a = new OnlineAbcdEducationFlow.DomainA(b); var controller = new OnlineAbcdEducationFlow.Controller(a);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var first = pool.submit(() -> controller.execute(req("TX-CON", "KC", 1)));
            assertTrue(entered.await(3, TimeUnit.SECONDS));
            var second = pool.submit(() -> controller.execute(req("TX-CON", "KC", 1)));
            var secondResult = second.get(3, TimeUnit.SECONDS);
            assertEquals(OnlineAbcdEducationFlow.Outcome.CONFLICT, secondResult.outcome());
            release.countDown();
            assertEquals(OnlineAbcdEducationFlow.Outcome.SUCCESS, first.get(3, TimeUnit.SECONDS).outcome());
        }
        assertEquals(1, effects.get());
    }

    @Test
    void rollback_restores_preexisting_value_instead_of_deleting_it() {
        var f = fixture();
        f.repo.begin("K-OLD"); f.repo.save("K-OLD", "OLD"); f.repo.commit("K-OLD");
        f.b.failAfterRemote = true;
        var result = f.controller.execute(req("TX-OLD", "K-OLD", 1));
        assertEquals(OnlineAbcdEducationFlow.Outcome.UNKNOWN, result.outcome());
        assertEquals("OLD", f.repo.snapshot().get("K-OLD"));
        assertEquals(1, f.remote.sideEffects.get());
        assertEquals(OnlineAbcdEducationFlow.Outcome.RECONCILED, f.d.reconcile(result).outcome());
        assertEquals("OLD", f.repo.snapshot().get("K-OLD"));
    }

    @Test
    void failure_after_local_save_before_commit_rolls_back_previous_db_value() {
        var f = fixture();
        f.repo.begin("K-TX"); f.repo.save("K-TX", "OLD"); f.repo.commit("K-TX");
        f.b.failAfterSave = true;
        var result = f.controller.execute(req("TX-AFTER-SAVE", "K-TX", 1));
        assertEquals(OnlineAbcdEducationFlow.Outcome.UNKNOWN, result.outcome());
        assertEquals("OLD", f.repo.snapshot().get("K-TX"));
        assertEquals(1, f.remote.sideEffects.get());
    }

    private static OnlineAbcdEducationFlow.Request req(String tx, String key, int attempt) {
        return new OnlineAbcdEducationFlow.Request(tx, key, "P", attempt);
    }
    private static Fixture fixture() {
        var repo = new OnlineAbcdEducationFlow.InMemoryRepository(); var remote = new OnlineAbcdEducationFlow.ScenarioRemote();
        var c = new OnlineAbcdEducationFlow.DomainC(remote); var b = new OnlineAbcdEducationFlow.DomainB(c, repo);
        var a = new OnlineAbcdEducationFlow.DomainA(b); var controller = new OnlineAbcdEducationFlow.Controller(a);
        return new Fixture(repo, remote, c, b, a, controller, new OnlineAbcdEducationFlow.DomainD(repo, remote));
    }
    private record Fixture(OnlineAbcdEducationFlow.InMemoryRepository repo, OnlineAbcdEducationFlow.ScenarioRemote remote,
                           OnlineAbcdEducationFlow.DomainC c, OnlineAbcdEducationFlow.DomainB b, OnlineAbcdEducationFlow.DomainA a,
                           OnlineAbcdEducationFlow.Controller controller, OnlineAbcdEducationFlow.DomainD d) {}
}
