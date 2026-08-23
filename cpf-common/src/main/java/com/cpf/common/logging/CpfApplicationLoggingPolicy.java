package com.cpf.common.logging;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** 실행 Application 하나의 일반 File Logging 계약입니다. */
public record CpfApplicationLoggingPolicy(
        Path root,
        String applicationName,
        String instanceId,
        Map<String, CpfLogFilePolicy> files) {

    public CpfApplicationLoggingPolicy {
        files = files == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(files));
    }
}
