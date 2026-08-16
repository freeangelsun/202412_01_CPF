package external.domain.policy;

import com.cpf.core.api.error.CpfValidationException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/** Generated Domain 정책의 멱등성 키와 버전 경계 규칙을 검증합니다. */
class SampleTransactionPolicyTest {
    private final SampleTransactionPolicy policy=new SampleTransactionPolicy();
    @Test void normalizesIdempotencyKey() { assertThat(policy.requireIdempotencyKey("  K-1 ")).isEqualTo("K-1"); }
    @Test void rejectsBlankIdempotencyKey() { assertThatThrownBy(() -> policy.requireIdempotencyKey(" ")).isInstanceOf(CpfValidationException.class); }
    @Test void rejectsNegativeVersion() { assertThatThrownBy(() -> policy.requireExpectedVersion(-1)).isInstanceOf(CpfValidationException.class); }
}
