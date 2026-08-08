package com.cpf.reference.batch.integrated;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class BatchAbcReferenceFlowTest {
    @Test
    void checkpoint_processKill_restart_sameJob_and_duplicate_prevention() {
        var f = fixture(2, 1); var items = items();
        f.step.killAfter = 1;
        var first = f.op.launch(id("TX-B1", "EX1", 1), items, "N1");
        assertEquals(BatchAbcReferenceFlow.State.UNKNOWN, first.state());
        assertEquals(2, first.checkpoint());
        assertEquals(2, first.committed());

        f.step.killAfter = -1;
        var restart = f.op.launch(id("TX-B1", "EX2", 2), items, "N2");
        assertEquals(BatchAbcReferenceFlow.State.SUCCESS, restart.state());
        assertEquals(3, restart.checkpoint());
        assertEquals(3, f.remote.effectKeys().size());
        assertEquals("TX-B1", restart.identity().transactionId());
        assertNotEquals(first.identity().executionId(), restart.identity().executionId());
        assertNotEquals(first.identity().attempt(), restart.identity().attempt());

        var again = f.op.launch(id("TX-B1", "EX3", 3), items, "N3");
        assertEquals(3, again.committed());
        assertEquals(3, f.remote.effectKeys().size());
    }

    @Test
    void retry_then_success_and_skip_have_operator_visible_events() {
        var f = fixture(2, 2); f.remote.failOnceKey = "K1"; f.b.skippableKey = "K2";
        var result = f.op.launch(id("TX-RETRY", "EX1", 1), items(), "N1");
        assertEquals(BatchAbcReferenceFlow.State.SUCCESS, result.state());
        assertEquals(2, f.remote.calls("K1"));
        assertTrue(result.events().stream().anyMatch(e -> e.businessKey().equals("K1") && e.state().equals("RETRY")));
        assertTrue(result.events().stream().anyMatch(e -> e.businessKey().equals("K2") && e.state().equals("SKIPPED")));
        assertEquals(1, result.skipped());
    }

    @Test
    void retry_exhaustion_is_retrying_and_restart_uses_same_transaction_new_execution_attempt() {
        var f = fixture(1, 1); f.remote.timeoutKey = "K1";
        var first = f.op.launch(id("TX-TIMEOUT", "EX1", 1), items(), "N1");
        assertEquals(BatchAbcReferenceFlow.State.RETRYING, first.state());
        f.remote.timeoutKey = null;
        var restart = f.op.launch(id("TX-TIMEOUT", "EX2", 2), items(), "N2");
        assertEquals(BatchAbcReferenceFlow.State.SUCCESS, restart.state());
        assertEquals("TX-TIMEOUT", restart.identity().transactionId());
        assertEquals(2, restart.identity().attempt());
    }

    @Test
    void remote_success_local_db_failure_is_unknown_until_reconciled() {
        var f = fixture(1, 0); f.b.dbFailKey = "K1";
        var result = f.op.launch(id("TX-DB", "EX1", 1), List.of(new BatchAbcReferenceFlow.Item("K1", "1")), "N1");
        assertEquals(BatchAbcReferenceFlow.State.UNKNOWN, result.state());
        assertTrue(f.remote.effectKeys().contains("K1"));
        assertFalse(f.store.committedKeys().contains("K1"));
        assertTrue(result.events().stream().anyMatch(e -> e.businessKey().equals("K1") && e.state().equals("ROLLBACK")));
        assertTrue(result.events().stream().anyMatch(e -> e.businessKey().equals("K1") && e.state().equals("UNKNOWN")));
        new BatchAbcReferenceFlow.Reconciler(f.store, f.remote).reconcile("K1");
        assertFalse(f.remote.effectKeys().contains("K1"));
    }


    @Test
    void domain_c_failure_rolls_back_before_commit_without_remote_side_effect() {
        var f = fixture(1, 0); f.remote.failKey = "K1";
        var result = f.op.launch(id("TX-CFAIL", "EX1", 1), List.of(new BatchAbcReferenceFlow.Item("K1", "1")), "N1");
        assertEquals(BatchAbcReferenceFlow.State.FAILED, result.state());
        assertFalse(f.store.committedKeys().contains("K1"));
        assertFalse(f.remote.effectKeys().contains("K1"));
        assertTrue(result.events().stream().anyMatch(e -> e.businessKey().equals("K1") && e.state().equals("ROLLBACK")));
        assertTrue(result.events().stream().anyMatch(e -> e.businessKey().equals("K1") && e.state().equals("FAILED")));
    }

    @Test
    void stale_fence_and_multi_instance_competition_do_not_double_process() throws Exception {
        var f = fixture(1, 0); f.step.stealLeaseAfter = 0;
        var stale = f.op.launch(id("TX-LEASE", "EX1", 1), items(), "N1");
        assertEquals(BatchAbcReferenceFlow.State.UNKNOWN, stale.state());
        assertEquals(0, stale.committed());
        f.lease.forceExpire(); f.step.stealLeaseAfter = -1;

        try (var pool = Executors.newFixedThreadPool(2)) {
            var one = pool.submit(() -> f.op.launch(id("TX-LEASE", "EX2", 2), items(), "N2"));
            var two = pool.submit(() -> f.op.launch(id("TX-LEASE", "EX3", 3), items(), "N3"));
            var a = one.get(3, TimeUnit.SECONDS); var b = two.get(3, TimeUnit.SECONDS);
            assertTrue(a.state() == BatchAbcReferenceFlow.State.SUCCESS || b.state() == BatchAbcReferenceFlow.State.SUCCESS);
            assertTrue(a.state() == BatchAbcReferenceFlow.State.RETRYING || b.state() == BatchAbcReferenceFlow.State.RETRYING
                    || (a.state() == BatchAbcReferenceFlow.State.SUCCESS && b.state() == BatchAbcReferenceFlow.State.SUCCESS));
        }
        assertEquals(3, f.store.committedKeys().size());
        assertEquals(3, f.remote.effectKeys().size());
    }

    @Test
    void every_event_keeps_structured_transaction_job_step_execution_identity() {
        var f = fixture(2, 0); var result = f.op.launch(id("TX-ID", "EX-ID", 4), items(), "N1");
        assertEquals(BatchAbcReferenceFlow.State.SUCCESS, result.state());
        assertTrue(result.events().stream().allMatch(e -> e.transactionId().equals("TX-ID")
                && e.jobId().equals("JOB") && e.stepId().equals("STEP") && e.executionId().equals("EX-ID") && e.attempt() == 4));
    }

    private static BatchAbcReferenceFlow.Identity id(String tx, String exec, int attempt) {
        return new BatchAbcReferenceFlow.Identity(tx, "JOB", exec, "STEP", attempt, 0);
    }
    private static List<BatchAbcReferenceFlow.Item> items() {
        return List.of(new BatchAbcReferenceFlow.Item("K1", "1"), new BatchAbcReferenceFlow.Item("K2", "2"), new BatchAbcReferenceFlow.Item("K3", "3"));
    }
    private static Fixture fixture(int chunk, int retries) {
        var store = new BatchAbcReferenceFlow.Store(); var remote = new BatchAbcReferenceFlow.Remote(); var lease = new BatchAbcReferenceFlow.Lease();
        var c = new BatchAbcReferenceFlow.DomainC(remote); var b = new BatchAbcReferenceFlow.DomainB(store); var a = new BatchAbcReferenceFlow.DomainA(b, c);
        var step = new BatchAbcReferenceFlow.Step(store, a, chunk, retries); var op = new BatchAbcReferenceFlow.SchedulerOperator(new BatchAbcReferenceFlow.Job(step, lease));
        return new Fixture(store, remote, lease, b, step, op);
    }
    private record Fixture(BatchAbcReferenceFlow.Store store, BatchAbcReferenceFlow.Remote remote, BatchAbcReferenceFlow.Lease lease,
                           BatchAbcReferenceFlow.DomainB b, BatchAbcReferenceFlow.Step step, BatchAbcReferenceFlow.SchedulerOperator op) {}
}
