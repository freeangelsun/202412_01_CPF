package fps.pfw.common.workflow;

import java.util.Arrays;

/**
 * 후속 거래 실패 시 프레임워크 로그에 남길 처리 정책입니다.
 * 실제 재시도/보상/수동처리 실행은 업무 구현 또는 후속 워크플로우 엔진이 담당합니다.
 */
public enum FpsWorkflowFailurePolicy {
    FAIL,
    RETRY,
    COMPENSATE,
    PENDING,
    VERIFY,
    MANUAL,
    IGNORE;

    public static FpsWorkflowFailurePolicy from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(policy -> policy.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(null);
    }
}
