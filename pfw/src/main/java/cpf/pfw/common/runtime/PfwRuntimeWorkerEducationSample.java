package cpf.pfw.common.runtime;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PFW runtime/worker 운영 상태와 ADM 조회 DTO를 학습하기 위한 샘플입니다.
 *
 * <p>실제 다중 WAS heartbeat 저장소 없이도 worker 제어, 감사 사유 필수화, heartbeat,
 * ghost 후보 판정, ADM 상태 DTO를 테스트 가능한 계약으로 보여줍니다.</p>
 */
public class PfwRuntimeWorkerEducationSample {

    public InMemoryRuntimeRegistry runtimeRegistry() {
        InMemoryRuntimeRegistry registry = new InMemoryRuntimeRegistry();
        registry.register("BAT-WORKER-01", "BATCH_WORKER", "RUNNING");
        registry.register("BAT-WORKER-02", "BATCH_WORKER", "RUNNING");
        return registry;
    }

    public static final class InMemoryRuntimeRegistry {
        private final Map<String, WorkerSnapshot> workers = new LinkedHashMap<>();

        public void register(String componentId, String componentType, String state) {
            workers.put(componentId, new WorkerSnapshot(
                    componentId,
                    componentType,
                    state,
                    Instant.parse("2026-07-09T03:00:00Z"),
                    "system"));
        }

        public CpfWorkerControlResult control(CpfWorkerControlRequest request) {
            requireAuditReason(request);
            WorkerSnapshot before = workers.get(request.componentId());
            if (before == null) {
                return new CpfWorkerControlResult(
                        request.componentId(),
                        request.action(),
                        "NOT_FOUND",
                        Instant.parse("2026-07-09T03:00:01Z"),
                        "제어 대상 worker가 없습니다.");
            }

            String nextState = switch (request.action().toUpperCase()) {
                case "PAUSE" -> "PAUSED";
                case "RESUME" -> "RUNNING";
                case "DRAIN" -> "DRAINING";
                case "STOP" -> "STOPPED";
                case "STATUS" -> before.state();
                default -> throw new IllegalArgumentException("지원하지 않는 worker 제어 명령입니다. action=" + request.action());
            };
            workers.put(request.componentId(), new WorkerSnapshot(
                    before.componentId(),
                    before.componentType(),
                    nextState,
                    Instant.parse("2026-07-09T03:00:01Z"),
                    request.requestedBy()));
            return new CpfWorkerControlResult(
                    request.componentId(),
                    request.action().toUpperCase(),
                    nextState,
                    Instant.parse("2026-07-09T03:00:01Z"),
                    "worker 제어 요청을 반영했습니다.");
        }

        public CpfHeartbeatResult heartbeat(CpfHeartbeatRequest request) {
            WorkerSnapshot before = workers.get(request.componentId());
            String type = before == null ? "UNKNOWN" : before.componentType();
            String state = before == null ? "RUNNING" : before.state();
            workers.put(request.componentId(), new WorkerSnapshot(
                    request.componentId(),
                    type,
                    state,
                    request.heartbeatAt(),
                    "heartbeat"));
            return new CpfHeartbeatResult(
                    request.componentId(),
                    "UPDATED",
                    request.heartbeatAt(),
                    "heartbeat를 기록했습니다.");
        }

        public List<CpfRuntimeGhostCandidate> ghostCandidates(Instant now, Duration threshold) {
            return workers.values().stream()
                    .filter(worker -> Duration.between(worker.lastHeartbeatAt(), now).compareTo(threshold) > 0)
                    .map(worker -> new CpfRuntimeGhostCandidate(
                            worker.componentId(),
                            worker.componentType(),
                            worker.lastHeartbeatAt(),
                            "heartbeat 지연 시간이 임계값을 초과했습니다."))
                    .toList();
        }

        public RuntimeAdminStatusDto adminStatus(String componentId) {
            WorkerSnapshot worker = workers.get(componentId);
            if (worker == null) {
                return new RuntimeAdminStatusDto(
                        componentId,
                        "UNKNOWN",
                        "NOT_FOUND",
                        null,
                        Map.of("adminVisible", "true"));
            }
            return new RuntimeAdminStatusDto(
                    worker.componentId(),
                    worker.componentType(),
                    worker.state(),
                    worker.lastHeartbeatAt(),
                    Map.of("adminVisible", "true", "lastUpdatedBy", worker.lastUpdatedBy()));
        }

        private void requireAuditReason(CpfWorkerControlRequest request) {
            String action = request.action().toUpperCase();
            if (!"STATUS".equals(action) && (request.auditReason() == null || request.auditReason().isBlank())) {
                throw new IllegalArgumentException("worker 제어 명령에는 감사 사유가 필요합니다.");
            }
        }
    }

    public record WorkerSnapshot(
            String componentId,
            String componentType,
            String state,
            Instant lastHeartbeatAt,
            String lastUpdatedBy) {
    }

    public record RuntimeAdminStatusDto(
            String componentId,
            String componentType,
            String state,
            Instant lastHeartbeatAt,
            Map<String, String> attributes) {

        public RuntimeAdminStatusDto {
            attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        }
    }
}
