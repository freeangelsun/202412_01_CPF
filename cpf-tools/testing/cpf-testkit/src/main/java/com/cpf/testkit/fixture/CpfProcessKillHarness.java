package com.cpf.testkit.fixture;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/** 별도 JVM/worker process를 실제 종료해 recovery 검증에 사용하는 harness. */
public final class CpfProcessKillHarness {
    public record Result(long pid, boolean aliveBeforeKill, boolean terminated, int exitValue) { }

    public Result launchAndKill(List<String> command, Duration startupWait, Duration killWait)
            throws IOException, InterruptedException {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(startupWait, "startupWait");
        Objects.requireNonNull(killWait, "killWait");
        if (command.isEmpty()) throw new IllegalArgumentException("command must not be empty");
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        Thread.sleep(Math.max(0L, startupWait.toMillis()));
        boolean alive = process.isAlive();
        if (alive) process.destroyForcibly();
        boolean terminated = process.waitFor(Math.max(1L, killWait.toMillis()), TimeUnit.MILLISECONDS);
        int exit = terminated ? process.exitValue() : Integer.MIN_VALUE;
        if (!terminated) process.destroyForcibly();
        return new Result(process.pid(), alive, terminated, exit);
    }
}
