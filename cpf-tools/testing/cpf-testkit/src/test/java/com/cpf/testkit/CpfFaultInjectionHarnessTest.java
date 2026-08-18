package com.cpf.testkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cpf.testkit.fixture.CpfProcessKillHarness.Result;
import com.cpf.testkit.fixture.CpfProcessLifecycleFixture;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * CPF Fault Injection Testkit의 공개 Harness가 실제 실패/UNKNOWN/process-kill 시나리오를
 * 재현할 수 있는지 직접 검증합니다. 운영 JVM을 종료하지 않는 lightweight harness와
 * 별도 child JVM을 실제 kill하는 harness를 함께 검증해 false-green을 방지합니다.
 */
class CpfFaultInjectionHarnessTest {

    @Test
    void failureSwitchCoversFailureTimeoutUnknownAndRecovery() {
        CpfFailureSwitch failure = new CpfFailureSwitch();
        assertEquals(CpfFailureSwitch.Mode.HEALTHY, failure.get());

        failure.set(CpfFailureSwitch.Mode.FAILURE);
        assertThrows(IllegalStateException.class, failure::check);
        failure.set(CpfFailureSwitch.Mode.TIMEOUT);
        assertThrows(CpfFailureSwitch.CpfInjectedTimeoutException.class, failure::check);
        failure.set(CpfFailureSwitch.Mode.UNKNOWN);
        assertThrows(CpfFailureSwitch.CpfInjectedUnknownException.class, failure::check);

        failure.set(CpfFailureSwitch.Mode.HEALTHY);
        failure.check();
    }

    @Test
    void deterministicIdGeneratorIsStableAndMonotonic() {
        CpfDeterministicIdGenerator ids = new CpfDeterministicIdGenerator("TX-");
        assertEquals("TX-00000001", ids.nextId());
        assertEquals("TX-00000002", ids.nextId());
    }

    @Test
    void lightweightProcessKillHarnessSupportsKillAndRestart() throws Exception {
        CpfProcessLifecycleFixture harness = new CpfProcessLifecycleFixture();
        assertTrue(harness.alive());
        harness.kill();
        assertTrue(harness.awaitKilled(Duration.ofSeconds(1)));
        assertFalse(harness.alive());
        harness.restart();
        assertTrue(harness.alive());
    }

    @Test
    void childJvmProcessKillHarnessActuallyTerminatesProcess() throws Exception {
        Path temp = Files.createTempDirectory("cpf-testkit-kill-");
        Path source = temp.resolve("Sleeper.java");
        Files.writeString(source, "public class Sleeper{public static void main(String[]a)throws Exception{Thread.sleep(30000);}}");
        String javaHome = System.getProperty("java.home");
        String sep = java.io.File.separator;
        Process javac = new ProcessBuilder(javaHome + sep + "bin" + sep + "javac", source.toString())
                .redirectErrorStream(true).start();
        assertEquals(0, javac.waitFor());

        Result result = new com.cpf.testkit.fixture.CpfProcessKillHarness().launchAndKill(
                List.of(javaHome + sep + "bin" + sep + "java", "-cp", temp.toString(), "Sleeper"),
                Duration.ofMillis(150), Duration.ofSeconds(3));
        assertTrue(result.aliveBeforeKill());
        assertTrue(result.terminated());
    }
}
