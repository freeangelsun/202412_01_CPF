package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeAck;
import com.cpf.core.api.runtimecontrol.CpfRuntimeAckState;
import com.cpf.core.api.runtimecontrol.CpfRuntimeActualState;
import com.cpf.core.api.runtimecontrol.CpfRuntimeAgentPort;
import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.api.runtimecontrol.CpfRuntimeInstanceLease;
import com.cpf.core.api.runtimecontrol.CpfRuntimeInstanceRegistration;
import com.cpf.core.api.runtimecontrol.CpfRuntimePayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfRuntimeControlAgentApplyGuardTest {

    @Test
    void timeoutIsAcknowledgedUnknownAndPreparedJournalIsRetained(@TempDir Path tempDir) {
        CpfRuntimeDelivery delivery = delivery("timeout-1", Instant.now().plusSeconds(5));
        FakeControlPlane controlPlane = new FakeControlPlane(delivery);
        CpfRuntimeInstanceInboxStore inbox = new CpfRuntimeInstanceInboxStore(tempDir.resolve("inbox"));
        CpfRuntimeApplyGuard guard = guard(30L, 3);
        CpfRuntimeControlAgent agent = new CpfRuntimeControlAgent(
                controlPlane, registration(), List.of(slowApplier()), inbox, guard);

        agent.start();
        agent.poll();
        agent.stop();

        CpfRuntimeAck ack = controlPlane.ack.get();
        assertEquals(CpfRuntimeAckState.UNKNOWN_RESULT.name(), ack.state());
        assertEquals("APPLY_TIMEOUT_UNKNOWN", ack.errorCode());
        assertEquals(CpfRuntimeInstanceInboxStore.State.PREPARED,
                inbox.find(delivery.deliveryId()).orElseThrow().state());
    }

    @Test
    void preexistingPreparedJournalSurvivesSafeGuardRejection(@TempDir Path tempDir) {
        CpfRuntimeDelivery delivery = delivery("expired-replay-1", Instant.now().minusSeconds(1));
        FakeControlPlane controlPlane = new FakeControlPlane(delivery);
        CpfRuntimeInstanceInboxStore inbox = new CpfRuntimeInstanceInboxStore(tempDir.resolve("inbox"));
        inbox.prepare(delivery);
        CpfRuntimeControlAgent agent = new CpfRuntimeControlAgent(
                controlPlane, registration(), List.of(successApplier()), inbox, guard(100L, 1));

        agent.start();
        agent.poll();
        agent.stop();

        CpfRuntimeAck ack = controlPlane.ack.get();
        assertEquals(CpfRuntimeAckState.FAILED.name(), ack.state());
        assertEquals("DELIVERY_EXPIRED", ack.errorCode());
        assertEquals(CpfRuntimeInstanceInboxStore.State.PREPARED,
                inbox.find(delivery.deliveryId()).orElseThrow().state());
    }

    private static CpfRuntimeApplyGuard guard(long timeoutMillis, int threshold) {
        return new CpfRuntimeApplyGuard(new CpfRuntimeApplyGuard.Policy(
                timeoutMillis, 1, 0L, 0L, 0, 1, threshold, 1_000L));
    }

    private static CpfRuntimeChangeApplier slowApplier() {
        return new CpfRuntimeChangeApplier() {
            @Override public String changeType() { return "TEST"; }
            @Override public boolean supportsIdempotentReplay() { return true; }
            @Override public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
                try {
                    Thread.sleep(1_000L);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
                return CpfRuntimeApplyResult.success("late-hash");
            }
        };
    }

    private static CpfRuntimeChangeApplier successApplier() {
        return new CpfRuntimeChangeApplier() {
            @Override public String changeType() { return "TEST"; }
            @Override public boolean supportsIdempotentReplay() { return true; }
            @Override public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
                return CpfRuntimeApplyResult.success("actual-hash");
            }
        };
    }

    private static CpfRuntimeDelivery delivery(String id, Instant expiresAt) {
        CpfRuntimePayload payload = CpfRuntimePayload.empty();
        return new CpfRuntimeDelivery(
                id, "change-1", "TEST", "instance-1", 1L, 7L,
                "request-hash", CpfRuntimeCanonicalHash.sha256(payload), 1, payload, 0, expiresAt);
    }

    private static CpfRuntimeInstanceRegistration registration() {
        return new CpfRuntimeInstanceRegistration(
                "instance-1", "service-1", "endpoint-1", "test", "zone-1", "cell-1",
                "http://localhost", "test", "commit", "APPLICATION", "SELF", "1", "config",
                Map.of("TEST", "1"), Map.of(), Instant.now(), 60);
    }

    private static final class FakeControlPlane implements CpfRuntimeAgentPort {
        private final CpfRuntimeDelivery delivery;
        private final AtomicReference<CpfRuntimeAck> ack = new AtomicReference<>();
        private boolean claimed;

        private FakeControlPlane(CpfRuntimeDelivery delivery) {
            this.delivery = delivery;
        }

        @Override
        public CpfRuntimeInstanceLease register(CpfRuntimeInstanceRegistration registration) {
            return new CpfRuntimeInstanceLease(
                    registration.instanceId(), 7L, 1L, 0L, "desired", "actual", "IN_SYNC",
                    Instant.now().plusSeconds(60));
        }

        @Override
        public CpfRuntimeInstanceLease heartbeat(
                String instanceId, long fencingToken, String actualHash, long actualVersion) {
            return new CpfRuntimeInstanceLease(
                    instanceId, fencingToken, 1L, actualVersion, "desired", actualHash, "IN_SYNC",
                    Instant.now().plusSeconds(60));
        }

        @Override
        public List<CpfRuntimeDelivery> claim(String instanceId, long fencingToken, int limit) {
            if (claimed) return List.of();
            claimed = true;
            return List.of(delivery);
        }

        @Override
        public CpfRuntimeChangeResult acknowledge(CpfRuntimeAck ack) {
            this.ack.set(ack);
            return null;
        }

        @Override
        public void reconcileActualState(
                String instanceId, long fencingToken, List<CpfRuntimeActualState> states) {
            // no-op test adapter
        }
    }
}
