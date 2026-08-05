package com.cpf.core.common.logging.file;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

public final class CpfFileLogCrossProcessHarness {
    private CpfFileLogCrossProcessHarness() {}

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("cpf-filelog-process-");
        Path log = root.resolve("test/cross-process/shared.log");
        Files.createDirectories(log.getParent());
        Path gzip = log.resolveSibling(log.getFileName() + ".gz");
        try (OutputStream output = new GZIPOutputStream(Files.newOutputStream(gzip))) {
            output.write("legacy-line\n".getBytes(StandardCharsets.UTF_8));
        }

        int writesPerProcess = 120;
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classpath = System.getProperty("java.class.path");
        Process left = new ProcessBuilder(
                java, "-cp", classpath,
                CpfFileLogProcessWorker.class.getName(), root.toString(), "left",
                Integer.toString(writesPerProcess)).redirectErrorStream(true).start();
        Process right = new ProcessBuilder(
                java, "-cp", classpath,
                CpfFileLogProcessWorker.class.getName(), root.toString(), "right",
                Integer.toString(writesPerProcess)).redirectErrorStream(true).start();
        if (!left.waitFor(30, TimeUnit.SECONDS) || !right.waitFor(30, TimeUnit.SECONDS)) {
            left.destroyForcibly();
            right.destroyForcibly();
            throw new AssertionError("child process timed out");
        }
        String leftOutput = new String(left.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String rightOutput = new String(right.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (left.exitValue() != 0 || right.exitValue() != 0) {
            throw new AssertionError("child failed left=" + left.exitValue() + " " + leftOutput
                    + " right=" + right.exitValue() + " " + rightOutput);
        }
        if (!Files.isRegularFile(log)) throw new AssertionError("restored log missing");
        if (Files.exists(gzip)) throw new AssertionError("archive should be consumed exactly once");
        List<String> lines = Files.readAllLines(log, StandardCharsets.UTF_8);
        long legacy = lines.stream().filter("legacy-line"::equals).count();
        long events = lines.stream().filter(line -> line.contains("worker=")).count();
        if (legacy != 1L) throw new AssertionError("legacy content count=" + legacy);
        if (events != writesPerProcess * 2L) {
            throw new AssertionError("cross-process append loss=" + events + "/" + (writesPerProcess * 2L));
        }
        System.out.println("CPF_FILE_LOG_CROSS_PROCESS_HARNESS_PASS");
    }
}
