package com.cpf.batch.agent.internal;

import com.cpf.batch.agent.AgentProperties;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Executes only pre-approved service-manager commands and preserves indeterminate outcomes. */
public final class ServiceManager {
    private static final long TERMINATION_GRACE_MILLIS = 5_000L;

    private final AgentProperties properties;

    public ServiceManager(AgentProperties properties) {
        this.properties = properties;
    }

    public Result execute(String serviceId, Action action) throws Exception {
        AgentProperties.ServiceDefinition service = service(serviceId);
        List<String> command = command(service, action);
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (ExecutorService ioExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> drain = ioExecutor.submit(
                    () -> copyBounded(process.getInputStream(), output, properties.getMaxProcessOutputBytes()));
            boolean finished = process.waitFor(
                    Math.max(1L, properties.getProcessTimeoutSeconds()), TimeUnit.SECONDS);
            if (!finished) {
                boolean terminated = terminateTree(process);
                drain.cancel(true);
                return Result.unknown(
                        -1,
                        terminated ? "PROCESS_TIMEOUT" : "PROCESS_TIMEOUT_TERMINATION_UNCONFIRMED");
            }

            try {
                drain.get(5L, TimeUnit.SECONDS);
            } catch (ExecutionException failure) {
                return Result.unknown(process.exitValue(), message(failure.getCause()));
            } catch (TimeoutException failure) {
                drain.cancel(true);
                return Result.unknown(process.exitValue(), "PROCESS_OUTPUT_DRAIN_TIMEOUT");
            }

            String text = sanitize(output.toString(StandardCharsets.UTF_8));
            return new Result(process.exitValue() == 0, process.exitValue(), text, false);
        }
    }

    public ServiceState state(String serviceId) throws Exception {
        return classifyStatus(execute(serviceId, Action.STATUS));
    }

    public boolean stopped(String serviceId) throws Exception {
        Result result = execute(serviceId, Action.STATUS);
        if (result.unknownResult()) {
            throw new IllegalStateException("SERVICE_STATUS_RESULT_UNKNOWN");
        }
        return classifyStatus(result) == ServiceState.STOPPED;
    }

    static ServiceState classifyStatus(Result result) {
        if (result == null || result.unknownResult()) {
            return ServiceState.UNKNOWN;
        }
        String output = result.output().toLowerCase(Locale.ROOT);
        if ((!result.success() && Set.of(3, 4).contains(result.exitCode()))
                || output.contains("inactive")
                || output.contains("stopped")
                || output.contains("dead")) {
            return ServiceState.STOPPED;
        }
        if (result.success()) {
            return ServiceState.RUNNING;
        }
        return ServiceState.UNKNOWN;
    }

    private List<String> command(AgentProperties.ServiceDefinition service, Action action) {
        if (isWindows()) {
            Path script = Path.of(required(service.getWindowsStartScript(), "Windows script"))
                    .toAbsolutePath()
                    .normalize();
            if (!Files.isRegularFile(script, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(script)) {
                throw new SecurityException("unsafe Windows script");
            }
            return List.of(
                    "pwsh",
                    "-NoProfile",
                    "-NonInteractive",
                    "-ExecutionPolicy",
                    "AllSigned",
                    "-File",
                    script.toString(),
                    "-Action",
                    action.name());
        }

        String unit = required(service.getSystemdUnit(), "systemd unit");
        if (!unit.matches("[A-Za-z0-9@_.-]+\\.service")) {
            throw new SecurityException("invalid unit");
        }
        return List.of(
                "systemctl",
                switch (action) {
                    case START -> "start";
                    case STOP -> "stop";
                    case RESTART -> "restart";
                    case STATUS -> "is-active";
                },
                unit);
    }

    private static void copyBounded(InputStream input, OutputStream output, long maximumBytes) {
        try (input) {
            byte[] buffer = new byte[8_192];
            long written = 0L;
            for (int read; (read = input.read(buffer)) >= 0; ) {
                written += read;
                if (written > maximumBytes) {
                    throw new IOException("PROCESS_OUTPUT_LIMIT_EXCEEDED");
                }
                output.write(buffer, 0, read);
            }
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    private static boolean terminateTree(Process process) {
        List<ProcessHandle> handles = new ArrayList<>(process.descendants().toList());
        handles.add(process.toHandle());
        handles.stream().filter(ProcessHandle::isAlive).forEach(ProcessHandle::destroy);
        if (awaitStopped(handles, TERMINATION_GRACE_MILLIS / 2L)) {
            return true;
        }
        handles.stream().filter(ProcessHandle::isAlive).forEach(ProcessHandle::destroyForcibly);
        return awaitStopped(handles, TERMINATION_GRACE_MILLIS / 2L);
    }

    private static boolean awaitStopped(List<ProcessHandle> handles, long timeoutMillis) {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        while (handles.stream().anyMatch(ProcessHandle::isAlive)) {
            if (System.nanoTime() >= deadline) {
                return false;
            }
            try {
                Thread.sleep(25L);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return true;
    }

    private AgentProperties.ServiceDefinition service(String serviceId) {
        return properties.getServices().values().stream()
                .filter(candidate -> serviceId != null && serviceId.equals(candidate.getServiceId()))
                .findFirst()
                .orElseThrow(() -> new SecurityException("unknown service"));
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows");
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(field + " missing");
        }
        return value;
    }

    private static String message(Throwable failure) {
        Throwable source = failure instanceof UncheckedIOException && failure.getCause() != null
                ? failure.getCause()
                : failure;
        String value = source == null ? "PROCESS_OUTPUT_RESULT_UNKNOWN" : source.getMessage();
        return sanitize(value == null ? "PROCESS_OUTPUT_RESULT_UNKNOWN" : value);
    }

    private static String sanitize(String value) {
        String safe = SensitiveTextSanitizer.sanitize(value == null ? "" : value);
        return safe.length() > 512 ? safe.substring(0, 512) : safe;
    }

    public enum Action {
        START,
        STOP,
        RESTART,
        STATUS
    }

    public enum ServiceState {
        RUNNING,
        STOPPED,
        UNKNOWN
    }

    public record Result(boolean success, int exitCode, String output, boolean unknownResult) {
        public Result(boolean success, int exitCode, String output) {
            this(success, exitCode, output, false);
        }

        public static Result unknown(int exitCode, String output) {
            return new Result(false, exitCode, output, true);
        }
    }
}
