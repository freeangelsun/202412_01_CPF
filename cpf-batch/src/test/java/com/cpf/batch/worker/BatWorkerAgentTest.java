package com.cpf.batch.worker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class BatWorkerAgentTest {
    private final List<BatWorkerAgent> agents = new ArrayList<>();

    @AfterEach
    void stopAgents() {
        agents.forEach(BatWorkerAgent::stop);
    }

    @Test
    void twoWorkersCannotClaimSameExecution() throws Exception {
        SharedLeaseStore store = new SharedLeaseStore();
        CountDownLatch dispatched = new CountDownLatch(1);
        BatWorkerJobDispatcher dispatcher = lease -> {
            dispatched.countDown();
            return BatWorkerJobDispatcher.DispatchResult.completed(100L);
        };
        BatWorkerAgent first = agent("worker-1", store, dispatcher);
        BatWorkerAgent second = agent("worker-2", store, dispatcher);

        // ApplicationReadyEvent 이전 scheduled 호출이 executor를 종료하지 않아야 합니다.
        first.heartbeat();
        first.poll();
        first.start();
        second.start();
        first.poll();
        second.poll();

        assertThat(dispatched.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(store.completed.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(store.claimCount.get()).isEqualTo(1);
        assertThat(store.completeCount.get()).isEqualTo(1);
        assertThat(store.claimedWorker).isIn("worker-1", "worker-2");
    }

    @Test
    void drainingWorkerDoesNotClaimNewExecution() {
        SharedLeaseStore store = new SharedLeaseStore();
        store.controlStatus = "DRAINING";
        BatWorkerAgent worker = agent(
                "worker-drain",
                store,
                lease -> BatWorkerJobDispatcher.DispatchResult.completed(101L));

        worker.start();
        worker.poll();

        assertThat(worker.snapshot().controlStatus()).isEqualTo("DRAINING");
        assertThat(store.claimCount.get()).isZero();
    }

    private BatWorkerAgent agent(
            String workerId,
            BatWorkerLeaseStore store,
            BatWorkerJobDispatcher dispatcher) {
        BatWorkerProperties properties = new BatWorkerProperties(
                true, workerId, "1.0.0", Set.of("SMOKE"),
                1, 1, 100, 1_000, 5, true);
        BatWorkerAgent agent = new BatWorkerAgent(properties, store, dispatcher);
        agents.add(agent);
        return agent;
    }

    private static final class SharedLeaseStore implements BatWorkerLeaseStore {
        private final AtomicBoolean available = new AtomicBoolean(true);
        private final AtomicInteger claimCount = new AtomicInteger();
        private final AtomicInteger completeCount = new AtomicInteger();
        private final CountDownLatch completed = new CountDownLatch(1);
        private volatile String controlStatus = "RUNNING";
        private volatile String claimedWorker;

        @Override
        public void register(BatWorkerIdentity identity, BatWorkerProperties properties) {
        }

        @Override
        public String heartbeat(BatWorkerIdentity identity, String workerStatus, BatWorkerLease currentLease) {
            return controlStatus;
        }

        @Override
        public int recoverExpiredLeases(String requestUser) {
            return 0;
        }

        @Override
        public Optional<BatWorkerLease> claim(BatWorkerIdentity identity, BatWorkerProperties properties) {
            if (!available.compareAndSet(true, false)) {
                return Optional.empty();
            }
            claimedWorker = identity.workerId();
            claimCount.incrementAndGet();
            return Optional.of(new BatWorkerLease(
                    1L, "CPF_BAT_SMOKE_JOB", "{}", "token", identity.workerId(),
                    Instant.now().plusSeconds(5), 1, 0));
        }

        @Override
        public boolean markRunning(BatWorkerLease lease) {
            return true;
        }

        @Override
        public boolean renew(BatWorkerLease lease, int leaseSeconds) {
            return true;
        }

        @Override
        public boolean complete(BatWorkerLease lease, String executionStatus, Long springBatchExecutionId, String failureMessage) {
            completeCount.incrementAndGet();
            completed.countDown();
            return true;
        }

        @Override
        public void markStopped(BatWorkerIdentity identity, String workerStatus) {
        }
    }
}
