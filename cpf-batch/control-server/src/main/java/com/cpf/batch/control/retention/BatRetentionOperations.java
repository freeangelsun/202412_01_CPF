package com.cpf.batch.control.retention;

import com.cpf.core.api.retention.CpfRetentionCommand;
import com.cpf.core.api.retention.CpfRetentionOperations;
import com.cpf.core.api.retention.CpfRetentionResult;
import com.cpf.core.spi.retention.CpfRetentionTargetHandler;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** BAT가 소유한 Retention Handler Router. */
@Service
public class BatRetentionOperations implements CpfRetentionOperations {
    private final Map<String,CpfRetentionTargetHandler> handlers;
    public BatRetentionOperations(List<CpfRetentionTargetHandler> handlers) {
        Map<String,CpfRetentionTargetHandler> map = new LinkedHashMap<>();
        for (CpfRetentionTargetHandler h : handlers) {
            String key = h.target().trim().toUpperCase();
            if (map.putIfAbsent(key, h) != null) throw new IllegalStateException("중복 Retention target: " + key);
        }
        this.handlers = Map.copyOf(map);
    }
    @Override public Set<String> targets() { return handlers.keySet(); }
    @Override public CpfRetentionResult execute(CpfRetentionCommand command) {
        String target = command.policy().target().toUpperCase();
        CpfRetentionTargetHandler handler = handlers.get(target);
        if (handler == null) throw new IllegalArgumentException("지원하지 않는 Retention target: " + target);
        return handler.execute(command);
    }
}
