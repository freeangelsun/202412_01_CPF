package com.cpf.education.operations.runtime.model;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
/** EduTargetRecord 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record EduTargetRecord(String targetId, String operationId, String targetKey,
        String state, Map<String,Object> beforeValue, Map<String,Object> afterValue,
        String errorCode, String errorMessage, long version, Instant updatedAt) implements Serializable {
    public EduTargetRecord { beforeValue = Map.copyOf(beforeValue); afterValue = Map.copyOf(afterValue); }
}
