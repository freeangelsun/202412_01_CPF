package cpf.bizadm.edu.auth;

import java.util.Set;

/**
 * 메뉴 권한 판단 샘플입니다.
 */
public class BizAdmMenuPermissionEducationSample {

    public boolean canAccess(Set<String> menuIds, String menuId) {
        return menuIds.contains(menuId);
    }
}
