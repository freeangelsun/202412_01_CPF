package cpf.pfw.common.audit;

import java.time.Instant;
import java.util.Map;

/**
 * 변경 작업에서 누가, 언제, 무엇을, 왜 바꿨는지 남기는 감사 context 샘플입니다.
 */
public class PfwAuditContextEducationSample {

    public AuditContext changed(String actorId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("감사 사유는 필수입니다.");
        }
        return new AuditContext(
                actorId,
                "UPDATE",
                reason,
                Instant.parse("2026-07-08T00:00:00Z"),
                Map.of("before.status", "READY", "after.status", "DONE"));
    }

    public record AuditContext(
            String actorId,
            String action,
            String reason,
            Instant occurredAt,
            Map<String, String> diff) {
    }
}
