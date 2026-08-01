package com.cpf.reference.edu.runtime.model;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
public record EduTargetRecord(String targetId, String operationId, String targetKey,
        String state, Map<String,Object> beforeValue, Map<String,Object> afterValue,
        String errorCode, String errorMessage, long version, Instant updatedAt) implements Serializable {
    public EduTargetRecord { beforeValue = Map.copyOf(beforeValue); afterValue = Map.copyOf(afterValue); }
}
