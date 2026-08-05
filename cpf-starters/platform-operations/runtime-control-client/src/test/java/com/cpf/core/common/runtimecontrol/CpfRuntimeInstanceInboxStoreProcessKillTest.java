package com.cpf.core.common.runtimecontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.api.runtimecontrol.CpfRuntimePayload;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CpfRuntimeInstanceInboxStoreProcessKillTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void preparedJournalSurvivesAbruptJvmTerminationAndSupportsReplayRecovery() throws Exception {
        Path inboxDirectory = temporaryDirectory.resolve("instance-1");
        Process child = new ProcessBuilder(
                javaExecutable(),
                "-cp",
                System.getProperty("java.class.path"),
                CrashAfterPrepare.class.getName(),
                inboxDirectory.toString())
                .redirectErrorStream(true)
                .start();

        assertTrue(child.waitFor(20, TimeUnit.SECONDS), "child JVM did not terminate");
        assertEquals(23, child.exitValue(), "child JVM must terminate after durable PREPARED");

        CpfRuntimeDelivery delivery = delivery();
        CpfRuntimeInstanceInboxStore restarted = new CpfRuntimeInstanceInboxStore(inboxDirectory);
        CpfRuntimeInstanceInboxStore.Entry prepared = restarted.find(delivery).orElseThrow();
        assertEquals(CpfRuntimeInstanceInboxStore.State.PREPARED, prepared.state());

        // Idempotent replay는 같은 identity의 PREPARED를 재사용하고 성공 상태를 durable APPLIED로 승격합니다.
        assertEquals(prepared, restarted.prepare(delivery));
        restarted.markApplied(delivery, delivery.payloadHash());

        CpfRuntimeInstanceInboxStore secondRestart = new CpfRuntimeInstanceInboxStore(inboxDirectory);
        assertEquals(1, secondRestart.latestAppliedStates().size());
        assertEquals(delivery.payloadHash(), secondRestart.latestAppliedStates().getFirst().actualHash());

        // 서버 ACK가 확정된 후에만 journal을 제거하여 response-loss 재전송 근거를 보존합니다.
        secondRestart.clearApplied(delivery);
        assertTrue(secondRestart.find(delivery).isEmpty());
    }

    private static String javaExecutable() {
        String executable = System.getProperty("os.name", "")
                .toLowerCase(java.util.Locale.ROOT)
                .contains("win") ? "java.exe" : "java";
        return Path.of(System.getProperty("java.home"), "bin", executable).toString();
    }

    private static CpfRuntimeDelivery delivery() {
        CpfRuntimePayload payload = CpfRuntimePayload.parse("{\"enabled\":true}");
        String payloadHash = CpfRuntimeCanonicalHash.sha256(payload);
        return new CpfRuntimeDelivery(
                "delivery-process-kill",
                "change-process-kill",
                "CONFIG_PARAMETER_FEATURE_FLAG",
                "instance-1",
                7L,
                11L,
                "request-process-kill",
                payloadHash,
                1,
                payload,
                0,
                Instant.now().plusSeconds(300));
    }

    /** 별도 JVM에서 fsync된 PREPARED를 남긴 직후 shutdown hook 없이 강제 종료합니다. */
    public static final class CrashAfterPrepare {
        private CrashAfterPrepare() {}

        public static void main(String[] args) {
            CpfRuntimeInstanceInboxStore store = new CpfRuntimeInstanceInboxStore(Path.of(args[0]));
            store.prepare(delivery());
            Runtime.getRuntime().halt(23);
        }
    }
}
