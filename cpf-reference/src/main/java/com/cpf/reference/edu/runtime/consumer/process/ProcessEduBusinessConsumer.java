package com.cpf.reference.edu.runtime.consumer.process;

import com.cpf.reference.edu.runtime.application.EduValidationException;
import com.cpf.reference.edu.runtime.consumer.*;
import com.cpf.reference.edu.runtime.model.EduExecutionCommand;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Allowlisted repository script consumer. It never invokes a shell or concatenates user input. */
public final class ProcessEduBusinessConsumer implements EduBusinessConsumer {
    private final Path repositoryRoot;
    private final ObjectMapper json;

    public ProcessEduBusinessConsumer(Path repositoryRoot, ObjectMapper json) {
        this.repositoryRoot = Objects.requireNonNull(repositoryRoot).toAbsolutePath().normalize();
        this.json = Objects.requireNonNull(json);
    }

    @Override
    public EduConsumerType type() {
        return EduConsumerType.PROCESS;
    }

    @Override
    public EduBusinessConsumerResult invoke(EduConsumerBinding binding,
                                            EduExecutionCommand command,
                                            long fencingToken) {
        Path payloadFile = null;
        Process process = null;
        try {
            Path script = repositoryRoot.resolve(binding.entryPoint()).normalize();
            if (!script.startsWith(repositoryRoot) || !Files.isRegularFile(script)) {
                throw new EduValidationException("allowlisted script missing: " + binding.entryPoint());
            }

            List<String> executable = commandFor(script);
            Map<String, String> environment = new LinkedHashMap<>();
            environment.put("CPF_EDU_REQUIREMENT_ID", binding.requirementId());
            environment.put("CPF_EDU_BUSINESS_KEY", command.businessKey());
            environment.put("CPF_EDU_DATA_SCOPE", command.dataScope());
            environment.put("CPF_EDU_TRACE_ID", command.traceId());
            environment.put("CPF_EDU_FENCING_TOKEN", Long.toString(fencingToken));

            payloadFile = Files.createTempFile("cpf-edu-payload-", ".json");
            Files.writeString(payloadFile, json.writeValueAsString(command.payload()), StandardCharsets.UTF_8);
            environment.put("CPF_EDU_PAYLOAD_FILE", payloadFile.toString());

            ProcessBuilder builder = new ProcessBuilder(executable)
                    .directory(repositoryRoot.toFile())
                    .redirectErrorStream(true);
            builder.environment().putAll(environment);
            process = builder.start();

            boolean completed = process.waitFor(binding.timeoutSeconds(), TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
                throw new IllegalStateException("script timeout after " + binding.timeoutSeconds() + "s");
            }

            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (process.exitValue() != 0) {
                throw new IllegalStateException("script exit=" + process.exitValue() + " output=" + sanitize(output));
            }
            return EduBusinessConsumerResult.completed("PROCESS_OK", Map.of(
                    "script", binding.entryPoint(),
                    "exitCode", process.exitValue(),
                    "outputDigest", Integer.toHexString(output.hashCode())));
        } catch (EduValidationException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("process consumer interrupted", e);
        } catch (Exception e) {
            throw new IllegalStateException("process consumer failed: " + e.getMessage(), e);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            if (payloadFile != null) {
                try {
                    Files.deleteIfExists(payloadFile);
                } catch (Exception ignored) {
                    // The payload contains no secrets and is created under the OS temp directory.
                    // Cleanup failure is intentionally not allowed to hide the business outcome.
                }
            }
        }
    }

    private static List<String> commandFor(Path script) {
        String name = script.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".ps1")) {
            return List.of("pwsh", "-NoProfile", "-NonInteractive", "-File", script.toString());
        }
        if (name.endsWith(".py")) {
            return List.of("python", script.toString());
        }
        if (name.endsWith(".sh")) {
            return List.of("bash", script.toString());
        }
        throw new EduValidationException("unsupported script type: " + name);
    }

    private static String sanitize(String text) {
        String value = text.replaceAll(
                "(?i)(password|secret|token|api[_-]?key)\\s*[:=]\\s*\\S+",
                "$1=***");
        return value.length() > 500 ? value.substring(0, 500) : value;
    }
}
