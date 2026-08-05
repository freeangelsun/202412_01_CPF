package com.cpf.core.common.logging.file;

import org.springframework.core.env.Environment;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;

public final class CpfFileLogProcessWorker {
    private CpfFileLogProcessWorker() {}

    public static void main(String[] args) {
        if (args.length != 3) throw new IllegalArgumentException("root worker count required");
        Path root = Path.of(args[0]);
        String worker = args[1];
        int count = Integer.parseInt(args[2]);
        CpfFileLogWriter writer = new CpfFileLogWriter(new WorkerEnvironment(root), Clock.systemUTC());
        for (int index = 0; index < count; index++) {
            writer.writeEventAtRelativePath(
                    Path.of("cross-process", "shared.log"),
                    Map.of("worker", worker, "sequence", index));
        }
    }

    private record WorkerEnvironment(Path root) implements Environment {
        @Override
        public String getProperty(String key) {
            return switch (key) {
                case "cpf.logging.file.base-path" -> root.toAbsolutePath().toString();
                case "cpf.logging.file.enabled" -> "true";
                case "cpf.logging.file.archive-compress-enabled" -> "false";
                case "cpf.logging.file.retention-check-interval-ms" -> "86400000";
                case "cpf.logging.file.total-size-cap" -> "0B";
                case "cpf.logging.file.process-lock-timeout-ms" -> "30000";
                case "cpf.framework.module-id" -> "CORE";
                case "CPF_INSTANCE_ID" -> "shared-instance";
                case "cpf.environment" -> "test";
                case "cpf.logging.file.timezone" -> "UTC";
                default -> null;
            };
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getProperty(String key, Class<T> type, T defaultValue) {
            String value = getProperty(key);
            if (value == null) return defaultValue;
            if (type == Boolean.class) return (T) Boolean.valueOf(value);
            if (type == Long.class) return (T) Long.valueOf(value);
            if (type == Integer.class) return (T) Integer.valueOf(value);
            return (T) value;
        }

        @Override
        public String[] getActiveProfiles() {
            return new String[] {"test"};
        }
    }
}
