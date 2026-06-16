package cpf.pfw.common.workflow;

import java.util.Arrays;

/**
 * ?꾩냽 嫄곕옒 ?ㅽ뙣 ???꾨젅?꾩썙??濡쒓렇???④만 泥섎━ ?뺤콉?낅땲??
 * ?ㅼ젣 ?ъ떆??蹂댁긽/?섎룞泥섎━ ?ㅽ뻾? ?낅Т 援ы쁽 ?먮뒗 ?꾩냽 ?뚰겕?뚮줈???붿쭊???대떦?⑸땲??
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

