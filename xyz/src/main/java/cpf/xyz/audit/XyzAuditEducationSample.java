package cpf.xyz.audit;

import java.time.Instant;
import java.util.Map;

/**
 * 조회/변경/다운로드 감사 로그에 들어갈 핵심 항목 샘플입니다.
 */
public class XyzAuditEducationSample {

    public AuditRecord changed(String actorId, String reason) {
        return new AuditRecord(actorId, "UPDATE", reason, Instant.parse("2026-07-08T00:00:00Z"), Map.of("field", "status"));
    }

    public record AuditRecord(String actorId, String action, String reason, Instant occurredAt, Map<String, String> diff) {
    }
}
