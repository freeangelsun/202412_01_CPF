package external.domain.policy;

import com.cpf.core.api.error.CpfValidationException;
import org.springframework.stereotype.Component;

/** Sample Transaction의 멱등/버전/입력 규칙을 Runtime에서 재사용하는 순수 정책입니다. */
@Component
public final class SampleTransactionPolicy {
    public String requireIdempotencyKey(String value) {
        if (value == null || value.isBlank()) throw new CpfValidationException("idempotencyKey는 필수입니다.");
        String normalized = value.trim();
        if (normalized.length() > 180) throw new CpfValidationException("idempotencyKey는 180자 이하여야 합니다.");
        return normalized;
    }
    /** requireSampleKey 작업을 CPF 표준 계약에 따라 수행한다. */
    public String requireSampleKey(String value) {
        if (value == null || value.isBlank()) throw new CpfValidationException("sampleKey는 필수입니다.");
        String normalized = value.trim().toUpperCase(java.util.Locale.ROOT);
        if (normalized.length() > 100) throw new CpfValidationException("sampleKey는 100자 이하여야 합니다.");
        return normalized;
    }
    public String requireItemName(String value) {
        if (value == null || value.isBlank()) throw new CpfValidationException("itemName은 필수입니다.");
        String normalized = value.trim();
        if (normalized.length() > 200) throw new CpfValidationException("itemName은 200자 이하여야 합니다.");
        return normalized;
    }
    /** requireStatusCode 작업을 CPF 표준 계약에 따라 수행한다. */
    public String requireStatusCode(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
        if (!normalized.equals("ACTIVE") && !normalized.equals("INACTIVE"))
            throw new CpfValidationException("statusCode는 ACTIVE 또는 INACTIVE여야 합니다.");
        return normalized;
    }
    public void requireExpectedVersion(long version) {
        if (version < 0) throw new CpfValidationException("expectedVersion은 0 이상이어야 합니다.");
    }
}
