package cpf.pfw.common.runtime;

import java.util.Map;

/**
 * scheduler/worker 제어 요청 후보 DTO입니다.
 */
public record CpfWorkerControlRequest(
        String componentId,
        String action,
        String requestedBy,
        String auditReason,
        Map<String, String> parameters) {

    public CpfWorkerControlRequest {
        if (componentId == null || componentId.isBlank()) {
            throw new IllegalArgumentException("componentId는 필수입니다.");
        }
        action = action == null || action.isBlank() ? "STATUS" : action;
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
