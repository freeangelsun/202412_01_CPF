package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeAck;
import com.cpf.core.api.runtimecontrol.CpfRuntimeAckState;
import com.cpf.core.api.runtimecontrol.CpfRuntimeActualState;
import com.cpf.core.api.runtimecontrol.CpfRuntimeAgentPort;
import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException;
import com.cpf.core.api.runtimecontrol.CpfRuntimeInstanceLease;
import com.cpf.core.api.runtimecontrol.CpfRuntimeInstanceRegistration;
import com.cpf.core.api.runtimecontrol.CpfRuntimePayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfRuntimeControlAgentApplyGuardTest {

    @Test
    void successfulAckClearsAppliedRecoveryJournal(@TempDir Path tempDir) {
        CpfRuntimeDelivery delivery = delivery("acked-cleanup-1", Instant.now().plusSeconds(5));
        FakeControlPlane controlPlane = new FakeControlPlane(delivery);
        CpfRuntimeInstanceInboxStore inbox = new CpfRuntimeInstanceInboxStore(tempDir.resolve("acked-cleanup"));
        CpfRuntimeControlAgent agent = new CpfRuntimeControlAgent(
                controlPlane, registration(), List.of(successApplier()), inbox, guard(100L, 1));

        agent.start();
        agent.poll();
        agent.stop();

        assertEquals(CpfRuntimeAckState.SUCCESS.name(), controlPlane.ack.get().state());
        assertTrue(inbox.find(delivery.deliveryId()).isEmpty());
    }

    @Test
    void failedAckTransportRetainsAppliedRecoveryJournal(@TempDir Path tempDir) {
        CpfRuntimeDelivery delivery = delivery("acked-retain-1", Instant.now().plusSeconds(5));
        AtomicReference<CpfRuntimeAck> attemptedAck = new AtomicReference<>();
        CpfRuntimeAgentPort controlPlane = new CpfRuntimeAgentPort() {
            private boolean claimed;
            @Override public CpfRuntimeInstanceLease register(CpfRuntimeInstanceRegistration value) {
                return new CpfRuntimeInstanceLease(value.instanceId(),7L,1L,0L,"desired","actual","IN_SYNC",Instant.now().plusSeconds(60));
            }
            @Override public CpfRuntimeInstanceLease heartbeat(String instanceId,long fence,String hash,long version) {
                return new CpfRuntimeInstanceLease(instanceId,fence,1L,version,"desired",hash,"IN_SYNC",Instant.now().plusSeconds(60));
            }
            @Override public List<CpfRuntimeDelivery> claim(String instanceId,long fence,int limit) {
                if (claimed) return List.of(); claimed=true; return List.of(delivery);
            }
            @Override public CpfRuntimeChangeResult acknowledge(CpfRuntimeAck ack) {
                attemptedAck.set(ack); throw new IllegalStateException("response lost");
            }
        };
        CpfRuntimeInstanceInboxStore inbox = new CpfRuntimeInstanceInboxStore(tempDir.resolve("acked-retain"));
        CpfRuntimeControlAgent agent = new CpfRuntimeControlAgent(
                controlPlane, registration(), List.of(successApplier()), inbox, guard(100L, 1));

        agent.start();
        agent.poll();
        agent.stop();

        assertEquals(CpfRuntimeAckState.SUCCESS.name(), attemptedAck.get().state());
        assertEquals(CpfRuntimeInstanceInboxStore.State.APPLIED,
                inbox.find(delivery.deliveryId()).orElseThrow().state());
    }

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


    @Test
    void conflictingDurableInboxIdentityIsAcknowledgedUnknownWithoutApplying(@TempDir Path tempDir) {
        CpfRuntimeDelivery delivery = delivery("conflict-1", Instant.now().plusSeconds(5));
        FakeControlPlane controlPlane = new FakeControlPlane(delivery);
        CpfRuntimeInstanceInboxStore inbox = new CpfRuntimeInstanceInboxStore(tempDir.resolve("inbox"));
        CpfRuntimeDelivery conflicting = new CpfRuntimeDelivery(
                delivery.deliveryId(), "other-change", delivery.changeType(), delivery.instanceId(),
                delivery.desiredVersion(), delivery.fencingToken(), delivery.requestHash(), "other-payload-hash",
                delivery.payloadSchemaVersion(), delivery.payload(), delivery.attempt(), delivery.expiresAt());
        inbox.prepare(conflicting);
        AtomicInteger calls = new AtomicInteger();
        CpfRuntimeChangeApplier applier = new CpfRuntimeChangeApplier() {
            @Override public String changeType() { return "TEST"; }
            @Override public boolean supportsIdempotentReplay() { return true; }
            @Override public CpfRuntimeApplyResult apply(CpfRuntimeDelivery ignored) {
                calls.incrementAndGet();
                return CpfRuntimeApplyResult.success("unexpected");
            }
        };
        CpfRuntimeControlAgent agent = new CpfRuntimeControlAgent(
                controlPlane, registration(), List.of(applier), inbox, guard(100L, 1));

        agent.start();
        agent.poll();
        agent.stop();

        CpfRuntimeAck ack = controlPlane.ack.get();
        assertEquals(CpfRuntimeAckState.UNKNOWN_RESULT.name(), ack.state());
        assertEquals("INBOX_IDENTITY_CONFLICT", ack.errorCode());
        assertEquals(0, calls.get());
        assertEquals("other-change", inbox.find(delivery.deliveryId()).orElseThrow().changeId());
    }


    @Test
    void appliedJournalPersistenceFailureIsAcknowledgedUnknown(@TempDir Path tempDir) {
        CpfRuntimeDelivery delivery = delivery("persist-unknown-1", Instant.now().plusSeconds(5));
        FakeControlPlane controlPlane = new FakeControlPlane(delivery);
        Path inboxPath = tempDir.resolve("inbox-persist-failure");
        CpfRuntimeInstanceInboxStore inbox = new CpfRuntimeInstanceInboxStore(inboxPath);
        CpfRuntimeChangeApplier applier = new CpfRuntimeChangeApplier() {
            @Override public String changeType() { return "TEST"; }
            @Override public boolean supportsIdempotentReplay() { return true; }
            @Override public CpfRuntimeApplyResult apply(CpfRuntimeDelivery ignored) {
                try (var paths = java.nio.file.Files.walk(inboxPath)) {
                    paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                        try { java.nio.file.Files.deleteIfExists(path); }
                        catch (java.io.IOException ex) { throw new IllegalStateException(ex); }
                    });
                } catch (java.io.IOException ex) {
                    throw new IllegalStateException(ex);
                }
                return CpfRuntimeApplyResult.success("actual-persist-unknown");
            }
        };
        CpfRuntimeControlAgent agent = new CpfRuntimeControlAgent(
                controlPlane, registration(), List.of(applier), inbox, guard(100L, 1));

        agent.start();
        agent.poll();
        agent.stop();

        CpfRuntimeAck ack = controlPlane.ack.get();
        assertEquals(CpfRuntimeAckState.UNKNOWN_RESULT.name(), ack.state());
        assertEquals("INBOX_APPLIED_PERSIST_UNKNOWN", ack.errorCode());
        assertEquals("actual-persist-unknown", ack.actualHash());
    }

    @Test
    void fencingReregistrationReconcilesDurableInboxAgain(@TempDir Path tempDir) {
        CpfRuntimeDelivery delivery = delivery("recovery-1", Instant.now().plusSeconds(5));
        CpfRuntimeInstanceInboxStore inbox = new CpfRuntimeInstanceInboxStore(tempDir.resolve("inbox"));
        inbox.prepare(delivery);
        inbox.markApplied(delivery, "actual-hash");
        AtomicInteger registrations = new AtomicInteger();
        AtomicInteger reconciliations = new AtomicInteger();
        CpfRuntimeAgentPort controlPlane = new CpfRuntimeAgentPort() {
            @Override
            public CpfRuntimeInstanceLease register(CpfRuntimeInstanceRegistration registration) {
                long fence = registrations.incrementAndGet();
                return new CpfRuntimeInstanceLease(registration.instanceId(), fence, 1L, 0L,
                        "desired", "actual", "DRIFT", Instant.now().plusSeconds(60));
            }

            @Override
            public CpfRuntimeInstanceLease heartbeat(
                    String instanceId, long fencingToken, String actualHash, long actualVersion) {
                throw new CpfRuntimeFenceException("stale fence");
            }

            @Override
            public List<CpfRuntimeDelivery> claim(String instanceId, long fencingToken, int limit) {
                return List.of();
            }

            @Override
            public CpfRuntimeChangeResult acknowledge(CpfRuntimeAck ack) {
                return null;
            }

            @Override
            public void reconcileActualState(
                    String instanceId, long fencingToken, List<CpfRuntimeActualState> states) {
                reconciliations.incrementAndGet();
                assertEquals(1, states.size());
                assertEquals("recovery-1", states.getFirst().sourceDeliveryId());
            }
        };
        CpfRuntimeControlAgent agent = new CpfRuntimeControlAgent(
                controlPlane, registration(), List.of(successApplier()), inbox, guard(100L, 1));

        agent.start();
        agent.poll();
        agent.stop();

        assertEquals(2, registrations.get());
        assertEquals(2, reconciliations.get());
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
