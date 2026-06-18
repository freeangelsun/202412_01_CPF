package cpf.pfw.common.workflow;

import java.util.Arrays;

/**
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
public enum CpfWorkflowFailurePolicy {
    FAIL,
    RETRY,
    COMPENSATE,
    PENDING,
    VERIFY,
    MANUAL,
    IGNORE;

    public static CpfWorkflowFailurePolicy from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(policy -> policy.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(null);
    }
}

