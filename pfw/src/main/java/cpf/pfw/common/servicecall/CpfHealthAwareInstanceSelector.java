package cpf.pfw.common.servicecall;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 서비스 instance 목록에서 호출 가능한 대상을 선택합니다.
 */
public class CpfHealthAwareInstanceSelector {

    public Optional<Map<String, Object>> select(List<Map<String, Object>> instances, String requestedInstanceId) {
        if (instances == null || instances.isEmpty()) {
            return Optional.empty();
        }
        if (requestedInstanceId != null && !requestedInstanceId.isBlank()) {
            return instances.stream()
                    .filter(row -> requestedInstanceId.equals(String.valueOf(row.get("instanceId"))))
                    .findFirst();
        }
        return instances.stream()
                .filter(this::activeAndUp)
                .findFirst()
                .or(() -> instances.stream().filter(this::active).findFirst())
                .or(() -> instances.stream().findFirst());
    }

    private boolean activeAndUp(Map<String, Object> row) {
        return active(row) && "UP".equalsIgnoreCase(String.valueOf(row.get("instanceStatus")));
    }

    private boolean active(Map<String, Object> row) {
        return "Y".equalsIgnoreCase(String.valueOf(row.get("activeYn")));
    }
}
