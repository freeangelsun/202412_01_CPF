package cpf.pfw.common.servicecall;

import java.util.Objects;

/**
 * 업무 코드가 허용된 named policy만 선택할 수 있게 하는 typed 고급 옵션입니다.
 *
 * <p>raw timeout과 retry 횟수는 이 계약에 노출하지 않으며 중앙 정책의 상한과 감사를 우회할 수 없습니다.</p>
 *
 * @param policyId 중앙 정책 식별자
 * @since 1.0.0
 */
public record CpfServiceCallOptions(CpfPolicyId policyId) {

    /** options를 검증합니다. */
    public CpfServiceCallOptions {
        Objects.requireNonNull(policyId, "policyId는 필수입니다.");
    }

    /**
     * 일반 조회 기본 정책을 반환합니다.
     *
     * @return 기본 조회 옵션
     */
    public static CpfServiceCallOptions defaultQuery() {
        return new CpfServiceCallOptions(CpfPolicyId.DEFAULT_QUERY);
    }
}
