package com.cpf.batch.worker;

import com.cpf.batch.runtime.SensitiveTextSanitizer;
import org.springframework.stereotype.Component;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 승인된 Script Catalog만 실행합니다. Command line 문자열, shell expansion, redirection을 받지 않습니다.
 */
@Component
public class ApprovedShellExecutor {
    private final WorkerOperationalProperties properties;
    public ApprovedShellExecutor(WorkerOperationalProperties properties) { this.properties = properties; }

    public Result execute(String scriptKey, Map<String,Object> parameters) throws Exception {
        WorkerOperationalProperties.ShellDefinition definition = properties.getScripts().get(scriptKey);
        if (definition == null) throw new SecurityException("Approved script not found: " + scriptKey);
        Path executable = Path.of(Objects.requireNonNull(definition.getExecutable(), "executable")).toAbsolutePath().normalize();
        if (!Files.isRegularFile(executable)) throw new IllegalStateException("Approved script file not found");
        Set<String> allowed = Set.copyOf(definition.getAllowedParameters());
        for (String key : parameters.keySet()) if (!allowed.contains(key)) throw new SecurityException("Unapproved shell parameter: " + key);
        List<String> command = new ArrayList<>(); command.add(executable.toString()); command.addAll(definition.getFixedArguments());
        for (String key : definition.getAllowedParameters()) {
            if (!parameters.containsKey(key)) continue;
            command.add("--" + key); command.add(String.valueOf(parameters.get(key)));
        }
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        boolean finished = process.waitFor(Math.max(1, definition.getTimeoutSeconds()), TimeUnit.SECONDS);
        if (!finished) { process.destroyForcibly(); return new Result(false, -1, "TIMEOUT"); }
        String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        return new Result(process.exitValue() == 0, process.exitValue(), SensitiveTextSanitizer.sanitize(output));
    }
    public record Result(boolean success,int exitCode,String output) {}
}
