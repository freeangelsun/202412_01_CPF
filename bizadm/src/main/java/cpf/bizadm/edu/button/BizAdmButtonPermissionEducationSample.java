package cpf.bizadm.edu.button;

import java.util.Set;

/**
 * 버튼/행위 권한 판단 샘플입니다.
 */
public class BizAdmButtonPermissionEducationSample {

    public String state(Set<String> actions, String action) {
        return actions.contains(action) ? "VISIBLE_ENABLED" : "HIDDEN_OR_DISABLED";
    }
}
