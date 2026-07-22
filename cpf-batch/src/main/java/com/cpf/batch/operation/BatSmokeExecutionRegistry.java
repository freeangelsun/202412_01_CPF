package cpf.bat.operation;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * BAT smoke 실행 결과를 health API에서 확인할 수 있도록 보관합니다.
 */
public class BatSmokeExecutionRegistry {
    private final AtomicReference<Map<String, Object>> latestSuccess = new AtomicReference<>(Map.of());
    private final AtomicReference<Map<String, Object>> latestFailure = new AtomicReference<>(Map.of());
    private final AtomicReference<LocalDateTime> updatedAt = new AtomicReference<>();

    public void recordSuccess(Map<String, Object> result) {
        latestSuccess.set(result == null ? Map.of() : Map.copyOf(result));
        updatedAt.set(LocalDateTime.now());
    }

    public void recordFailure(Map<String, Object> result) {
        latestFailure.set(result == null ? Map.of() : Map.copyOf(result));
        updatedAt.set(LocalDateTime.now());
    }

    public Map<String, Object> snapshot() {
        return Map.of(
                "latestSuccess", latestSuccess.get(),
                "latestFailure", latestFailure.get(),
                "updatedAt", updatedAt.get() == null ? "" : updatedAt.get().toString());
    }
}
