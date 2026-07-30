package com.cpf.batch.worker;

import com.cpf.batch.spi.FileProcessHandler;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

/** 등록된 FILE_PROCESS Handler를 중복 없이 조회하고 미설치 Capability를 fail-closed 처리합니다. */
@Component
public final class BatchFileProcessHandlerRegistry {
    private final Map<String, FileProcessHandler> handlers;

    public BatchFileProcessHandlerRegistry(List<FileProcessHandler> handlers) {
        LinkedHashMap<String, FileProcessHandler> indexed = new LinkedHashMap<>();
        for (FileProcessHandler handler : handlers) {
            String id = normalize(handler.processorId());
            if (indexed.putIfAbsent(id, handler) != null) {
                throw new IllegalStateException("Duplicate FILE_PROCESS handler: " + id);
            }
        }
        this.handlers = Map.copyOf(indexed);
    }

    public FileProcessHandler require(String processorId) {
        FileProcessHandler handler = handlers.get(normalize(processorId));
        if (handler == null) {
            throw new IllegalStateException(
                    "FILE_PROCESS Capability is not installed: processorId=" + processorId);
        }
        return handler;
    }

    public boolean installed(String processorId) {
        return handlers.containsKey(normalize(processorId));
    }

    public Map<String, String> capabilities() {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        handlers.forEach((id, handler) -> result.put(id, handler.getClass().getName()));
        return Map.copyOf(result);
    }

    private static String normalize(String value) {
        if (value == null || !value.matches("[A-Za-z0-9._:-]{1,120}")) {
            throw new IllegalArgumentException("processorId has invalid format");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
