package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.api.runtimecontrol.CpfRuntimePayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfRuntimeInstanceInboxStoreTest {

    @Test
    void rejectsSameDeliveryIdWithDifferentDurableIdentity(@TempDir Path tempDir) {
        CpfRuntimeInstanceInboxStore store = new CpfRuntimeInstanceInboxStore(tempDir.resolve("inbox"));
        CpfRuntimeDelivery original = delivery("delivery-1", "change-1", 1L, "hash-1");
        store.prepare(original);

        CpfRuntimeDelivery conflicting = delivery("delivery-1", "change-2", 1L, "hash-2");

        assertThrows(CpfRuntimeInstanceInboxStore.IdentityConflictException.class,
                () -> store.find(conflicting));
        assertEquals("change-1", store.find("delivery-1").orElseThrow().changeId());
    }

    @Test
    void appliedJournalCannotBeOverwrittenWithDifferentActualHash(@TempDir Path tempDir) {
        CpfRuntimeInstanceInboxStore store = new CpfRuntimeInstanceInboxStore(tempDir.resolve("inbox"));
        CpfRuntimeDelivery delivery = delivery("delivery-1", "change-1", 1L, "hash-1");
        store.prepare(delivery);
        store.markApplied(delivery, "actual-1");

        assertThrows(CpfRuntimeInstanceInboxStore.IdentityConflictException.class,
                () -> store.markApplied(delivery, "actual-2"));
        assertEquals("actual-1", store.find(delivery).orElseThrow().actualHash());
    }


    @Test
    void separateProcessStoresUseIndependentTemporaryFiles(@TempDir Path tempDir) throws Exception {
        Path inbox = tempDir.resolve("inbox");
        CpfRuntimeInstanceInboxStore first = new CpfRuntimeInstanceInboxStore(inbox);
        CpfRuntimeInstanceInboxStore second = new CpfRuntimeInstanceInboxStore(inbox);
        CpfRuntimeDelivery one = delivery("delivery-one", "change-one", 1L, "payload-one");
        CpfRuntimeDelivery two = delivery("delivery-two", "change-two", 2L, "payload-two");

        Thread a = new Thread(() -> first.prepare(one));
        Thread b = new Thread(() -> second.prepare(two));
        a.start();
        b.start();
        a.join();
        b.join();

        assertTrue(first.find(one).isPresent());
        assertTrue(second.find(two).isPresent());
        try (var files = java.nio.file.Files.list(inbox)) {
            assertEquals(0L, files.filter(path -> path.getFileName().toString().endsWith(".tmp")).count());
        }
    }

    @Test
    void latestAppliedStatesReadsPersistedJournalPathWithoutReencodingDeliveryId(@TempDir Path tempDir) {
        Path inbox = tempDir.resolve("inbox");
        CpfRuntimeInstanceInboxStore firstProcess = new CpfRuntimeInstanceInboxStore(inbox);
        CpfRuntimeDelivery older = delivery("delivery:older", "change-1", 1L, "hash-1");
        CpfRuntimeDelivery newer = delivery("delivery:newer", "change-2", 2L, "hash-2");
        firstProcess.prepare(older);
        firstProcess.markApplied(older, "actual-1");
        firstProcess.prepare(newer);
        firstProcess.markApplied(newer, "actual-2");

        CpfRuntimeInstanceInboxStore restartedProcess = new CpfRuntimeInstanceInboxStore(inbox);

        assertEquals(1, restartedProcess.latestAppliedStates().size());
        assertEquals(2L, restartedProcess.latestAppliedStates().getFirst().actualVersion());
        assertEquals("actual-2", restartedProcess.latestAppliedStates().getFirst().actualHash());
        assertEquals("delivery:newer", restartedProcess.latestAppliedStates().getFirst().sourceDeliveryId());
    }


    @Test
    void oversizedAppliedHashCannotReplacePreparedEvidence(@TempDir Path tempDir) {
        CpfRuntimeInstanceInboxStore store = new CpfRuntimeInstanceInboxStore(tempDir.resolve("inbox"));
        CpfRuntimeDelivery delivery = delivery("delivery-1", "change-1", 1L, "hash-1");
        store.prepare(delivery);

        assertThrows(IllegalArgumentException.class,
                () -> store.markApplied(delivery, "h".repeat(65)));
        assertEquals(CpfRuntimeInstanceInboxStore.State.PREPARED,
                store.find(delivery).orElseThrow().state());
    }

    @Test
    void corruptedAppliedJournalFailsClosedDuringRestartReconciliation(@TempDir Path tempDir) throws Exception {
        Path inbox = tempDir.resolve("inbox");
        CpfRuntimeInstanceInboxStore store = new CpfRuntimeInstanceInboxStore(inbox);
        CpfRuntimeDelivery delivery = delivery("delivery-1", "change-1", 1L, "hash-1");
        store.prepare(delivery);
        store.markApplied(delivery, "actual-1");

        Path journal;
        try (var files = java.nio.file.Files.list(inbox)) {
            journal = files.filter(path -> path.getFileName().toString().endsWith(".inbox"))
                    .findFirst().orElseThrow();
        }
        java.util.Properties properties = new java.util.Properties();
        try (var input = java.nio.file.Files.newInputStream(journal)) {
            properties.load(input);
        }
        properties.setProperty("actualHash", "h".repeat(65));
        try (var output = java.nio.file.Files.newOutputStream(journal)) {
            properties.store(output, "corrupt fixture");
        }

        CpfRuntimeInstanceInboxStore restarted = new CpfRuntimeInstanceInboxStore(inbox);
        assertThrows(IllegalStateException.class, restarted::latestAppliedStates);
    }

    @Test
    void appliedRequiresPreparedJournal(@TempDir Path tempDir) {
        CpfRuntimeInstanceInboxStore store = new CpfRuntimeInstanceInboxStore(tempDir.resolve("inbox"));
        CpfRuntimeDelivery delivery = delivery("delivery-1", "change-1", 1L, "hash-1");

        assertThrows(IllegalStateException.class, () -> store.markApplied(delivery, "actual-1"));
    }

    private static CpfRuntimeDelivery delivery(
            String deliveryId, String changeId, long desiredVersion, String payloadHash) {
        return new CpfRuntimeDelivery(
                deliveryId, changeId, "TEST", "instance-1", desiredVersion, 7L,
                "request-hash", payloadHash, 1, CpfRuntimePayload.empty(), 1,
                Instant.now().plusSeconds(60));
    }
}
