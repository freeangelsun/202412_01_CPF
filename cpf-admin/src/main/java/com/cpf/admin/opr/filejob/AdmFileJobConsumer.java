package com.cpf.admin.opr.filejob;

import java.util.LinkedHashMap;
import java.util.Map;

/** Upload Template의 실제 업무 Owner Command를 연결하는 SPI입니다. */
public interface AdmFileJobConsumer {
    String templateCode();
    ApplyResult apply(ApplyCommand command);
    void rollback(RollbackCommand command);

    record ApplyCommand(String rowOperationId, Map<String,String> values, String operatorId,
                        String reason, String clientIp) {
        public ApplyCommand {
            rowOperationId = required(rowOperationId, "rowOperationId");
            values = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(
                    java.util.Objects.requireNonNull(values, "values")));
            operatorId = required(operatorId, "operatorId");
            reason = required(reason, "reason");
            clientIp = clientIp == null ? "" : clientIp.trim();
        }
    }
    record RollbackCommand(String rowOperationId, String rollbackToken, String operatorId,
                           String reason, String clientIp) {
        public RollbackCommand {
            rowOperationId = required(rowOperationId, "rowOperationId");
            rollbackToken = required(rollbackToken, "rollbackToken");
            operatorId = required(operatorId, "operatorId");
            reason = required(reason, "reason");
            clientIp = clientIp == null ? "" : clientIp.trim();
        }
    }
    record ApplyResult(String businessKey, String rollbackToken, String message) {
        public ApplyResult {
            businessKey = required(businessKey, "businessKey");
            message = message == null ? "" : message.strip();
        }
    }
    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + "는 필수입니다.");
        return value.trim();
    }
}
