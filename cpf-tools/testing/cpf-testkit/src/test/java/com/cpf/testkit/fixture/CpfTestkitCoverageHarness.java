package com.cpf.testkit.fixture;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CpfTestkitCoverageHarness {
    public static void main(String[] args) throws Exception {
        CpfDeterministicClock clock = CpfDeterministicClock.utc(Instant.parse("2026-08-10T00:00:00Z"));
        require(clock.instant().equals(Instant.parse("2026-08-10T00:00:00Z")), "clock initial");
        clock.advance(Duration.ofSeconds(30));
        require(clock.instant().equals(Instant.parse("2026-08-10T00:00:30Z")), "clock advance");
        require(clock.withZone(ZoneId.of("Asia/Seoul")).instant().equals(clock.instant()), "clock zone");

        CpfDeterministicIdSource ids = new CpfDeterministicIdSource("tx-", 7);
        require(ids.get().equals("tx-7") && ids.get().equals("tx-8"), "deterministic ids");

        CpfFailureInjector failure = new CpfFailureInjector();
        failure.failNext("payment");
        require(failure.consume("payment") == CpfFailureInjector.Mode.FAIL, "fail next");
        require(failure.consume("payment") == CpfFailureInjector.Mode.SUCCESS, "one shot reset");
        failure.unknownNext("payment");
        require(failure.consume("payment") == CpfFailureInjector.Mode.UNKNOWN, "unknown next");

        CpfProtocolProbe probe = new CpfProtocolProbe();
        for (CpfProtocolProbe.Channel c : CpfProtocolProbe.Channel.values()) {
            probe.record(new CpfProtocolProbe.Event(c, "op-" + c.name(), "SUCCESS", "tx-9", "i-1",
                    clock.instant(), Map.of("tenantId", "tenant-a")));
        }
        require(probe.snapshot().size() == 11, "all protocol fixtures");
        require(probe.byChannel(CpfProtocolProbe.Channel.OUTBOX).size() == 1, "outbox probe");

        CpfMultiInstanceHarness<String> multi = new CpfMultiInstanceHarness<>();
        List<String> seen = new ArrayList<>();
        multi.register("i-2", e -> seen.add("i-2:" + e));
        multi.register("i-1", e -> seen.add("i-1:" + e));
        require(multi.broadcast("reconcile").equals(List.of("i-1", "i-2")), "deterministic broadcast order");
        require(seen.size() == 2, "multi instance delivery");
        boolean duplicate = false;
        try { multi.register("i-1", e -> {}); } catch (IllegalStateException expected) { duplicate = true; }
        require(duplicate, "duplicate instance fail closed");

        String javaCmd = System.getProperty("java.home") + java.io.File.separator + "bin" + java.io.File.separator + "java";
        java.nio.file.Path temp = java.nio.file.Files.createTempDirectory("cpf-kill-");
        java.nio.file.Path src = temp.resolve("Sleeper.java");
        java.nio.file.Files.writeString(src, "public class Sleeper{public static void main(String[]a)throws Exception{Thread.sleep(30000);}}");
        Process javac = new ProcessBuilder(System.getProperty("java.home") + java.io.File.separator + "bin" + java.io.File.separator + "javac", src.toString()).inheritIO().start();
        require(javac.waitFor() == 0, "sleeper compile");
        CpfProcessKillHarness.Result sleeper = new CpfProcessKillHarness().launchAndKill(
                List.of(javaCmd, "-cp", temp.toString(), "Sleeper"), Duration.ofMillis(150), Duration.ofSeconds(3));
        require(sleeper.aliveBeforeKill(), "process alive before kill");
        require(sleeper.terminated(), "process kill terminated");
        System.out.println("CPF_TESTKIT_COVERAGE=PASS channels=11 multiInstance=2 processKill=true deterministicClock=true deterministicId=true failureInjection=true");
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
